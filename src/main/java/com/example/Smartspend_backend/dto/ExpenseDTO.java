package com.example.Smartspend_backend.dto;

import jakarta.validation.constraints.*;
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
    @NotBlank
    private String title;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    @NotBlank
    private String category;
    @NotNull
    @Pattern(regexp = "EXPENSE|INCOME")
    private String type; // "EXPENSE" or "INCOME"
    @NotNull
    private LocalDate date;
    private String description;

}