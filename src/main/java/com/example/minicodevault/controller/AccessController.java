package com.example.minicodevault.controller;

import com.example.minicodevault.dto.Views;
import com.example.minicodevault.service.AccessService;

import jakarta.servlet.http.HttpSession;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AccessController {
    public record ReviewInput(int rating, String comment) {}

    private final AccessService access;
    private final SessionIdentity identity;

    public AccessController(AccessService access, SessionIdentity identity) {
        this.access = access;
        this.identity = identity;
    }

    @GetMapping("/library")
    public List<Views.Access> library(HttpSession session) {
        return access.library(identity.current(session));
    }

    @GetMapping("/library/{id}")
    public Views.Lesson lesson(@PathVariable long id, HttpSession session) {
        return access.lesson(identity.current(session), id);
    }

    @PostMapping("/library/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable long id, HttpSession session) {
        byte[] bytes =
                access.download(identity.current(session), id).getBytes(StandardCharsets.UTF_8);
        String disposition =
                ContentDisposition.attachment()
                        .filename("codevault-ebook-" + id + ".txt")
                        .build()
                        .toString();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .cacheControl(CacheControl.noStore())
                .body(bytes);
    }

    @GetMapping("/library/{id}/video")
    public ResponseEntity<Resource> video(@PathVariable long id, HttpSession session) {
        access.checkVideo(identity.current(session), id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/webm"))
                .cacheControl(CacheControl.noStore())
                .body(new ClassPathResource("media/demo.webm"));
    }

    @PostMapping("/learning/{id}/review")
    public Views.Result review(
            @PathVariable long id, @RequestBody ReviewInput input, HttpSession session) {
        access.review(identity.current(session), id, input.rating(), input.comment());
        return Views.Result.success();
    }
}
