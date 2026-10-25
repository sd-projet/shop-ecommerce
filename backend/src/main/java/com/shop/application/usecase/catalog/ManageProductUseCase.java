package com.shop.application.usecase.catalog;

import com.shop.domain.exception.ResourceNotFoundException;
import com.shop.domain.model.Product;
import com.shop.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Back-office product management (admin only, enforced at the presentation/security layer). */
@Service
@Transactional
public class ManageProductUseCase {

    public record ProductData(String name, String description, long priceCents, String imageUrl, String category, int stock) {}

    private final ProductRepository products;

    public ManageProductUseCase(ProductRepository products) {
        this.products = products;
    }

    public Product create(ProductData data) {
        return products.save(Product.create(data.name(), data.description(), data.priceCents(), data.imageUrl(), data.category(), data.stock()));
    }

    public Product update(UUID id, ProductData data) {
        Product product = load(id);
        product.update(data.name(), data.description(), data.priceCents(), data.imageUrl(), data.category(), data.stock());
        return products.save(product);
    }

    public Product updateStock(UUID id, int stock) {
        Product product = load(id);
        product.updateStock(stock);
        return products.save(product);
    }

    public void archive(UUID id) {
        Product product = load(id);
        product.archive();
        products.save(product);
    }

    private Product load(UUID id) {
        return products.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }
}
