package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;

@RestController
public class AwsController
{
    @Autowired
    private S3Service s3Service;

    @PostMapping("/uploadFile")
    public ResponseEntity<?> uploadFile(@RequestParam MultipartFile file)
    {
        if(file == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error!!! -File not found.");
        }

        s3Service.uploadFile(file);
        return ResponseEntity.ok().body("Picture uploaded successfully.");
    }
}
