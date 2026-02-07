package com.comp5348.bank.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * DataLoader is now handled by Flyway migrations.
 * This class is kept for compatibility but does nothing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info("[Bank] Database initialization handled by Flyway migrations");
    }
}