package com.example.friendzone

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {

    private lateinit var textview : TextView
    private val requestHandler = RequestHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button : Button = findViewById(R.id.button)
        requestHandler.initialize(this)
        textview = findViewById(R.id.textView)

        var getLogin = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                textview.text= result.data!!.dataString

            }
        }

        button.setOnClickListener {
            requestHandler.requestLogin("FGHIRZ", "lasalade", this)
            /*val intent : Intent = Intent(this, Login::class.java).apply {
                putExtra(EXTRA_MESSAGE, "yoyo")
            }
            getLogin.launch(intent)*/
        }
    }

    fun test()
    {
        textview.setText("Alain Ouakbar")
    }
}