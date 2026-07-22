package com.schoolerp.controller;

import com.schoolerp.entity.FeePayment;
import com.schoolerp.entity.FeeStructure;
import com.schoolerp.service.FeeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fees")
public class FeeController {

    private final FeeService feeService;

    public FeeController(FeeService feeService) {
        this.feeService = feeService;
    }

    // ---- Fee structures ----
    @GetMapping("/structures")
    public List<FeeStructure> getStructures() {
        return feeService.findAllStructures();
    }

    @PostMapping("/structures")
    public FeeStructure createStructure(@Valid @RequestBody FeeStructure structure) {
        return feeService.createStructure(structure);
    }

    @DeleteMapping("/structures/{id}")
    public void deleteStructure(@PathVariable Long id) {
        feeService.deleteStructure(id);
    }

    // ---- Payments ----
    @GetMapping("/payments")
    public List<FeePayment> getAllPayments() {
        return feeService.findAllPayments();
    }

    @GetMapping("/payments/student/{studentId}")
    public List<FeePayment> getPaymentsByStudent(@PathVariable Long studentId) {
        return feeService.findPaymentsByStudent(studentId);
    }

    @PostMapping("/payments")
    public FeePayment recordPayment(@Valid @RequestBody FeePayment payment) {
        return feeService.recordPayment(payment);
    }

    @DeleteMapping("/payments/{id}")
    public void deletePayment(@PathVariable Long id) {
        feeService.deletePayment(id);
    }

    // ---- Summary ----
    @GetMapping("/summary/student/{studentId}")
    public Map<String, Object> getStudentSummary(@PathVariable Long studentId) {
        return feeService.getStudentFeeSummary(studentId);
    }
}
