package com.example.friendzone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class ChangeUsername : AppCompatActivity() {

    private val requestHandler : RequestHandler = RequestHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_username)

        val username : EditText = findViewById(R.id.new_username_edittext)
        val password : EditText = findViewById(R.id.check_password_edittext)
        val button : Button = findViewById(R.id.send_new_username_button)

        button.setOnClickListener {
            requestHandler.requestUsernameChange()
        }
    }
}