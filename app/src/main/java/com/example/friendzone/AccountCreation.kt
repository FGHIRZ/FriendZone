package com.example.friendzone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class AccountCreation : AppCompatActivity() {

    private val requestHandler : RequestHandler = RequestHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_creation)

        requestHandler.initialize(this)
        val createButton : Button = findViewById(R.id.create_button)
        val usernameBox : EditText = findViewById(R.id.usernameSetting)
        val passwordBox : EditText = findViewById(R.id.passwordSetting)
        val passwordCheckBox : EditText = findViewById(R.id.passwordCheck)

        createButton.setOnClickListener {
            if(passwordBox.text.toString() == passwordCheckBox.text.toString())
            {
                requestHandler.requestAccountCreation(usernameBox.text.toString(),passwordBox.text.toString(), this)
            }
            else
            {
                Toast.makeText(this, "Les mot de passe ne match pas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun success()
    {
        Toast.makeText(this, "Account has been created successfully, you can now login", Toast.LENGTH_LONG).show()
        finish()
    }
}