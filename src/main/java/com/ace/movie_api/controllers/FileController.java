package com.ace.movie_api.controllers;

import com.ace.movie_api.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping("/file")
public class FileController {
    private final FileService fileService;

    @Value("${project.poster}")
    private String path;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFileHandler(@RequestPart MultipartFile file) throws IOException {
        String fileName = fileService.uploadFile(path, file);
        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{filename}")
    public void serverFileHandler(@PathVariable String filename, HttpServletResponse response) throws IOException {
        InputStream resourceFile = fileService.getResourceFile(path, filename);
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        StreamUtils.copy(resourceFile, response.getOutputStream());

    }
}
