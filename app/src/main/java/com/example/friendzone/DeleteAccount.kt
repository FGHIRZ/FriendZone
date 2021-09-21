package com.example.friendzone

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class DeleteAccount : AppCompatActivity() {

    private val requestHandler : RequestHandler = RequestHandler()

    private var PRIVATE_MODE = 0
    private val PREF_NAME = "friendzone-app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_account)

        requestHandler.initialize(this)

        val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        val user_id = sharedPref.getInt("USER_ID", 0)
        val username = sharedPref.getString("USER_USERNAME", "bug")

        val delete_button : Button = findViewById(R.id.send_delete_accounnt_button)
        val password : EditText = findViewById(R.id.delete_account_password_edittext)

        delete_button.setOnClickListener {
            requestHandler.requestAccountDeletion(user_id, username!!,requestHandler.md5(password.text.toString()),this )
        }
    }

    fun success()
    {
        Toast.makeText(this, "Account has been deleted successfully, you will be redirected to login page", Toast.LENGTH_LONG).show()
        finish()
    }
}
