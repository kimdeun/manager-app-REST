package org.example.catalogue.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.catalogue.controller.payload.UpdateProductPayload;
import org.example.catalogue.entity.Product;
import org.example.catalogue.service.ProductService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("catalogue-api/products/{productId}")
public class ProductRestController {
    private final ProductService productService;
    private final MessageSource messageSource;

    @ModelAttribute
    public Product getProduct(@PathVariable("productId") int productId) {
        return productService.findProduct(productId)
                .orElseThrow(() -> new NoSuchElementException("catalogue.errors.product.not_found"));
    }

    @GetMapping
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Product.class))),
                    @ApiResponse(responseCode = "403", content = @Content()),
                    @ApiResponse(responseCode = "404", content = @Content())
            }
    )
    public Product findProduct(@Parameter(hidden = true) @ModelAttribute("product") Product product) {
        return product;
    }

    @PatchMapping
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "204", content = @Content()),
                    @ApiResponse(responseCode = "400", content = @Content(
                        examples = @ExampleObject("""
                                {
                                    "errors": ["Название товара должно быть от 3 до 50 символов"]
                                }"""))),
                    @ApiResponse(responseCode = "403", content = @Content()),
                    @ApiResponse(responseCode = "404", content = @Content())
            }
    )
    public ResponseEntity<?> updateProduct(@PathVariable("productId") int productId,
                                           @Valid @RequestBody UpdateProductPayload payload,
                                           BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            productService.updateProduct(productId, payload.title(), payload.details());
            return ResponseEntity.noContent().build();
        }
    }

    @DeleteMapping
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "204", content = @Content()),
                    @ApiResponse(responseCode = "403", content = @Content()),
                    @ApiResponse(responseCode = "404", content = @Content())
            }
    )
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") int productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNoSuchElementException(NoSuchElementException exception,
                                                                      Locale locale) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                        messageSource.getMessage(exception.getMessage(), new Object[0],
                                exception.getMessage(), locale)));
    }
}
