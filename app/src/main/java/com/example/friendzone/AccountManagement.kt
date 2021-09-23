package com.example.friendzone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class AccountManagement : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_management)

        val passwordButton : Button = findViewById(R.id.change_password_button)
        val usernameButton : Button = findViewById(R.id.change_username_button)
        val deleteButton : Button = findViewById(R.id.delete_account_button)
        val returnButton : Button = findViewById(R.id.am_return_button)

        passwordButton.setOnClickListener {
            val intent = Intent(this, ChangePassword::class.java)
            startActivity(intent)
        }

        usernameButton.setOnClickListener {
            val intent = Intent(this, ChangeUsername::class.java)
            startActivity(intent)
        }

        deleteButton.setOnClickListener {
            val intent = Intent(this, DeleteAccount::class.java)
            startActivity(intent)
        }
        returnButton.setOnClickListener {
            finish()
        }
    }
}