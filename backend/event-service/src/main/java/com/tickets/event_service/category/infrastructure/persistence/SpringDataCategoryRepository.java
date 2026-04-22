package com.tickets.event_service.category.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataCategoryRepository extends JpaRepository<CategoryJpaEntity, Long> {

    boolean existsByNameIgnoreCase(String name);
}
