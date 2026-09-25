package com.example.studentprofile.utils

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.Toast

// --- 1. Ẩn / Hiện View dễ đọc ---
fun View.show() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

// --- 2. Hiển thị thông báo Toast nhanh gọn ---
fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

// --- 3. Lấy chuỗi từ EditText đã cắt khoảng trắng thừa ---
fun EditText.trimmedText(): String = text.toString().trim()

// --- 4. Quy đổi điểm GPA (hệ 4.0) sang Xếp loại học lực ---
fun Double.toAcademicRanking(): String = when {
    this >= 3.6 -> "Xuất sắc (Excellent)"
    this >= 3.2 -> "Giỏi (Very Good)"
    this >= 2.5 -> "Khá (Good)"
    this >= 2.0 -> "Trung bình (Average)"
    this >= 1.0 -> "Yếu (Weak)"
    else        -> "Kém (Poor)"
}
