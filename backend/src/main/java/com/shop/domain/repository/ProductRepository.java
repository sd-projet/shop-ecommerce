package com.shop.domain.repository;

import com.shop.domain.model.PageResult;
import com.shop.domain.model.Product;
import com.shop.domain.model.ProductSearchCriteria;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID id);
    /** Loads the products with a write lock, ordered by id (prevents overselling and deadlocks). */
    List<Product> findAllByIdForUpdate(Collection<UUID> ids);
    PageResult<Product> search(ProductSearchCriteria criteria);
    List<String> findCategories();
    long countActive();
    long countLowStock(int threshold);
}
