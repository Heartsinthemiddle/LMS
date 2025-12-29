package com.lms.service;

import com.lms.dto.response.ImportJobStatus;
import com.lms.dto.response.XapiStatementsResponse;
import com.lms.util.MultipartInputStreamFileResource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
import java.util.List;
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

    public Mono<String> getLaunchLink(String registrationId) {
        Map<String, Object> body = Map.of(
                "redirectOnExitUrl", "http://127.0.0.1:8080/course",
                "launchType", "iframe",
                "expiry", 0,
                "tracking", false,
                "redirect", true,
                "newWindow", false
        );

        return rusticiWebClient.post()
                .uri("/registrations/{registrationId}/launchLink", registrationId)
                .headers(h -> h.setBasicAuth(appId, secret))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(r -> (String) r.get("launchLink"));
    }





    public Mono<Void> createRegistration(
            String courseId,
            String registrationId,
            String learnerId,
            String firstName,
            String lastName,
            String email
    ) {
        Map<String, Object> payload = Map.of(
                "courseId", courseId,
                "registrationId", registrationId,
                "learner", Map.of(
                        "id", learnerId,
                        "firstName", firstName,
                        "lastName", lastName,
                        "email", email
                )
        );

        return rusticiWebClient.post()
                .uri("/registrations")
                .headers(h -> {
                    h.setBasicAuth(appId, secret);
                    h.setContentType(MediaType.APPLICATION_JSON);
                })
                .bodyValue(payload)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .doOnNext(body ->
                                        System.err.println("CREATE REG ERROR: " + body))
                                .map(RuntimeException::new)
                )
                .bodyToMono(Void.class)
                .doOnSuccess(v ->
                        System.out.println("REGISTRATION CREATED SUCCESSFULLY"))
                .doOnError(e ->
                        System.err.println("REGISTRATION FAILED: " + e.getMessage()));
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

    public Mono<Map<String, Object>> getRegistrationProgress(String registrationId) {
        return rusticiWebClient.get()
                .uri("/registrations/{registrationId}/", registrationId)
                .headers(h -> h.setBasicAuth(appId, secret))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    // For registration statements
    public Mono<List<Map<String, Object>>> getRegistrationStatements(String registrationId) {
        return rusticiWebClient.get()
                .uri("/registrations/{registrationId}/xAPIStatements", registrationId)
                .headers(h -> h.setBasicAuth(appId, secret))
                .retrieve()
                .bodyToMono(XapiStatementsResponse.class)
                .map(XapiStatementsResponse::getStatements);
    }

}
