package com.example.studentprofile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.studentprofile.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // Bước 1: Khai báo biến binding với lateinit var
    private lateinit var binding: ActivityMainBinding

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

        // --- Demo Bài 2: Thực hành với Scope Functions ---
        // 1. Thực hành với with: Gom nhóm thao tác hiển thị sinh viên
        displayStudent("Ngo VO Quy", 3.8, "ngovoquy2006@gmail.com")

        // 2. Thực hành với let: Xử lý an toàn biến null (thử nghiệm với null để nạp ảnh mặc định)
        processAvatarUri(null)

        // 3. Thực hành với also: Tính điểm hệ 10 và ghi log/toast hành động phụ
        calculateAndAudit(3.8)

        // 4. Thực hành với apply: Khi nhấn nút cập nhật, cấu hình Intent và mở DetailActivity
        binding.btnUpdate.setOnClickListener {
            openDetailActivity("SV2026001")
        }
    }

    // --- Hàm tiện ích hỗ trợ hiển thị Toast ---
    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // --- 1. Kiểm tra Null Safety với Safe Call ?.let ---
    private fun processAvatarUri(avatarUri: Uri?) {
        // Khối lệnh chỉ chạy khi avatarUri KHÁC NULL
        avatarUri?.let { validUri ->
            binding.imgAvatar.setImageURI(validUri)
            binding.tvAvatarStatus.text = "Đã tải ảnh đại diện!"
            toast("Ảnh đã được cập nhật")
        } ?: run {
            // Chạy khi avatarUri == null
            binding.imgAvatar.setImageResource(R.drawable.ic_default_avatar)
        }
    }

    // --- 2. Chèn hành động phụ (Side-Effects) với 'also' ---
    private fun calculateAndAudit(rawScore: Double): Double {
        return (rawScore * 10.0 / 4.0)
            .also { finalScore ->
                Log.d("STUDENT_AUDIT", "Điểm hệ 10 quy đổi: $finalScore")
            }
            .also {
                toast("Đã tính xong điểm: $it")
            }
    }

    // --- 3. Gom nhóm thao tác hiển thị với 'with(binding)' ---
    private fun displayStudent(name: String, gpa: Double, email: String) {
        // Bên trong with(binding), mọi View thuộc binding đều là 'this'
        with(binding) {
            tvName.text = name
            tvGpa.text = "Điểm tích lũy: $gpa"
            tvEmail.text = email
            btnUpdate.isEnabled = true
            progressBar.visibility = View.GONE
        }
    }

    // --- 4. Cấu hình Intent hoặc View mới với 'apply' ---
    private fun openDetailActivity(studentId: String) {
        val detailIntent = Intent(this, DetailActivity::class.java).apply {
            putExtra("KEY_STUDENT_ID", studentId)
            putExtra("KEY_TIMESTAMP", System.currentTimeMillis())
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(detailIntent)
    }
}