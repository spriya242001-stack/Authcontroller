package com.example.Smartspend_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDTO {

    private Long id;
    private String category;
    private BigDecimal amount;
    private int month;
    private int year;

}