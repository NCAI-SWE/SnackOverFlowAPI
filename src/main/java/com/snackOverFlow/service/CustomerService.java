package com.snackOverFlow.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.snackOverFlow.dto.request.RegisterCustomerRequest;
import com.snackOverFlow.dto.response.CustomerResponse;
import com.snackOverFlow.exception.BusinessException;
import com.snackOverFlow.mapper.CustomerMapper;
import com.snackOverFlow.repository.CustomerRepository;

@Service
@Slf4j
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerMapper customerMapper;

    @Transactional
    public CustomerResponse register(RegisterCustomerRequest request) {
        customerRepository.findByEmail(request.getEmail()).ifPresent(c -> {
            throw new BusinessException("Customer with email '" + request.getEmail() + "' already exists");
        });

        var customer = customerMapper.toEntity(request);
        var saved = customerRepository.save(customer);
        log.info("Registered customer: id={} email={}", saved.getId(), saved.getEmail());
        return customerMapper.toResponse(saved);
    }
}
