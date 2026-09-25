package com.example.studentprofile

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.example.studentprofile.databinding.ActivityMainBinding
import com.example.studentprofile.model.Student
import com.example.studentprofile.utils.gone
import com.example.studentprofile.utils.show
import com.example.studentprofile.utils.toAcademicRanking
import com.example.studentprofile.utils.toRankingColor
import com.example.studentprofile.utils.toast
import com.example.studentprofile.utils.trimmedText

class MainActivity : AppCompatActivity() {
    companion object {
        private const val KEY_STUDENT = "KEY_STUDENT"
    }

    // Bước 1: Khai báo biến binding với lateinit var
    private lateinit var binding: ActivityMainBinding

    // Dữ liệu sinh viên mặc định - Giữ nguyên thông tin sẵn có của bạn
    private val defaultStudent = Student(
        id = "SV2026001",
        name = "Ngo VO Quy",
        className = "24T2",
        email = "ngovoquy2006@gmail.com",
        gpa = 3.8
    )

    // Đối tượng sinh viên hiện tại
    private var currentStudent = defaultStudent

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

        // Khôi phục nếu vừa xoay màn hình (Bài 6)
        @Suppress("DEPRECATION")
        val restoredStudent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState?.getSerializable(KEY_STUDENT, Student::class.java)
        } else {
            savedInstanceState?.getSerializable(KEY_STUDENT) as? Student
        }
        restoredStudent?.let {
            currentStudent = it
        }

        // Gán dữ liệu sinh viên lên Views (kèm đổi màu badge động)
        bindStudentData(currentStudent)

        // Nạp ảnh đại diện an toàn
        processAvatarUri(null)

        // Lắng nghe sự kiện người dùng gõ từng ký tự vào ô nhập điểm (Realtime preview)
        binding.edtGpaInput.doOnTextChanged { text, start, before, count ->
            val input = text?.toString()?.trim() ?: ""

            if (input.isNotEmpty()) {
                binding.edtGpaInput.error = null
                val tempScore = input.toDoubleOrNull()
                if (tempScore != null && tempScore in 0.0..4.0) {
                    binding.tvPreviewRanking.text = "Dự kiến: ${tempScore.toAcademicRanking()}"
                    binding.tvPreviewRanking.setTextColor(tempScore.toRankingColor())
                    binding.tvPreviewRanking.show()
                } else {
                    binding.tvPreviewRanking.gone()
                }
            } else {
                binding.tvPreviewRanking.gone()
            }
        }

        // Bắt sự kiện cập nhật điểm
        binding.btnUpdateGpa.setOnClickListener {
            val rawInput = binding.edtGpaInput.trimmedText()
            val gpa = rawInput.toDoubleOrNull()

            if (gpa == null || gpa !in 0.0..4.0) {
                binding.edtGpaInput.error = "GPA phải từ 0.0 đến 4.0"
                binding.edtGpaInput.requestFocus()
                toast("Điểm số không hợp lệ, vui lòng kiểm tra lại!")
                return@setOnClickListener
            }

            binding.edtGpaInput.error = null
            currentStudent = currentStudent.copy(gpa = gpa)
            bindStudentData(currentStudent)
            calculateAndAudit(gpa)
            toast("Đã cập nhật GPA thành công!")
        }

        // Tự mở rộng 2: Nút Khôi phục kèm Dialog Xác nhận (AlertDialog với apply)
        binding.btnReset.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("Xác nhận khôi phục")
                setMessage("Bạn có chắc chắn muốn đặt lại điểm GPA ban đầu (${defaultStudent.gpa}) không?")
                setNegativeButton("Hủy") { dialog, _ ->
                    dialog.dismiss()
                }
                setPositiveButton("Đồng ý") { _, _ ->
                    currentStudent = defaultStudent
                    bindStudentData(currentStudent)
                    binding.edtGpaInput.error = null
                    binding.tvPreviewRanking.gone()
                    toast("Đã khôi phục dữ liệu ban đầu!")
                }
            }.show()
        }

        // Tự mở rộng 3: Nút Gửi Email Báo cáo Kết quả (Implicit Intent với apply)
        binding.btnSendReport.setOnClickListener {
            sendEmailReport(currentStudent)
        }

        // Xem chi tiết sinh viên (mở DetailActivity)
        binding.btnUpdate.setOnClickListener {
            openDetailActivity(currentStudent.id)
        }
    }

    // Lưu dữ liệu trước khi Activity bị hủy do xoay màn hình
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(KEY_STUDENT, currentStudent)
    }

    // Hàm gán toàn bộ thông tin từ model lên các Views giao diện
    private fun bindStudentData(student: Student) {
        with(binding) {
            tvStudentName.text = student.name
            tvStudentDetails.text = "MSSV: ${student.id} • Lớp: ${student.className}"
            tvStudentEmail.text = "Email: ${student.email}"
            tvGpaBadge.text = "${student.gpa} GPA • ${student.gpa.toAcademicRanking()}"

            // Tự mở rộng 1: Đổi màu chữ của tvGpaBadge động theo ngưỡng học lực
            tvGpaBadge.setTextColor(student.gpa.toRankingColor())

            edtGpaInput.setText(student.gpa.toString())
            progressBar.gone()
        }
    }

    // Tự mở rộng 3: Gửi email báo cáo kết quả học tập qua Implicit Intent
    private fun sendEmailReport(student: Student) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${student.email}")
            putExtra(Intent.EXTRA_SUBJECT, "[Báo cáo học tập] Sinh viên ${student.name} - MSSV ${student.id}")
            putExtra(
                Intent.EXTRA_TEXT,
                """
                Họ và tên: ${student.name}
                MSSV: ${student.id}
                Lớp: ${student.className}
                Điểm GPA: ${student.gpa}
                Xếp loại: ${student.gpa.toAcademicRanking()}
                """.trimIndent()
            )
        }
        try {
            startActivity(Intent.createChooser(emailIntent, "Chọn ứng dụng gửi email"))
        } catch (e: Exception) {
            toast("Không tìm thấy ứng dụng email phù hợp!")
        }
    }

    // Kiểm tra Null Safety với Safe Call ?.let
    private fun processAvatarUri(avatarUri: Uri?) {
        avatarUri?.let { validUri ->
            binding.imgAvatar.setImageURI(validUri)
            binding.tvAvatarStatus.text = "Đã tải ảnh đại diện!"
            toast("Ảnh đã được cập nhật")
        } ?: run {
            binding.imgAvatar.setImageResource(R.drawable.ic_default_avatar)
        }
    }

    // Chèn hành động phụ (Side-Effects) với 'also'
    private fun calculateAndAudit(rawScore: Double): Double {
        return (rawScore * 10.0 / 4.0)
            .also { finalScore ->
                Log.d("STUDENT_AUDIT", "Điểm hệ 10 quy đổi: $finalScore")
            }
            .also {
                toast("Đã tính xong điểm: $it")
            }
    }

    // Cấu hình Intent với 'apply'
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