package com.snackOverFlow.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.snackOverFlow.config.CacheConfig;
import com.snackOverFlow.dto.request.CreateMenuItemRequest;
import com.snackOverFlow.dto.request.UpdateMenuItemRequest;
import com.snackOverFlow.dto.response.MenuItemResponse;
import com.snackOverFlow.entity.MenuItem;
import com.snackOverFlow.enums.MenuCategory;
import com.snackOverFlow.exception.ResourceNotFoundException;
import com.snackOverFlow.mapper.MenuItemMapper;
import com.snackOverFlow.repository.MenuItemRepository;

@Service
@Slf4j
public class MenuService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private MenuItemMapper menuItemMapper;

    @Autowired
    private MinioStorageService minioStorageService;

    @Cacheable(value = CacheConfig.MENU_ITEMS_CACHE, key = "#category + '_' + #available + '_' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<MenuItemResponse> listMenuItems(MenuCategory category, Boolean available, Pageable pageable) {
        Page<MenuItem> items;

        if (category != null && available != null) {
            items = menuItemRepository.findAllByIsDeletedFalseAndCategoryAndIsAvailable(category, available, pageable);
        } else if (category != null) {
            items = menuItemRepository.findAllByIsDeletedFalseAndCategory(category, pageable);
        } else if (available != null) {
            items = menuItemRepository.findAllByIsDeletedFalseAndIsAvailable(available, pageable);
        } else {
            items = menuItemRepository.findAllByIsDeletedFalse(pageable);
        }

        return items.map(menuItemMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItemById(Long id) {
        MenuItem item = menuItemRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
        return menuItemMapper.toResponse(item);
    }

    @CacheEvict(value = CacheConfig.MENU_ITEMS_CACHE, allEntries = true)
    @Transactional
    public MenuItemResponse createMenuItem(CreateMenuItemRequest request) {
        MenuItem item = menuItemMapper.toEntity(request);
        MenuItem saved = menuItemRepository.save(item);
        log.info("Created menu item: id={} name={}", saved.getId(), saved.getName());
        return menuItemMapper.toResponse(saved);
    }

    @CacheEvict(value = CacheConfig.MENU_ITEMS_CACHE, allEntries = true)
    @Transactional
    public MenuItemResponse updateMenuItem(Long id, UpdateMenuItemRequest request) {
        MenuItem item = menuItemRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
        menuItemMapper.updateEntity(request, item);
        return menuItemMapper.toResponse(menuItemRepository.save(item));
    }

    @CacheEvict(value = CacheConfig.MENU_ITEMS_CACHE, allEntries = true)
    @Transactional
    public void deleteMenuItem(Long id) {
        MenuItem item = menuItemRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
        item.setIsDeleted(true);
        menuItemRepository.save(item);
        log.info("Soft-deleted menu item: id={}", id);
    }

    @CacheEvict(value = CacheConfig.MENU_ITEMS_CACHE, allEntries = true)
    @Transactional
    public MenuItemResponse toggleAvailability(Long id) {
        MenuItem item = menuItemRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
        item.setIsAvailable(!item.getIsAvailable());
        return menuItemMapper.toResponse(menuItemRepository.save(item));
    }

    @CacheEvict(value = CacheConfig.MENU_ITEMS_CACHE, allEntries = true)
    @Transactional
    public MenuItemResponse uploadImage(Long id, MultipartFile file) {
        MenuItem item = menuItemRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
        String imageUrl = minioStorageService.uploadMenuImage(id.toString(), file);
        item.setImageUrl(imageUrl);
        return menuItemMapper.toResponse(menuItemRepository.save(item));
    }
}
