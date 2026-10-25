package com.shop.presentation.controller;

import com.shop.application.usecase.catalog.ProductQueryUseCase;
import com.shop.domain.model.ProductSearchCriteria;
import com.shop.domain.model.ProductSort;
import com.shop.presentation.dto.Dtos.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductQueryUseCase products;

    public ProductController(ProductQueryUseCase products) {
        this.products = products;
    }

    @GetMapping("/products")
    public PageResponse<ProductResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long minPriceCents,
            @RequestParam(required = false) Long maxPriceCents,
            @RequestParam(defaultValue = "false") boolean inStock,
            @RequestParam(defaultValue = "NEWEST") ProductSort sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        var criteria = new ProductSearchCriteria(q, category, minPriceCents, maxPriceCents, inStock, false, sort, page, size);
        return PageResponse.from(products.search(criteria), ProductResponse::from);
    }

    @GetMapping("/products/{id}")
    public ProductResponse get(@PathVariable UUID id) {
        return ProductResponse.from(products.getById(id));
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return products.categories();
    }
}
