package com.schoolerp.service;

import com.schoolerp.entity.FeePayment;
import com.schoolerp.entity.FeeStructure;
import com.schoolerp.entity.Student;
import com.schoolerp.exception.ResourceNotFoundException;
import com.schoolerp.repository.FeePaymentRepository;
import com.schoolerp.repository.FeeStructureRepository;
import com.schoolerp.repository.SchoolClassRepository;
import com.schoolerp.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeeService {

    private final FeeStructureRepository feeStructureRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final StudentRepository studentRepository;
    private final SchoolClassRepository schoolClassRepository;

    public FeeService(FeeStructureRepository feeStructureRepository, FeePaymentRepository feePaymentRepository,
                       StudentRepository studentRepository, SchoolClassRepository schoolClassRepository) {
        this.feeStructureRepository = feeStructureRepository;
        this.feePaymentRepository = feePaymentRepository;
        this.studentRepository = studentRepository;
        this.schoolClassRepository = schoolClassRepository;
    }

    // ---- Fee Structures ----

    public List<FeeStructure> findAllStructures() {
        return feeStructureRepository.findAll();
    }

    public FeeStructure createStructure(FeeStructure structure) {
        if (structure.getSchoolClass() != null && structure.getSchoolClass().getId() != null) {
            structure.setSchoolClass(schoolClassRepository.findById(structure.getSchoolClass().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found")));
        }
        return feeStructureRepository.save(structure);
    }

    public void deleteStructure(Long id) {
        if (!feeStructureRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fee structure not found with id: " + id);
        }
        feeStructureRepository.deleteById(id);
    }

    // ---- Fee Payments ----

    public List<FeePayment> findAllPayments() {
        return feePaymentRepository.findAll();
    }

    public List<FeePayment> findPaymentsByStudent(Long studentId) {
        return feePaymentRepository.findByStudentId(studentId);
    }

    public FeePayment recordPayment(FeePayment payment) {
        Student student = studentRepository.findById(payment.getStudent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        payment.setStudent(student);

        if (payment.getFeeStructure() != null && payment.getFeeStructure().getId() != null) {
            FeeStructure structure = feeStructureRepository.findById(payment.getFeeStructure().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found"));
            payment.setFeeStructure(structure);
        }

        return feePaymentRepository.save(payment);
    }

    public void deletePayment(Long id) {
        if (!feePaymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payment not found with id: " + id);
        }
        feePaymentRepository.deleteById(id);
    }

    // ---- Summary ----

    public Map<String, Object> getStudentFeeSummary(Long studentId) {
        List<FeePayment> payments = feePaymentRepository.findByStudentId(studentId);
        double totalPaid = payments.stream().mapToDouble(FeePayment::getAmountPaid).sum();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        double totalDue = 0.0;
        if (student.getSchoolClass() != null) {
            totalDue = feeStructureRepository.findBySchoolClassId(student.getSchoolClass().getId())
                    .stream().mapToDouble(FeeStructure::getAmount).sum();
        }

        return Map.of(
                "studentId", studentId,
                "totalDue", totalDue,
                "totalPaid", totalPaid,
                "balance", totalDue - totalPaid,
                "payments", payments
        );
    }
}
