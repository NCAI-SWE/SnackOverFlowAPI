package com.snackOverFlow.dto.messaging;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessageMetadata {
    private Integer preparationTime;
    private BigDecimal amount;
    private Integer itemCount;
}


