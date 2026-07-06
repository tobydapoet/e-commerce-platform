package com.example.e_commerce.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class UploadService {

    private final Cloudinary cloudinary;

    private final ExecutorService uploadExecutor = Executors.newFixedThreadPool(8);

    public UploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String upload(MultipartFile file, String folder) {
        try {
            String publicId = UUID.randomUUID().toString();
            Map uploadRes = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", folder, "public_id", publicId)
            );
            return uploadRes.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    public List<String> uploadMultiple(List<MultipartFile> files, String folder) {
        List<CompletableFuture<String>> tasks = files.stream()
                .map(file -> CompletableFuture.supplyAsync(
                        () -> upload(file, folder),
                        uploadExecutor
                ))
                .toList();

        return tasks.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    public void delete(String url) {
        String[] parts = url.split("/upload/");
        if (parts.length < 2)
            throw new RuntimeException("Invalid Cloudinary URL");

        String path = parts[1];

        int firstSlash = path.indexOf("/");
        if (firstSlash != -1) {
            path = path.substring(firstSlash + 1);
        }

        int dotIndex = path.lastIndexOf(".");
        if (dotIndex != -1) {
            path = path.substring(0, dotIndex);
        }

        try {
            cloudinary.uploader().destroy(path, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Delete file error", e);
        }
    }

    public void deleteMultiple(List<String> urls) {
        List<CompletableFuture<Void>> tasks = urls.stream()
                .map(url -> CompletableFuture.runAsync(() -> delete(url), uploadExecutor))
                .toList();

        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
    }
}