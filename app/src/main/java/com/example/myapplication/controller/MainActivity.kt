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

        // Sử dụng layout XML
        setContentView(R.layout.activity_main)

        databaseHelper = DatabaseAccount(this)
        val usernameEditText = findViewById<EditText>(R.id.editTextText3)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword2)
        val loginButton = findViewById<Button>(R.id.button_lgn)
        val registerButton = findViewById<Button>(R.id.button_rgt)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

//            val intent = Intent(this, HomeActivity::class.java)
//            startActivity(intent)
//            finish()

            if (databaseHelper.checkUser(username, password)) {
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                // Chuyển sang màn hình khác nếu muốn
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu sai!", Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }


    }
}
