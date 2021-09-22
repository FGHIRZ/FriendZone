package com.example.friendzone

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class ChangePassword : AppCompatActivity() {

    private val requestHandler : RequestHandler = RequestHandler()

    private var PRIVATEMODE = 0
    private val PREFNAME = "friendzone-app"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        requestHandler.initialize(this)
        val sharedPref: SharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
        val userId = sharedPref.getInt("USER_ID", 0)
        val username = sharedPref.getString("USER_USERNAME", "bug")

        val newPassword : EditText = findViewById(R.id.new_password_edittext)
        val newPasswordRepeat : EditText = findViewById(R.id.repeat_new_password_edittext)
        val password : EditText = findViewById(R.id.old_password_edittext)
        val button : Button = findViewById(R.id.send_new_password_button)

        button.setOnClickListener {
            if(newPassword.text.toString() == newPasswordRepeat.text.toString()) {
                requestHandler.requestPasswordChange(userId,username!!,requestHandler.md5(password.text.toString()),requestHandler.md5(newPassword.text.toString()),this)
            }else{
                Toast.makeText(this, "Passwords aren't equal!", Toast.LENGTH_SHORT).show()
            }

        }
    }
    fun success()
    {
        Toast.makeText(this, "Password has been changed successfully", Toast.LENGTH_LONG).show()
        finish()
    }
}