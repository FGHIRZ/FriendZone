package com.example.friendzone

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.*
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

        setContentView(R.layout.loading_screen)

        val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        var auto_login = sharedPref.getBoolean("AUTO_LOGIN", false)
        requestHandler.initialize(this)

        Log.d("loginactivity", auto_login.toString())
        if(auto_login)
        {
            val uname : String? = sharedPref.getString("USER_USERNAME", "")
            val pass : String? = sharedPref.getString("USER_PASSWORD", "")
            requestHandler.requestAutoLogin(uname!!, pass!!, this)
        }

        else
        {
            setContentView(R.layout.activity_login)
            val username : EditText = findViewById(R.id.user_id)
            val password : EditText = findViewById(R.id.password)
            val loginButton : Button = findViewById(R.id.login_button)
            val rememberMe : CheckBox = findViewById(R.id.remember_me_checkbox)

            val createAccount : TextView = findViewById(R.id.create_account)
            val editor = sharedPref.edit()


            rememberMe.isChecked= sharedPref.getBoolean("AUTO_LOGIN", true)

            rememberMe.setOnCheckedChangeListener { buttonView, isChecked ->
                editor.putBoolean("AUTO_LOGIN", isChecked)
                editor.apply()
            }

            loginButton.setOnClickListener {
                requestHandler.requestLogin(username.text.toString(), password.text.toString(), this)

                auto_login = rememberMe.isChecked

                if(auto_login)
                {
                    editor.putString("USER_USERNAME", username.text.toString())
                    editor.putString("USER_PASSWORD", requestHandler.md5(password.text.toString()))
                    editor.apply()
                }
            }

            createAccount.setOnClickListener {
                showCreateAccountPage()
            }
        }



    }

    fun startMapActivity(user : User)
    {
        val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        val editor = sharedPref.edit()



        editor.putInt("USER_ID", user.user_id)
        editor.putString("USER_SKIN", user.skin)
        editor.putString("USER_PSEUDO", user.pseudo)
        editor.apply()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun loginError()
    {
        val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        val editor = sharedPref.edit()
        editor.putBoolean("AUTO_LOGIN", false)
        editor.apply()

        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }

    private fun showCreateAccountPage() {
        val intent = Intent(this, AccountCreation::class.java)
        getCreateACcount.launch(intent)
    }
}