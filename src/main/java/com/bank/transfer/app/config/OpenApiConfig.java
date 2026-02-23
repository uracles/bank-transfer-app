package com.bank.transfer.app.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank Transfer Service API")
                        .description("REST API for simulating money transfer operations between bank accounts")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Bank Transfer Team")
                                .email("api@bank.com")));
    }
}
