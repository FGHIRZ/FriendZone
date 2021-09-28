package com.example.friendzone

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import org.json.JSONObject


class CacheActivity : AppCompatActivity() {
    private var PRIVATEMODE = 0
    private val PREFNAME = "friendzone-app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cache)

        var requestHandler = RequestHandler()

        requestHandler.initialize(this)
        requestHandler.requestSkinList(this)

    }

    fun loadSkinImages(skinListJSON: JSONObject)
    {
        val sharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
        sharedPreferences.edit().putString("SKINS_LIST", skinListJSON.toString()).commit()

        Log.d("LECACHE", skinListJSON.toString())

        val skinList = skinListJSON.getJSONArray("file_list")
        for(i in 0 until skinList.length())
        {
            val url="http://82.165.223.209:8080/skins/" + skinList[i] as String + ".png"
            Glide.with(this)
                .load(url)
        }

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}