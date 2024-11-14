// RegisterActivity.kt
package com.example.myapplication.controller

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.myapplication.R
import com.example.myapplication.model.DatabaseAccount

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val usernameEditText = findViewById<EditText>(R.id.editTextText3)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirm_password)
        val registerButton = findViewById<Button>(R.id.button_rgt)

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dbHelper = DatabaseAccount(this)
            val isAdded = dbHelper.addUser(username, password)

            if (isAdded) {
                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
