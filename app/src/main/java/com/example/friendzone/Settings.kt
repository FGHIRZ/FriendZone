package com.example.friendzone

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Switch

class Settings : AppCompatActivity() {
    private var editor: SharedPreferences.Editor? = null
    private var pref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        pref = getPreferences(Context.MODE_PRIVATE)
        editor = pref!!.edit()

        val visibility_switch : Switch = findViewById(R.id.visibility_switch)
        val user_visibile = pref!!.getBoolean("USER_VISIBILITY", true)
        visibility_switch.isChecked=user_visibile

        visibility_switch.setOnCheckedChangeListener { _, isChecked ->
            editor!!.putBoolean("USER_VISIBILITY", isChecked)
        }
    }
}