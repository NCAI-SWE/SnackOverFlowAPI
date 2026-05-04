package com.snackOverFlow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import com.snackOverFlow.entity.MenuItem;
import com.snackOverFlow.enums.MenuCategory;

import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long>, JpaSpecificationExecutor<MenuItem> {

    Page<MenuItem> findAllByIsDeletedFalse(Pageable pageable);

    Page<MenuItem> findAllByIsDeletedFalseAndCategory(MenuCategory category, Pageable pageable);

    Page<MenuItem> findAllByIsDeletedFalseAndIsAvailable(Boolean isAvailable, Pageable pageable);

    Page<MenuItem> findAllByIsDeletedFalseAndCategoryAndIsAvailable(MenuCategory category, Boolean isAvailable, Pageable pageable);

    Optional<MenuItem> findByIdAndIsDeletedFalse(Long id);
}
