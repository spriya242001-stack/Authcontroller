package com.example.Smartspend_backend.service;

import com.example.Smartspend_backend.model.Expense;
import com.example.Smartspend_backend.model.User;
import com.example.Smartspend_backend.repository.ExpenseRepository;
import com.example.Smartspend_backend.repository.UserRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    public ByteArrayInputStream generatePdfReport(Long userId, String userEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Security check: Only allow users to access their own reports
        if (!user.getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized access to user reports");
        }
        
        List<Expense> expenses = expenseRepository.findByUser(user);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("SmartSpend - Expense Report").setBold().setFontSize(18));
            document.add(new Paragraph("User: " + user.getEmail()));
            document.add(new Paragraph("\n"));

            // Extracted helper method for table creation
            Table table = createExpenseTable(expenses);
            document.add(table);

            document.close();
        } catch (Exception e) {
            logger.error("Error generating PDF report for user id: {}", userId, e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private Table createExpenseTable(List<Expense> expenses) {
        Table table = new Table(5);
        table.addCell("Title");
        table.addCell("Amount");
        table.addCell("Category");
        table.addCell("Type");
        table.addCell("Date");

        for (Expense expense : expenses) {
            table.addCell(expense.getTitle() != null ? expense.getTitle() : "N/A");
            table.addCell(expense.getAmount() != null ? expense.getAmount().toString() : "0");
            table.addCell(expense.getCategory() != null ? expense.getCategory() : "N/A");
            table.addCell(expense.getType() != null ? expense.getType() : "N/A");
            table.addCell(expense.getDate() != null ? expense.getDate().toString() : "N/A");
        }
        return table;
    }

    public ByteArrayInputStream generateExcelReport(Long userId, String userEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Security check: Only allow users to access their own reports
        if (!user.getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized access to user reports");
        }
        
        List<Expense> expenses = expenseRepository.findByUser(user);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Expenses");

            Row headerRow = sheet.createRow(0);
            String[] columns = {"Title", "Amount", "Category", "Type", "Date", "Description"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (Expense expense : expenses) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(expense.getTitle() != null ? expense.getTitle() : "");
                row.createCell(1).setCellValue(expense.getAmount() != null ? expense.getAmount().doubleValue() : 0.0);
                row.createCell(2).setCellValue(expense.getCategory() != null ? expense.getCategory() : "");
                row.createCell(3).setCellValue(expense.getType() != null ? expense.getType() : "");
                row.createCell(4).setCellValue(expense.getDate() != null ? expense.getDate().toString() : "");
                row.createCell(5).setCellValue(expense.getDescription() != null ? expense.getDescription() : "");
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            logger.error("Error generating Excel report for user id: {}", userId, e);
            throw new RuntimeException("Failed to export Excel report", e);
        }
    }
}