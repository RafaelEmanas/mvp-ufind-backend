package com.ufind.ufindapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ufind.ufindapp.dto.PresignedUploadDTO;
import com.ufind.ufindapp.service.FileService;

@RestController
@RequestMapping("/api")
public class FileController {

    private final FileService fileService;

    public FileController(
        FileService fileService
    ){
        this.fileService = fileService;
    }
    
    @GetMapping("/upload-url")
    public ResponseEntity<PresignedUploadDTO> generateUrl(
        @RequestParam
        String contentType
    ) {

        PresignedUploadDTO presignedUploadDTO = fileService.generatePutPresignedUrl(contentType);
        return ResponseEntity.status(200).body(presignedUploadDTO);

    }

}
