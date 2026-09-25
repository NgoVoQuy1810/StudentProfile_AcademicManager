package com.example.studentprofile

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.example.studentprofile.databinding.ActivityMainBinding
import com.example.studentprofile.model.Student
import com.example.studentprofile.utils.gone
import com.example.studentprofile.utils.show
import com.example.studentprofile.utils.toAcademicRanking
import com.example.studentprofile.utils.toast
import com.example.studentprofile.utils.trimmedText

class MainActivity : AppCompatActivity() {
    companion object {
        private const val KEY_STUDENT_DATA = "EXTRA_KEY_STUDENT"
    }

    // Bước 1: Khai báo biến binding với lateinit var
    private lateinit var binding: ActivityMainBinding

    // Bài 5: Đối tượng sinh viên hiện tại - Giữ nguyên thông tin đã nhập
    private var currentStudent = Student(
        id = "SV2026001",
        name = "Ngo VO Quy",
        className = "24T2",
        email = "ngovoquy2006@gmail.com",
        gpa = 3.8
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Bước 2: Nạp layout XML thông qua hàm inflate()
        binding = ActivityMainBinding.inflate(layoutInflater)

        // Bước 3: Truyền root view vào setContentView
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Bước 4: Thao tác View trực tiếp, không lo Null!
        binding.tvWelcome.text = "Chào mừng bạn đến với ViewBinding!"

        // --- Bài 6: Khôi phục dữ liệu từ savedInstanceState khi xoay màn hình ---
        @Suppress("DEPRECATION")
        val restoredStudent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState?.getSerializable(KEY_STUDENT_DATA, Student::class.java)
        } else {
            savedInstanceState?.getSerializable(KEY_STUDENT_DATA) as? Student
        }
        restoredStudent?.let {
            currentStudent = it
        }

        // Bài 5: Nạp dữ liệu sinh viên lên giao diện (đã khôi phục hoặc mặc định ban đầu)
        bindStudentData(currentStudent)

        // Bài 2: Nạp ảnh đại diện an toàn
        processAvatarUri(null)

        // Bài 4: Lắng nghe sự kiện người dùng gõ từng ký tự vào ô nhập điểm
        binding.edtGpaInput.doOnTextChanged { text, start, before, count ->
            val input = text?.toString()?.trim() ?: ""

            if (input.isNotEmpty()) {
                // 1. Tự động xóa thông báo lỗi đỏ cũ khi người dùng bắt đầu sửa
                binding.edtGpaInput.error = null

                // 2. Xem trước xếp loại học lực tương ứng thời gian thực
                val tempScore = input.toDoubleOrNull()
                if (tempScore != null && tempScore in 0.0..4.0) {
                    binding.tvPreviewRanking.text = "Dự kiến: ${tempScore.toAcademicRanking()}"
                    binding.tvPreviewRanking.show() // Dùng extension
                } else {
                    binding.tvPreviewRanking.gone()
                }
            } else {
                binding.tvPreviewRanking.gone()
            }
        }

        // Bài 4: Validate điểm GPA với setOnClickListener
        binding.btnUpdateGpa.setOnClickListener {
            val rawInput = binding.edtGpaInput.trimmedText()

            // 1. Chuyển đổi an toàn: trả về null nếu chuỗi là chữ hoặc rỗng
            val newGpa = rawInput.toDoubleOrNull()

            // 2. Kiểm tra điều kiện hợp lệ (từ 0.0 đến 4.0)
            if (newGpa == null || newGpa !in 0.0..4.0) {
                // Hiển thị icon cảnh báo và thông điệp lỗi ngay trên EditText
                binding.edtGpaInput.error = "Vui lòng nhập GPA hợp lệ (0.0 - 4.0)"
                binding.edtGpaInput.requestFocus()
                toast("Điểm số không hợp lệ, vui lòng kiểm tra lại!")
                return@setOnClickListener // Dừng thực thi
            }

            // 3. Nếu dữ liệu hợp lệ: Xóa thông báo lỗi và cập nhật
            binding.edtGpaInput.error = null
            updateStudentScore(newGpa)
        }

        // Mở màn hình chi tiết khi nhấn nút xem chi tiết
        binding.btnUpdate.setOnClickListener {
            openDetailActivity(currentStudent.id)
        }
    }

    // --- Bài 6: Lưu dữ liệu trước khi Activity bị hủy do xoay màn hình ---
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(KEY_STUDENT_DATA, currentStudent)
    }

    // --- Bài 5: Hàm gán toàn bộ thông tin từ model lên các Views giao diện ---
    private fun bindStudentData(student: Student) {
        with(binding) {
            tvStudentName.text = student.name
            tvStudentDetails.text = "MSSV: ${student.id} • Lớp: ${student.className}"
            tvStudentEmail.text = "Email: ${student.email}"
            tvGpaBadge.text = "${student.gpa} GPA • ${student.gpa.toAcademicRanking()}"
            edtGpaInput.setText(student.gpa.toString())
            progressBar.gone()
        }
    }

    // --- Bài 5: Cập nhật điểm số bất biến với phương thức copy() ---
    private fun updateStudentScore(newGpa: Double) {
        currentStudent = currentStudent.copy(gpa = newGpa)
        bindStudentData(currentStudent)
        calculateAndAudit(newGpa)
        toast("Cập nhật điểm thành công: $newGpa")
    }

    // --- Bài 2: Kiểm tra Null Safety với Safe Call ?.let ---
    private fun processAvatarUri(avatarUri: Uri?) {
        avatarUri?.let { validUri ->
            binding.imgAvatar.setImageURI(validUri)
            binding.tvAvatarStatus.text = "Đã tải ảnh đại diện!"
            toast("Ảnh đã được cập nhật")
        } ?: run {
            binding.imgAvatar.setImageResource(R.drawable.ic_default_avatar)
        }
    }

    // --- Bài 2: Chèn hành động phụ (Side-Effects) với 'also' ---
    private fun calculateAndAudit(rawScore: Double): Double {
        return (rawScore * 10.0 / 4.0)
            .also { finalScore ->
                Log.d("STUDENT_AUDIT", "Điểm hệ 10 quy đổi: $finalScore")
            }
            .also {
                toast("Đã tính xong điểm: $it")
            }
    }

    // --- Bài 2: Cấu hình Intent với 'apply' ---
    private fun openDetailActivity(studentId: String) {
        val detailIntent = Intent(this, DetailActivity::class.java).apply {
            putExtra("KEY_STUDENT_ID", studentId)
            putExtra("KEY_STUDENT", currentStudent)
            putExtra("KEY_TIMESTAMP", System.currentTimeMillis())
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(detailIntent)
    }
}