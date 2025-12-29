package com.lms.service;

import com.lms.dto.response.ImportJobStatus;
import com.lms.util.MultipartInputStreamFileResource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class RusticiClient {

    private final WebClient rusticiWebClient;

    @Value("${rustici.app-id}")
    private String appId;

    @Value("${rustici.secret}")
    private String secret;

    /**
     * Upload SCORM ZIP to Rustici Cloud
     */
    public Mono<String> uploadScormZip(MultipartFile file, String courseId) {

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource())
                .filename(file.getOriginalFilename())
                .contentType(MediaType.APPLICATION_OCTET_STREAM);

        return rusticiWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/courses/importJobs/upload")
                        .queryParam("courseId", courseId)
                        .queryParam("mayCreateNewVersion", true)
                        .build()
                )
                .headers(headers -> {
                    headers.setBasicAuth(appId, secret);
                    headers.add("uploadedContentType", "application/zip");
                })
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(Map.class)
                .doOnNext(response -> System.out.println("Success Response: " + response))
                .map(response -> (String) response.get("result")); // return only course_id
    }


    public Mono<ImportJobStatus> getImportJobStatus(String importJobId) {
        return rusticiWebClient.get()
                .uri("/courses/importJobs/{importJobId}", importJobId)
                .headers(headers -> headers.setBasicAuth(appId, secret))
                .retrieve()
                .bodyToMono(ImportJobStatus.class)
                .doOnNext(status -> System.out.println("Import Job Status: " + status.getStatus()));
    }






    /**
     * Fetch SCORM Launch URL
     */
    public Mono<String> getLaunchUrl(String courseId, String learnerId) {
        return rusticiWebClient.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/courses/{courseId}/launchLink")
                                .queryParam("learnerId", learnerId)
                                .build(courseId)
                )
                .headers(headers -> headers.setBasicAuth(appId, secret))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("launchLink"));
    }

    public String buildLaunchUrl(String courseId, String learnerId) {
        return String.format(
                "https://cloud.scorm.com/sc/launch/%s?learnerId=%s&appId=%s",
                courseId,
                learnerId,
                appId
        );
    }

}
