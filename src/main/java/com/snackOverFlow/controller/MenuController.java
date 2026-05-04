package com.snackOverFlow.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.snackOverFlow.dto.request.CreateMenuItemRequest;
import com.snackOverFlow.dto.request.UpdateMenuItemRequest;
import com.snackOverFlow.dto.response.ApiResponse;
import com.snackOverFlow.dto.response.MenuItemResponse;
import com.snackOverFlow.enums.MenuCategory;
import com.snackOverFlow.service.MenuService;

@RestController
@RequestMapping("/v1/menu")
@Slf4j
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MenuItemResponse>>> listMenuItems(
            @RequestParam(required = false) MenuCategory category,
            @RequestParam(required = false) Boolean available,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(menuService.listMenuItems(category, available, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> getMenuItem(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(menuService.getMenuItemById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MenuItemResponse>> createMenuItem(
            @Valid @RequestBody CreateMenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(menuService.createMenuItem(request), "Menu item created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMenuItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(menuService.updateMenuItem(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long id) {
        menuService.deleteMenuItem(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Menu item deleted successfully"));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<MenuItemResponse>> toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(menuService.toggleAvailability(id)));
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MenuItemResponse>> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(menuService.uploadImage(id, file)));
    }
}

