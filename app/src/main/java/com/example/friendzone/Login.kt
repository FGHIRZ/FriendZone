package com.example.friendzone

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject


class Login : AppCompatActivity() {


    private val requestHandler :RequestHandler = RequestHandler()
    private var getCreateACcount = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        Log.d("callback", result.toString())
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Compte créé, vous pouvez désormais vous loger", Toast.LENGTH_LONG)
        }
    }

    private var PRIVATE_MODE = 0
    private val PREF_NAME = "friendzone-app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        val auto_login = sharedPref.getBoolean("AUTO_LOGIN", false)


        val username : EditText = findViewById(R.id.user_id)
        val password : EditText = findViewById(R.id.password)
        val loginButton : Button = findViewById(R.id.login_button)

        val createAccount : TextView = findViewById(R.id.create_account)

        requestHandler.initialize(this)



        loginButton.setOnClickListener {
            requestHandler.requestLogin(username.text.toString(), password.text.toString(), this)

            val editor = sharedPref.edit()
            editor.putString("USERNAME", username.text.toString())
            editor.putString("PASSWORD", requestHandler.md5(password.text.toString()))
            editor.apply()
        }

        createAccount.setOnClickListener {
            showCreateAccountPage()
        }

        if(auto_login)
        {
            val uname : String? = sharedPref.getString("USERNAME", "")
            val pass : String? = sharedPref.getString("PASSWORD", "")
            requestHandler.requestAutoLogin(uname!!, pass!!, this)
        }
    }

    fun startMapActivity(user : User)
    {
        val userJSON = JSONObject()
        userJSON.put("user_id", user.user_id)
        userJSON.put("skin", user.skin)
        val data_json=userJSON.toString()
        val intent: Intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_INFO", data_json)
        }
        val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        val editor = sharedPref.edit()
        editor.putBoolean("AUTO_LOGIN", true)
        editor.putInt("user_id", user.user_id)
        editor.putString("skin", user.skin)
        editor.apply()
        startActivity(intent)
        finish()
    }

    private fun showCreateAccountPage() {
        val intent = Intent(this, AccountCreation::class.java)
        getCreateACcount.launch(intent)
    }
}