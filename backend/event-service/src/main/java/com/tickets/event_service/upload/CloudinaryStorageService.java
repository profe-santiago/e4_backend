package com.tickets.event_service.upload;

import com.cloudinary.Cloudinary;
import com.tickets.event_service.exception.ImageUploadException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryStorageService {

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    private final Cloudinary cloudinary;

    public CloudinaryStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImageUploadException("El archivo no puede estar vacío");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new ImageUploadException("Solo se permiten imágenes JPEG, PNG o WEBP");
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), Map.of(
                    "folder",    "events",
                    "public_id", UUID.randomUUID().toString(),
                    "overwrite", false
            ));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new ImageUploadException("Error al subir la imagen: " + e.getMessage());
        }
    }
}
