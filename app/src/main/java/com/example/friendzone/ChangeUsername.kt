package com.example.friendzone

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class ChangeUsername : AppCompatActivity() {

    private val requestHandler : RequestHandler = RequestHandler()

    private var PRIVATE_MODE = 0
    private val PREF_NAME = "friendzone-app"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_username)

        requestHandler.initialize(this)
        val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        val user_id = sharedPref.getInt("USED_ID", 0)

        val username : EditText = findViewById(R.id.new_username_edittext)
        val password : EditText = findViewById(R.id.check_password_edittext)
        val button : Button = findViewById(R.id.send_new_username_button)

        button.setOnClickListener {
            requestHandler.requestUsernameChange(user_id,username.text.toString(),requestHandler.md5(password.text.toString()),this)
        }

    }
}