package com.example.friendzone

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class Settings : AppCompatActivity() {

    private var PRIVATEMODE = 0
    private val PREFNAME = "friendzone-app"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val pref = getSharedPreferences(PREFNAME, PRIVATEMODE)
        val editor = pref.edit()

        val visibilitySwitch : Switch = findViewById(R.id.visibility_switch)
        val viewOthersSwitch : Switch = findViewById(R.id.view_others_switch)
        val accountManagementButton : Button = findViewById(R.id.account_management_button)
        val logoutButton : Button = findViewById(R.id.logout_button)

        val userVisibile = pref.getBoolean("USER_VISIBILITY", true)
        visibilitySwitch.isChecked=userVisibile
        val viewOthers = pref.getBoolean("VIEW_OTHERS", true)
        viewOthersSwitch.isChecked=viewOthers

        visibilitySwitch.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("USER_VISIBILITY", isChecked)
            editor.apply()
        }

        viewOthersSwitch.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("VIEW_OTHERS", isChecked)
            editor.apply()
        }

        accountManagementButton.setOnClickListener {
            openAccountManagement()
        }


        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun openAccountManagement()
    {
        val intent = Intent(this, AccountManagement::class.java)
        startActivity(intent)
    }

    private fun logout()
    {
        val pref = getSharedPreferences(PREFNAME, PRIVATEMODE)
        val editor = pref.edit()

        editor.putBoolean("AUTO_LOGIN", false)
        editor.apply()

        val intent = Intent(this, Login::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_SINGLE_TOP)
        finishAffinity()
        startActivity(intent)
    }
}