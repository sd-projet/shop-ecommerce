package com.shop.infrastructure.persistence.jpa;

import com.shop.infrastructure.persistence.entity.ProductEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID>, JpaSpecificationExecutor<ProductEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductEntity p where p.id in :ids order by p.id")
    List<ProductEntity> findAllByIdForUpdate(Collection<UUID> ids);

    @Query("select distinct p.category from ProductEntity p where p.archived = false and p.category is not null order by p.category")
    List<String> findDistinctCategories();

    long countByArchivedFalse();

    long countByArchivedFalseAndStockLessThanEqual(int threshold);
}
