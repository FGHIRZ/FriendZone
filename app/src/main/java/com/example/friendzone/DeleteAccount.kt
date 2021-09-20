package com.example.friendzone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class DeleteAccount : AppCompatActivity() {

    private val requestHandler : RequestHandler = RequestHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_account)

        val delete_button : Button = findViewById(R.id.send_delete_accounnt_button)
        val password : EditText = findViewById(R.id.delete_account_password_edittext)

        delete_button.setOnClickListener {
            //requestHandler.requestAccountDeletion()
        }
    }
}