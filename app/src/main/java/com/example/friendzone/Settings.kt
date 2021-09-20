package com.example.friendzone

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class Settings : AppCompatActivity() {

    private var PRIVATE_MODE = 0
    private val PREF_NAME = "friendzone-app"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val pref = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        val editor = pref.edit()

        val visibility_switch : Switch = findViewById(R.id.visibility_switch)
        val view_others_switch : Switch = findViewById(R.id.view_others_switch)
        val account_management_button : Button = findViewById(R.id.account_management_button)
        val logout_button : Button = findViewById(R.id.logout_button)

        val user_visibile = pref.getBoolean("USER_VISIBILITY", true)
        visibility_switch.isChecked=user_visibile
        val view_others = pref.getBoolean("VIEW_OTHERS", true)
        view_others_switch.isChecked=view_others

        visibility_switch.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("USER_VISIBILITY", isChecked)
            editor.apply()
        }

        view_others_switch.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("VIEW_OTHERS", isChecked)
            editor.apply()
        }

        account_management_button.setOnClickListener {
            openAccountManagement()
        }


        logout_button.setOnClickListener {
            logout(pref, editor)
        }


    }

    private fun openAccountManagement()
    {
        val intent = Intent(this, AccountManagement::class.java)
        startActivity(intent)
    }

    private fun logout(pref : SharedPreferences, editor : SharedPreferences.Editor)
    {
        editor.putBoolean("AUTO_LOGIN", false)
        editor.apply()

        val returnIntent = Intent()
        setResult(RESULT_CANCELED, returnIntent)

        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }
}