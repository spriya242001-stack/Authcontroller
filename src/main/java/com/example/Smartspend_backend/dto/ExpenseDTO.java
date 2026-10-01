package com.example.Smartspend_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDTO {

    private Long id;
    private String title;
    private BigDecimal amount;
    private String category;
    private String type; // "EXPENSE" or "INCOME"
    private LocalDate date;
    private String description;

}