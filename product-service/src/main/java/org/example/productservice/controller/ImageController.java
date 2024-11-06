package org.example.productservice.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.core.io.UrlResource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/uploads")
public class ImageController {

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
    private final String uploadDir = System.getProperty("user.dir") + "/uploads/images/";

    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            logger.info("File path being accessed: " + filePath);

            Resource resource = new UrlResource(filePath.toUri());
            logger.info("Resource created: " + resource);

            if (resource.exists() && resource.isReadable()) {
                logger.info("Resource exists and is readable.");
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                logger.warn("Resource not found or is not readable.");
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            logger.error("MalformedURLException: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
