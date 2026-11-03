package com.example.minicodevault.controller;

import com.example.minicodevault.dto.Views;
import com.example.minicodevault.service.AppException;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<Views.ErrorView> application(AppException error) {
        return ResponseEntity.status(error.getStatus())
                .body(new Views.ErrorView(error.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Views.ErrorView> validation(IllegalArgumentException error) {
        return ResponseEntity.badRequest()
                .body(
                        new Views.ErrorView(
                                error.getMessage() == null
                                        ? "Data tidak valid."
                                        : error.getMessage()));
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Views.ErrorView> malformed(Exception error) {
        return ResponseEntity.badRequest()
                .body(
                        new Views.ErrorView(
                                "Format data tidak valid. Periksa angka, jenis course, dan tingkat"
                                        + " kesulitan soal."));
    }
}
