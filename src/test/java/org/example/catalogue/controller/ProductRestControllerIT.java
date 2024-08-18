package org.example.catalogue.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class ProductRestControllerIT {
    @Autowired
    MockMvc mockMvc;

    @Test
    @Sql("/sql/products.sql")
    void findProduct_ProductExists_ReturnsProductsList() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/products/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "view_catalogue")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                                {
                                    "id": 1,
                                    "title": "Товар №1",
                                    "details": "Описание товара №1"
                                }""")
                );
    }

    @Test
    void findProduct_ProductDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/products/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "view_catalogue")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql("/sql/products.sql")
    void findProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/products/1")
                .with(jwt());

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql("/sql/products.sql")
    void updateProduct_RequestIsValid_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/catalogue-api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Новое название",
                            "details": "Новое описание"
                        }""")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @Sql("/sql/products.sql")
    void updateProduct_RequestIsInvalid_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/catalogue-api/products/1")
                .locale(Locale.of("ru"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": " ",
                            "details": null
                        }""")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                                {
                                    "errors": ["Название товара должно быть от 3 до 50 символов"]
                                }""")
                );
    }

    @Test
    void updateProduct_ProductDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/catalogue-api/products/1")
                .locale(Locale.of("ru"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Новое название",
                            "details": "Новое описание"
                        }""")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/catalogue-api/products/1")
                .locale(Locale.of("ru"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Новое название",
                            "details": "Новое описание"
                        }""")
                .with(jwt());

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql("/sql/products.sql")
    void deleteProduct_ProductExists_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/catalogue-api/products/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_ProductDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/catalogue-api/products/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/catalogue-api/products/1")
                .with(jwt());

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
