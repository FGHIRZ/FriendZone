package com.example.friendzone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class ChangePassword : AppCompatActivity() {

    private val requestHandler : RequestHandler = RequestHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        requestHandler.initialize(this)
        val new_password : EditText = findViewById(R.id.new_password_edittext)
        val new_password_repeat : EditText = findViewById(R.id.repeat_new_password_edittext)
        val password : EditText = findViewById(R.id.old_password_edittext)
        val button : Button = findViewById(R.id.send_new_password_button)

        button.setOnClickListener {

            //requestHandler.requestPasswordChange()
        }
    }
}