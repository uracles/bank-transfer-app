package com.bank.transfer.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

//    public static final String AUTHORIZATION_HEADER = "Authorization";

//    @Bean
//    public OpenAPI myOpenAPI() {
//
//        Contact contact = new Contact();
//        contact.setEmail("contact@coronationmb.com");
//        contact.setName("CoronationMB");
//        contact.setUrl("https://www.coronationmb.com");
//
//        License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");
//
//        Info info = new Info()
//                .title("CoronationMB NIBSS IDH-V2")
//                .version("1.0")
//                .contact(contact)
//                .description("This Specification exposes endpoints for Sending transactions to nibss and summary")
//                .termsOfService("https://www.coronationmb.com/terms")
//                .license(mitLicense);
//
//        return new OpenAPI()
//                .info(info)
//                .specVersion(SpecVersion.V30)
//                .addSecurityItem(new SecurityRequirement()
//                        .addList(AUTHORIZATION_HEADER))
//                .components(new Components()
//                        .addSecuritySchemes(AUTHORIZATION_HEADER, new SecurityScheme()
//                                .name(AUTHORIZATION_HEADER)
//                                .type(SecurityScheme.Type.HTTP)
//                                .scheme("Bearer")
//                                .bearerFormat("JWT")
//                        )
//                );
//    }

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
