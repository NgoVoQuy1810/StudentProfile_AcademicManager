package com.example.studentprofile.model

import java.io.Serializable

// --- Data Class quản lý hồ sơ sinh viên ---
data class Student(
    val id: String,         // Mã số sinh viên (VD: 22505120005)
    val name: String,       // Họ và tên
    val className: String,  // Lớp sinh hoạt (VD: 22CT111)
    val email: String,      // Địa chỉ email sinh viên
    val gpa: Double         // Điểm trung bình tích lũy (0.0 - 4.0)
) : Serializable {
    // Thuộc tính tính toán (Computed Property)
    val isHonorStudent: Boolean
        get() = gpa >= 3.6
}
