package com.snackOverFlow.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.snackOverFlow.dto.request.CreateMenuItemRequest;
import com.snackOverFlow.dto.request.UpdateMenuItemRequest;
import com.snackOverFlow.dto.response.MenuItemResponse;
import com.snackOverFlow.entity.MenuItem;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MenuItemMapper {

    MenuItem toEntity(CreateMenuItemRequest request);

    MenuItemResponse toResponse(MenuItem menuItem);

    void updateEntity(UpdateMenuItemRequest request, @MappingTarget MenuItem menuItem);
}

