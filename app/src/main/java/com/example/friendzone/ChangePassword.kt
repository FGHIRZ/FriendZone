package com.example.friendzone

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class ChangePassword : AppCompatActivity() {

    private val requestHandler : RequestHandler = RequestHandler()

    private var PRIVATE_MODE = 0
    private val PREF_NAME = "friendzone-app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        //get user_id from preferences
        val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        val user_id = sharedPref.getInt("user_id", 0)

        val new_password : EditText = findViewById(R.id.new_password_edittext)
        val new_password_repeat : EditText = findViewById(R.id.repeat_new_password_edittext)
        val password : EditText = findViewById(R.id.old_password_edittext)
        val button : Button = findViewById(R.id.send_new_password_button)

        button.setOnClickListener {

            if(new_password.text.toString() == new_password_repeat.text.toString())
            {
                requestHandler.requestPasswordChange(user_id,requestHandler.md5(password.text.toString()),requestHandler.md5(new_password.text.toString()),this)
            }
            else
            {
                Toast.makeText(this, "Les mot de passe ne match pas", Toast.LENGTH_SHORT).show()
            }


        }
    }
}