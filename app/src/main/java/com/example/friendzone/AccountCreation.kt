package com.example.friendzone

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class AccountCreation : AppCompatActivity() {

    private val requestHandler : RequestHandler = RequestHandler()
    private var PRIVATEMODE = 0
    private val PREFNAME = "friendzone-app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_creation)

        val sharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
        requestHandler.initialize(this, sharedPreferences)
        val createButton : Button = findViewById(R.id.create_button)
        val usernameBox : EditText = findViewById(R.id.usernameSetting)
        val passwordBox : EditText = findViewById(R.id.passwordSetting)
        val passwordCheckBox : EditText = findViewById(R.id.passwordCheck)

        createButton.setOnClickListener {
            if(passwordBox.text.toString() == passwordCheckBox.text.toString())
            {
                requestHandler.requestAccountCreation(usernameBox.text.toString(),passwordBox.text.toString(), this)

                val sharedPref: SharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
                val editor = sharedPref.edit()

                editor.putBoolean("AUTO_LOGIN", true)
                editor.putString("USER_USERNAME", usernameBox.text.toString())
                editor.putString("USER_PASSWORD", requestHandler.md5(passwordBox.text.toString()))
                editor.apply()

            }
            else
            {
                Toast.makeText(this, "Les mot de passe ne match pas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun success()
    {

        Toast.makeText(this, "Account has been created successfully, you will be auto-logged in", Toast.LENGTH_LONG).show()
        // go back to login page (to be auto-logged)
        val intent = Intent(this, Login::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}