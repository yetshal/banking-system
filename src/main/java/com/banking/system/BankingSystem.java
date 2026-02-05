package com.banking.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankingSystem {

    public static void main(String[] args) {
        SpringApplication.run(BankingSystem.class, args);
        System.out.println("\n=== Banking System API Started ===");
        System.out.println("URL: http://localhost:8080");
        System.out.println("===================================\n");
    }
}
