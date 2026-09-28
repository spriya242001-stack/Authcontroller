package com.example.Smartspend_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseDTO {

    private Long id;
    private String title;
    private BigDecimal amount;
    private String category;
    private String type; // "EXPENSE" or "INCOME"
    private LocalDate date;
    private String description;

    // Getters and Setters for all fields...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}