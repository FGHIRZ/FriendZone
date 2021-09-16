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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val data = Intent()
        val username : EditText = findViewById(R.id.user_id)
        val loginButton : Button = findViewById(R.id.login_button)
        loginButton.setOnClickListener {
            data.setData(Uri.parse(username.text.toString()))
            setResult(RESULT_OK, data)
            Log.d("loginA", data.toString())
            finish()
        }
    }
}