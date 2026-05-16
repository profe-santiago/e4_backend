package com.tickets.event_service.upload;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
@Tag(name = "Upload")
public class ImageUploadController {

    private final CloudinaryStorageService storageService;

    public ImageUploadController(CloudinaryStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Subir imagen de evento a Cloudinary")
    public Map<String, String> uploadImage(@RequestPart("file") MultipartFile file) {
        return Map.of("imageUrl", storageService.upload(file));
    }
}
