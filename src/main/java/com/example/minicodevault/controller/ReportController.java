package com.example.minicodevault.controller;

import com.example.minicodevault.dto.Views;
import com.example.minicodevault.service.ReportService;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instructor/report")
public class ReportController {
    private final ReportService reports;
    private final SessionIdentity identity;

    public ReportController(ReportService reports, SessionIdentity identity) {
        this.reports = reports;
        this.identity = identity;
    }

    @GetMapping
    public Views.Report report(HttpSession session) {
        return reports.report(identity.current(session));
    }
}
