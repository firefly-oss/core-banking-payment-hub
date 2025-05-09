package com.catalis.core.banking.payments.hub.web;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * Main application class for the Core Banking Payment Hub.
 * Configures component scanning for all modules.
 */

@SpringBootApplication(
        scanBasePackages = {
                "com.catalis.core.banking.payments",
                "com.catalis.common.web"  // Scan common web library configurations
        }
)
@EnableWebFlux
@ConfigurationPropertiesScan
@OpenAPIDefinition(
        info = @Info(
                title = "${spring.application.name}",
                version = "${spring.application.version}",
                description = "${spring.application.description}",
                contact = @Contact(
                        name = "${spring.application.team.name}",
                        email = "${spring.application.team.email}"
                )
        ),
        servers = {
                @Server(
                        url = "http://core.catalis.vc/payment-hub",
                        description = "Development Environment"
                ),
                @Server(
                        url = "/",
                        description = "Local Development Environment"
                )
        }
)
public class CoreBankingPaymentHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreBankingPaymentHubApplication.class, args);
    }
}
