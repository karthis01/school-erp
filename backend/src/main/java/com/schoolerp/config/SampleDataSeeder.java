package com.schoolerp.config;

import com.schoolerp.entity.*;
import com.schoolerp.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Seeds 5 demo records per module into WHICHEVER tenant database is currently set in
 * TenantContext. This used to run automatically at app startup via a CommandLineRunner,
 * but that only ever works against a single fixed database - under multi-tenancy there is
 * no "the" database at startup. It's now invoked explicitly and optionally, per school, by
 * SchoolService.createSchool() (see the "seedSampleData" flag on SchoolCreateRequest), with
 * TenantContext already pointed at that new school.
 */
@Service
public class SampleDataSeeder {

    private final StaffRepository staffRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final FeePaymentRepository feePaymentRepository;

    public SampleDataSeeder(StaffRepository staffRepository,
                             SchoolClassRepository schoolClassRepository,
                             StudentRepository studentRepository,
                             AttendanceRepository attendanceRepository,
                             FeeStructureRepository feeStructureRepository,
                             FeePaymentRepository feePaymentRepository) {
        this.staffRepository = staffRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.feeStructureRepository = feeStructureRepository;
        this.feePaymentRepository = feePaymentRepository;
    }

    public void seedSampleData() {
        {
            // Only seed once — skip if classes already exist
            if (schoolClassRepository.count() > 0) {
                return;
            }

            System.out.println("Seeding sample data (5 records per module)...");

            // ---- Staff (5) ----
            Staff s1 = new Staff(null, "EMP-001", "Anita", "Sharma", "Teacher", "Mathematics", "9800000001", "anita.sharma@school-erp.local", LocalDate.of(2020, 6, 1), 42000.0, Staff.StaffStatus.ACTIVE);
            Staff s2 = new Staff(null, "EMP-002", "Rahul", "Verma", "Teacher", "Science", "9800000002", "rahul.verma@school-erp.local", LocalDate.of(2019, 4, 15), 45000.0, Staff.StaffStatus.ACTIVE);
            Staff s3 = new Staff(null, "EMP-003", "Priya", "Nair", "Teacher", "English", "9800000003", "priya.nair@school-erp.local", LocalDate.of(2021, 1, 10), 40000.0, Staff.StaffStatus.ACTIVE);
            Staff s4 = new Staff(null, "EMP-004", "Suresh", "Kumar", "Accountant", "Finance", "9800000004", "suresh.kumar@school-erp.local", LocalDate.of(2018, 8, 20), 38000.0, Staff.StaffStatus.ACTIVE);
            Staff s5 = new Staff(null, "EMP-005", "Meena", "Iyer", "Principal", "Administration", "9800000005", "meena.iyer@school-erp.local", LocalDate.of(2015, 3, 5), 60000.0, Staff.StaffStatus.ACTIVE);
            List<Staff> staffList = staffRepository.saveAll(List.of(s1, s2, s3, s4, s5));

            // ---- Classes (5) ----
            SchoolClass c1 = new SchoolClass(null, "Grade 1", "A", "2026-2027", staffList.get(0), null);
            SchoolClass c2 = new SchoolClass(null, "Grade 2", "A", "2026-2027", staffList.get(1), null);
            SchoolClass c3 = new SchoolClass(null, "Grade 3", "A", "2026-2027", staffList.get(2), null);
            SchoolClass c4 = new SchoolClass(null, "Grade 4", "A", "2026-2027", staffList.get(0), null);
            SchoolClass c5 = new SchoolClass(null, "Grade 5", "A", "2026-2027", staffList.get(1), null);
            List<SchoolClass> classList = schoolClassRepository.saveAll(List.of(c1, c2, c3, c4, c5));

            // ---- Students (5) ----
            Student st1 = seedStudent("ADM-2026-001", "Aarav", "Singh", LocalDate.of(2018, 5, 12), "Male", "Vikram Singh", "9900000001", "aarav.parent@example.com", "12 MG Road, Chennai", classList.get(0));
            Student st2 = seedStudent("ADM-2026-002", "Diya", "Reddy", LocalDate.of(2017, 8, 22), "Female", "Kiran Reddy", "9900000002", "diya.parent@example.com", "45 Anna Salai, Chennai", classList.get(1));
            Student st3 = seedStudent("ADM-2026-003", "Vihaan", "Menon", LocalDate.of(2016, 3, 3), "Male", "Arjun Menon", "9900000003", "vihaan.parent@example.com", "7 Nungambakkam, Chennai", classList.get(2));
            Student st4 = seedStudent("ADM-2026-004", "Ananya", "Iyer", LocalDate.of(2015, 11, 30), "Female", "Ramesh Iyer", "9900000004", "ananya.parent@example.com", "23 T Nagar, Chennai", classList.get(3));
            Student st5 = seedStudent("ADM-2026-005", "Kabir", "Joshi", LocalDate.of(2014, 1, 18), "Male", "Neha Joshi", "9900000005", "kabir.parent@example.com", "89 Adyar, Chennai", classList.get(4));
            List<Student> studentList = studentRepository.saveAll(List.of(st1, st2, st3, st4, st5));

            // ---- Attendance (5, today) ----
            LocalDate today = LocalDate.now();
            Attendance a1 = new Attendance(null, studentList.get(0), today, Attendance.AttendanceStatus.PRESENT, "");
            Attendance a2 = new Attendance(null, studentList.get(1), today, Attendance.AttendanceStatus.PRESENT, "");
            Attendance a3 = new Attendance(null, studentList.get(2), today, Attendance.AttendanceStatus.ABSENT, "Sick leave");
            Attendance a4 = new Attendance(null, studentList.get(3), today, Attendance.AttendanceStatus.LATE, "Bus delay");
            Attendance a5 = new Attendance(null, studentList.get(4), today, Attendance.AttendanceStatus.PRESENT, "");
            attendanceRepository.saveAll(List.of(a1, a2, a3, a4, a5));

            // ---- Fee Structures (5) ----
            FeeStructure f1 = new FeeStructure(null, "Tuition Fee", classList.get(0), 5000.0, "MONTHLY", "2026-2027");
            FeeStructure f2 = new FeeStructure(null, "Transport Fee", classList.get(1), 1500.0, "MONTHLY", "2026-2027");
            FeeStructure f3 = new FeeStructure(null, "Library Fee", null, 500.0, "ANNUAL", "2026-2027");
            FeeStructure f4 = new FeeStructure(null, "Lab Fee", classList.get(2), 2000.0, "QUARTERLY", "2026-2027");
            FeeStructure f5 = new FeeStructure(null, "Admission Fee", null, 10000.0, "ONE_TIME", "2026-2027");
            List<FeeStructure> feeStructureList = feeStructureRepository.saveAll(List.of(f1, f2, f3, f4, f5));

            // ---- Fee Payments (5) ----
            FeePayment p1 = new FeePayment(null, studentList.get(0), feeStructureList.get(0), 5000.0, today, "CASH", "TXN-1001", "", FeePayment.PaymentStatus.PAID);
            FeePayment p2 = new FeePayment(null, studentList.get(1), feeStructureList.get(1), 1500.0, today, "UPI", "TXN-1002", "", FeePayment.PaymentStatus.PAID);
            FeePayment p3 = new FeePayment(null, studentList.get(2), feeStructureList.get(2), 500.0, today, "CARD", "TXN-1003", "", FeePayment.PaymentStatus.PAID);
            FeePayment p4 = new FeePayment(null, studentList.get(3), feeStructureList.get(3), 1000.0, today, "BANK_TRANSFER", "TXN-1004", "Partial payment", FeePayment.PaymentStatus.PARTIAL);
            FeePayment p5 = new FeePayment(null, studentList.get(4), feeStructureList.get(4), 10000.0, today, "CHEQUE", "TXN-1005", "", FeePayment.PaymentStatus.PAID);
            feePaymentRepository.saveAll(List.of(p1, p2, p3, p4, p5));

            System.out.println("Sample data seeded: 5 staff, 5 classes, 5 students, 5 attendance, 5 fee structures, 5 payments.");
        }
    }

    private Student seedStudent(String admissionNumber, String firstName, String lastName, LocalDate dob,
                                 String gender, String guardianName, String guardianPhone, String email,
                                 String address, SchoolClass schoolClass) {
        Student student = new Student();
        student.setAdmissionNumber(admissionNumber);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setDateOfBirth(dob);
        student.setGender(gender);
        student.setGuardianName(guardianName);
        student.setGuardianPhone(guardianPhone);
        student.setEmail(email);
        student.setAddress(address);
        student.setSchoolClass(schoolClass);
        student.setAdmissionDate(LocalDate.of(2026, 6, 1));
        student.setStatus(Student.StudentStatus.ACTIVE);
        return student;
    }
}
