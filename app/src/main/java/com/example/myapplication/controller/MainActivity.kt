package com.example.myapplication.controller

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.myapplication.R
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.myapplication.model.DatabaseAccount

class MainActivity : ComponentActivity() {

    private lateinit var databaseHelper: DatabaseAccount

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Khởi tạo databaseHelper
        databaseHelper = DatabaseAccount(this)

        // Kiểm tra trạng thái đăng nhập
        if (databaseHelper.isLoggedIn()) {
            // Người dùng đã đăng nhập, chuyển sang HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("isLoggedIn", true) // Truyền cờ vào Intent
            startActivity(intent)
            finish()
            return
        }

        // Xử lý logic đăng nhập như trước
        val usernameEditText = findViewById<EditText>(R.id.editTextText3)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword2)
        val loginButton = findViewById<Button>(R.id.button_lgn)
        val registerButton = findViewById<Button>(R.id.button_rgt)
        val checkdbButton = findViewById<Button>(R.id.database)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (databaseHelper.checkUser(username, password)) {
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

                // Lưu trạng thái đăng nhập
                databaseHelper.saveLoginSession(username)

                // Chuyển sang HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("isLoggedIn", true) // Truyền cờ vào Intent
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        checkdbButton.setOnClickListener {
            val intent = Intent(this, Checkdb::class.java)
            startActivity(intent)
        }
    }
}
