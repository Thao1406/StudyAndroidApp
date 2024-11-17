package com.example.myapplication.controller

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.model.DatabaseAccount

class UserinforActivity : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtBirthday: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtAddress: EditText
    private lateinit var btnSave: Button
    private lateinit var database: DatabaseAccount

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_userinfor)

        // Khởi tạo các view
        edtName = findViewById(R.id.edtName)
        edtBirthday = findViewById(R.id.edtBirthday)
        edtEmail = findViewById(R.id.edtEmail)
        edtPhone = findViewById(R.id.edtPhone)
        edtAddress = findViewById(R.id.edtAddress)
        btnSave = findViewById(R.id.btnSave)

        // Khởi tạo database
        database = DatabaseAccount(this)

        // Lấy username từ Intent hoặc SharedPreferences
        val username = intent.getStringExtra("username") ?: database.getLoggedInUser()
        if (username == null) {
            Toast.makeText(this, "Không tìm thấy tài khoản đang đăng nhập!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Lấy thông tin người dùng từ database
        val userInfo = database.getUserInfo(username)

        if (userInfo != null) {
            // Hiển thị thông tin người dùng trong EditText
            edtName.setText(userInfo.name)
            edtBirthday.setText(userInfo.birthday)
            edtEmail.setText(userInfo.email)
            edtPhone.setText(userInfo.phone)
            edtAddress.setText(userInfo.address)
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show()
        }

        // Xử lý khi nhấn nút Lưu
        btnSave.setOnClickListener {
            val name = edtName.text.toString().trim()
            val birthday = edtBirthday.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val phone = edtPhone.text.toString().trim()
            val address = edtAddress.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Họ tên, email và số điện thoại không được để trống!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isSuccess = database.updateUserInfo(
                username = username,
                name = name,
                birthday = birthday,
                email = email,
                phone = phone,
                address = address
            )

            if (isSuccess) {
                Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật thông tin!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
