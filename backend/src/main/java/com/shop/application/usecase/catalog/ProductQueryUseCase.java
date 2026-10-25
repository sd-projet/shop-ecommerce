package com.shop.application.usecase.catalog;

import com.shop.domain.exception.ResourceNotFoundException;
import com.shop.domain.model.PageResult;
import com.shop.domain.model.Product;
import com.shop.domain.model.ProductSearchCriteria;
import com.shop.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProductQueryUseCase {

    private final ProductRepository products;

    public ProductQueryUseCase(ProductRepository products) {
        this.products = products;
    }

    public PageResult<Product> search(ProductSearchCriteria criteria) {
        return products.search(criteria);
    }

    public Product getById(UUID id) {
        return products.findById(id)
                .filter(p -> !p.isArchived())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    public List<String> categories() {
        return products.findCategories();
    }
}
