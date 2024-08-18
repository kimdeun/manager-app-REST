package org.example.catalogue.controller;

import org.example.catalogue.controller.payload.UpdateProductPayload;
import org.example.catalogue.entity.Product;
import org.example.catalogue.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductRestControllerTest {

    @Mock
    ProductService productService;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    ProductRestController controller;

    @Test
    void getProduct_ProductExists_ReturnsProduct() {
        var product = new Product(1, "Название товара", "Описание товара");
        doReturn(Optional.of(product)).when(productService).findProduct(1);

        var result = controller.getProduct(1);

        assertEquals(product, result);
    }

    @Test
    void getProduct_ProductDoesNotExist_ThrowsNoSuchElementException() {
        var exception = assertThrows(NoSuchElementException.class, () -> controller.getProduct(1));

        assertEquals("catalogue.errors.product.not_found", exception.getMessage());
    }

    @Test
    void findProduct_ReturnsProduct() {
        var product = new Product(1, "Название товара", "Описание товара");

        var result = controller.findProduct(product);

        assertEquals(product, result);
    }

    @Test
    void updateProduct_RequestIsValid_ReturnsNoContent() throws BindException {
        var payload = new UpdateProductPayload("Новое название", "Новое описание");
        var bindingResult = new MapBindingResult(Map.of(), "payload");

        var result = controller.updateProduct(1, payload, bindingResult);

        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        verify(productService).updateProduct(1, "Новое название", "Новое описание");
    }

    @Test
    void updateProduct_RequestIsInvalid_ReturnsBadRequest() {
        var payload = new UpdateProductPayload("   ", null);
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "title", "error"));

        var exception = assertThrows(BindException.class, () -> controller.updateProduct(1, payload, bindingResult));

        assertEquals(List.of(new FieldError("payload", "title", "error")), exception.getAllErrors());
        verifyNoInteractions(productService);
    }

    @Test
    void updateProduct_RequestIsInvalidAndBindResultIsBindException_ReturnsBadRequest() {
        // given
        var payload = new UpdateProductPayload("   ", null);
        var bindingResult = new BindException(new MapBindingResult(Map.of(), "payload"));
        bindingResult.addError(new FieldError("payload", "title", "error"));

        var exception = assertThrows(BindException.class, () -> controller.updateProduct(1, payload, bindingResult));

        assertEquals(List.of(new FieldError("payload", "title", "error")), exception.getAllErrors());
        verifyNoInteractions(productService);
    }

    @Test
    void deleteProduct_ReturnsNoContent() {
        var result = controller.deleteProduct(1);

        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        verify(productService).deleteProduct(1);
    }

    @Test
    void handleNoSuchElementException_ReturnsNotFound() {
        var exception = new NoSuchElementException("error_code");
        var locale = Locale.of("ru");

        doReturn("error details").when(messageSource)
                .getMessage("error_code", new Object[0], "error_code", Locale.of("ru"));

        var result = controller.handleNoSuchElementException(exception, locale);

        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertInstanceOf(ProblemDetail.class, result.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getBody().getStatus());
        assertEquals("error details", result.getBody().getDetail());

        verifyNoInteractions(productService);
    }
}