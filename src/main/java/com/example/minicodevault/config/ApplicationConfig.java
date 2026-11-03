package com.example.minicodevault.config;

import com.example.minicodevault.repository.InMemoryStore;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ApplicationConfig {
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public InMemoryStore store() {
        InMemoryStore store = new InMemoryStore();
        DemoData.load(store);
        return store;
    }
}
