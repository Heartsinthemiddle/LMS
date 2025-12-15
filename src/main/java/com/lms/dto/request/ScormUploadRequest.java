package com.lms.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.http.codec.multipart.FilePart;

@Data
public class ScormUploadRequest {

    @Schema(type = "string", format = "binary", description = "SCORM ZIP file")
    private FilePart file;
}
