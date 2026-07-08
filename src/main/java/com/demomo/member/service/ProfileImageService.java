package com.demomo.member.service;

import com.demomo.global.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class ProfileImageService {
    private static final long MAX_SIZE = 5L * 1024 * 1024;
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp"
    );
    private final Path uploadDirectory = Path.of("uploads", "profile").toAbsolutePath().normalize();

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "업로드할 이미지를 선택해 주세요.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "프로필 사진은 5MB 이하만 업로드할 수 있습니다.");
        }
        String contentType = file.getContentType();
        String extension = contentType == null ? null : EXTENSIONS.get(contentType);
        if (extension == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "JPG, PNG, GIF, WebP 이미지만 업로드할 수 있습니다.");
        }
        try {
            Files.createDirectories(uploadDirectory);
            String filename = UUID.randomUUID() + extension;
            Path destination = uploadDirectory.resolve(filename).normalize();
            if (!destination.getParent().equals(uploadDirectory)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "올바르지 않은 파일 이름입니다.");
            }
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/profile/" + filename;
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "프로필 사진을 저장하지 못했습니다.");
        }
    }

    public void delete(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/profile/")) {
            return;
        }
        String filename = imageUrl.substring("/uploads/profile/".length());
        Path target = uploadDirectory.resolve(filename).normalize();
        if (!target.getParent().equals(uploadDirectory)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // 이미지 정리 실패 때문에 계정 탈퇴를 중단하지 않습니다.
        }
    }
}
