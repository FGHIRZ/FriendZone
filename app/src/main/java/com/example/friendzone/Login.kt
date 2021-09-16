package com.example.friendzone

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.widget.Button
import android.widget.EditText

class Login : AppCompatActivity() {

    val requestHandler :RequestHandler = RequestHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val username : EditText = findViewById(R.id.user_id)
        val password : EditText = findViewById(R.id.password)
        val loginButton : Button = findViewById(R.id.login_button)

        requestHandler.initialize(this)

        loginButton.setOnClickListener {
            requestHandler.requestLogin(username.text.toString(), password.text.toString(), this)
        }
    }

    fun returnLogin(user : User)
    {
        val data = Intent()
        data.setData(Uri.parse("ok"))
        setResult(RESULT_OK, data)
        finish()
    }
}