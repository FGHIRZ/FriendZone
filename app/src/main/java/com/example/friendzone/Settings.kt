package com.example.friendzone

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Switch
import androidx.core.app.ActivityCompat

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
    }
}