package com.example.friendzone

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import org.json.JSONObject
import java.math.BigInteger
import java.security.MessageDigest

class Login : AppCompatActivity() {


    private val requestHandler :RequestHandler = RequestHandler()
    private var getCreateACcount = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        Log.d("callback", result.toString())
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Compte créé, vous pouvez désormais vous loger", Toast.LENGTH_LONG)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val username : EditText = findViewById(R.id.user_id)
        val password : EditText = findViewById(R.id.password)
        val loginButton : Button = findViewById(R.id.login_button)

        val createAccount : TextView = findViewById(R.id.create_account)

        requestHandler.initialize(this)

        loginButton.setOnClickListener {
            requestHandler.requestLogin(username.text.toString(), password.text.toString(), this)
        }

        createAccount.setOnClickListener {
            showCreateAccountPage()
        }
    }

    fun startMapActivity(user : User)
    {
        val userJSON = JSONObject()
        userJSON.put("user_id", user.id)
        userJSON.put("skin", user.skin)
        val data_json=userJSON.toString()
        val intent: Intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_INFO", data_json)
        }
        startActivity(intent)
        finish()
    }

    private fun showCreateAccountPage() {
        val intent = Intent(this, AccountCreation::class.java)
        getCreateACcount.launch(intent)
    }
}