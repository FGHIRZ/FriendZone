package com.example.friendzone

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class ChangeUsername : AppCompatActivity() {

    private val requestHandler : RequestHandler = RequestHandler()

    private var PRIVATEMODE = 0
    private val PREFNAME = "friendzone-app"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_username)

        requestHandler.initialize(this)
        val sharedPref: SharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
        val userId = sharedPref.getInt("USER_ID", 0)

        val username : EditText = findViewById(R.id.new_username_edittext)
        val password : EditText = findViewById(R.id.check_password_edittext)
        val button : Button = findViewById(R.id.send_new_username_button)

        button.setOnClickListener {
            requestHandler.requestUsernameChange(userId,username.text.toString(),requestHandler.md5(password.text.toString()),this)
        }

    }
    fun success(new_username: String)
    {
        // actualise the pref value of username
        val sharedPref: SharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
        val editor = sharedPref.edit()

        editor.putString("USER_USERNAME", new_username)
        editor.apply()

        Toast.makeText(this, "Username has been changed successfully", Toast.LENGTH_LONG).show()
        finish()
    }
}