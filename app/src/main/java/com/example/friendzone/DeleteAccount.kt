package com.example.friendzone

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class DeleteAccount : AppCompatActivity() {

    private val requestHandler : RequestHandler = RequestHandler()

    private var PRIVATEMODE = 0
    private val PREFNAME = "friendzone-app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_account)

        requestHandler.initialize(this)

        val sharedPref: SharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
        val userId = sharedPref.getInt("USER_ID", 0)
        val username = sharedPref.getString("USER_USERNAME", "bug")

        val deleteButton : Button = findViewById(R.id.send_delete_accounnt_button)
        val password : EditText = findViewById(R.id.delete_account_password_edittext)

        deleteButton.setOnClickListener {
            requestHandler.requestAccountDeletion(userId, username!!,requestHandler.md5(password.text.toString()),this )
        }
    }

    fun success()
    {
        Toast.makeText(this, "Account has been deleted successfully, you will be redirected to login page", Toast.LENGTH_LONG).show()
//        finish()
        val pref = getSharedPreferences(PREFNAME, PRIVATEMODE)
        val editor = pref.edit()
//        preferences.edit().remove("AUTO_LOGIN")
//        preferences.edit().remove("USER_ID")
//        preferences.edit().remove("USER_USERNAME")
//        preferences.edit().remove("USER_PASSWORD")
        // ou
        editor.clear()
        editor.putString("ACCESS_TOKEN", "null")
        editor.apply()

        // go back to main activity
        val intent = Intent(this, Login::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        finishAffinity()
        startActivity(intent)
    }
}