package com.shop.infrastructure.persistence.adapter;

import com.shop.domain.model.*;
import com.shop.domain.repository.ProductRepository;
import com.shop.infrastructure.persistence.entity.ProductEntity;
import com.shop.infrastructure.persistence.jpa.ProductJpaRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpa;

    public ProductRepositoryAdapter(ProductJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Product save(Product p) {
        return toDomain(jpa.save(toEntity(p)));
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Product> findAllByIdForUpdate(Collection<UUID> ids) {
        if (ids.isEmpty()) return List.of();
        return jpa.findAllByIdForUpdate(ids).stream().map(this::toDomain).toList();
    }

    @Override
    public PageResult<Product> search(ProductSearchCriteria c) {
        PageRequest pageable = PageRequest.of(Math.max(c.page(), 0), Math.min(Math.max(c.size(), 1), 100), toSort(c.sort()));
        Page<ProductEntity> page = jpa.findAll(toSpecification(c), pageable);
        return new PageResult<>(page.getContent().stream().map(this::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public List<String> findCategories() {
        return jpa.findDistinctCategories();
    }

    @Override
    public long countActive() {
        return jpa.countByArchivedFalse();
    }

    @Override
    public long countLowStock(int threshold) {
        return jpa.countByArchivedFalseAndStockLessThanEqual(threshold);
    }

    private Sort toSort(ProductSort sort) {
        if (sort == null) sort = ProductSort.NEWEST;
        return switch (sort) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "priceCents").and(Sort.by("id"));
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "priceCents").and(Sort.by("id"));
            case POPULARITY -> Sort.by(Sort.Direction.DESC, "salesCount").and(Sort.by("id"));
            case NEWEST -> Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"));
        };
    }

    /** Multi-criteria search: every word of the query must match the name, description or category. */
    private Specification<ProductEntity> toSpecification(ProductSearchCriteria c) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!c.includeArchived()) {
                predicates.add(cb.isFalse(root.get("archived")));
            }
            if (c.query() != null && !c.query().isBlank()) {
                for (String word : c.query().trim().toLowerCase().split("\\s+")) {
                    String like = "%" + word.replace("%", "\\%").replace("_", "\\_") + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("name")), like, '\\'),
                            cb.like(cb.lower(cb.coalesce(root.<String>get("description"), "")), like, '\\'),
                            cb.like(cb.lower(cb.coalesce(root.<String>get("category"), "")), like, '\\')));
                }
            }
            if (c.category() != null && !c.category().isBlank()) {
                predicates.add(cb.equal(root.get("category"), c.category()));
            }
            if (c.minPriceCents() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.<Long>get("priceCents"), c.minPriceCents()));
            }
            if (c.maxPriceCents() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.<Long>get("priceCents"), c.maxPriceCents()));
            }
            if (c.inStockOnly()) {
                predicates.add(cb.greaterThan(root.<Integer>get("stock"), 0));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private ProductEntity toEntity(Product p) {
        return new ProductEntity(p.getId(), p.getName(), p.getDescription(), p.getPriceCents(), p.getImageUrl(),
                p.getCategory(), p.getStock(), p.getSalesCount(), p.isArchived(), p.getCreatedAt());
    }

    private Product toDomain(ProductEntity e) {
        return new Product(e.getId(), e.getName(), e.getDescription(), e.getPriceCents(), e.getImageUrl(),
                e.getCategory(), e.getStock(), e.getSalesCount(), e.isArchived(), e.getCreatedAt());
    }
}
