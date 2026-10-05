package com.example.Smartspend_backend.dto;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.RoundingMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDTO {

    private Long id;
    @NotBlank
    private String category;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    @NotNull(message = "Budget month is required")
    @Min(value = 1, message = "Budget month must be between 1 and 12")
    @Max(value = 12, message = "Budget month must be between 1 and 12")
    private Integer month;
    @NotNull(message = "Budget year is required")
    @Min(value = 1, message = "Budget year must be between 1 and 9999")
    @Max(value = 9999, message = "Budget year must be between 1 and 9999")
    private Integer year;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal spentAmount = BigDecimal.ZERO;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public BigDecimal getLimitAmount() {
        return amount;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public BigDecimal getRemainingAmount() {
        return amount == null ? BigDecimal.ZERO : amount.subtract(spentAmount);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public BigDecimal getPercentage() {
        return amount == null || amount.signum() <= 0 ? BigDecimal.ZERO
                : spentAmount.multiply(BigDecimal.valueOf(100)).divide(amount, 2, RoundingMode.HALF_UP);
    }

}