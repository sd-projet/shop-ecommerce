package com.shop.presentation.controller;

import com.shop.application.usecase.catalog.ManageProductUseCase;
import com.shop.application.usecase.catalog.ManageProductUseCase.ProductData;
import com.shop.application.usecase.catalog.ProductQueryUseCase;
import com.shop.domain.model.ProductSearchCriteria;
import com.shop.domain.model.ProductSort;
import com.shop.presentation.dto.Dtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Back-office endpoints, reserved to ROLE_ADMIN by the security configuration (/api/admin/**). */
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ManageProductUseCase manage;
    private final ProductQueryUseCase query;

    public AdminProductController(ManageProductUseCase manage, ProductQueryUseCase query) {
        this.manage = manage;
        this.query = query;
    }

    @GetMapping
    public PageResponse<ProductResponse> list(@RequestParam(required = false) String q,
                                              @RequestParam(defaultValue = "NEWEST") ProductSort sort,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        var criteria = new ProductSearchCriteria(q, null, null, null, false, true, sort, page, size);
        return PageResponse.from(query.search(criteria), ProductResponse::from);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest r) {
        return ProductResponse.from(manage.create(toData(r)));
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable UUID id, @Valid @RequestBody ProductRequest r) {
        return ProductResponse.from(manage.update(id, toData(r)));
    }

    @PatchMapping("/{id}/stock")
    public ProductResponse updateStock(@PathVariable UUID id, @Valid @RequestBody StockRequest r) {
        return ProductResponse.from(manage.updateStock(id, r.stock()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(@PathVariable UUID id) {
        manage.archive(id);
    }

    private ProductData toData(ProductRequest r) {
        return new ProductData(r.name(), r.description(), r.priceCents(), r.imageUrl(), r.category(), r.stock());
    }
}
