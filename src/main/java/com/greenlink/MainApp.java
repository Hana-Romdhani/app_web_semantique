package com.greenlink;

import com.greenlink.config.FusekiServerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.annotation.PostConstruct;

@SpringBootApplication

public class MainApp {

    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
    }

    @PostConstruct
    public void startFusekiServer() {
        // Démarre le serveur Fuseki après l'initialisation de l'application Spring Boot
        FusekiServerConfig.startFusekiServer();
    }
}
