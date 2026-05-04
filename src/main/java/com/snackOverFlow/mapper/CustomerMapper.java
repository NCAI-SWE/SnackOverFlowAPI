package com.snackOverFlow.mapper;

import org.mapstruct.Mapper;
import com.snackOverFlow.dto.request.RegisterCustomerRequest;
import com.snackOverFlow.dto.response.CustomerResponse;
import com.snackOverFlow.entity.Customer;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    Customer toEntity(RegisterCustomerRequest request);

    CustomerResponse toResponse(Customer customer);
}

