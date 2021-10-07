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
import com.auth0.android.jwt.JWT
import com.mapbox.android.core.permissions.PermissionsListener


import com.mapbox.android.core.permissions.PermissionsManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*


class Login : AppCompatActivity(), PermissionsListener{


    var permissionsManager: PermissionsManager = PermissionsManager(this)
    var rememberme = true


    private val requestHandler : RequestHandler = RequestHandler()
    private var getCreateACcount = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        Log.d("callback", result.toString())
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Compte créé, vous pouvez désormais vous loger", Toast.LENGTH_LONG)
        }
    }

    private var PRIVATEMODE = 0
    private val PREFNAME = "friendzone-app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.loading_screen)

        val sharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
        requestHandler.initialize(this, sharedPreferences)
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            showLoginPage()
        }
        else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.listener = this
            permissionsManager.requestLocationPermissions(this)
        }
    }

    private fun showLoginPage()
    {
        val sharedPref: SharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
        val refreshToken = sharedPref.getString("REFRESH_TOKEN", "null")
        val now = Date()
        if(refreshToken != "null")
        {
            val refreshJWT = JWT(refreshToken!!)
            if(now < refreshJWT.expiresAt)
            {
                autoLogin()
            }
        }
        else
        {
            setContentView(R.layout.activity_login)
            val username : EditText = findViewById(R.id.user_id)
            val password : EditText = findViewById(R.id.password)
            val loginButton : Button = findViewById(R.id.login_button)
            val rememberMe : CheckBox = findViewById(R.id.remember_me_checkbox)

            val createAccount : TextView = findViewById(R.id.create_account)

            rememberMe.isChecked= true

            rememberMe.setOnCheckedChangeListener { _, isChecked ->
                rememberMe.isChecked = isChecked
            }

            loginButton.setOnClickListener {
                Log.d("INTERNET", "clicked on button")
                requestHandler.requestLogin(username.text.toString(), password.text.toString(), this)
            }
            createAccount.setOnClickListener {
                showCreateAccountPage()
            }
        }
    }

    private fun autoLogin()
    {
        val sharedPref: SharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)

        val userId = sharedPref.getInt("USER_ID", 0)
        requestHandler.requestAccessToken()
        requestHandler.requestClientInfo(userId)
    }

    fun startMapActivity(user : User)
    {
        val sharedPref: SharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
        val editor = sharedPref.edit()

        editor.putInt("USER_ID", user.user_id)
        editor.putString("USER_SKIN", user.skin)
        editor.putString("USER_PSEUDO", user.pseudo)
        editor.apply()

        val intent = Intent(this, CacheActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun loginSuccess(userId : Int, accessToken : String)
    {
        val sharedPref: SharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
        val editor = sharedPref.edit()

        if(rememberme)
        {
            editor.putInt("USER_ID", userId)
            editor.apply()
        }
    }

    fun loginError()
    {
        val sharedPref: SharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(p0: MutableList<String>?) {
    }

    override fun onPermissionResult(p0: Boolean) {
        if(p0)
        {
            showLoginPage()
        }
        else
        {
            Toast.makeText(this, "application can not work without location", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}