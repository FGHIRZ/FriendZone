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

        val sharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
        requestHandler.initialize(this, sharedPreferences)
        requestHandler.requestSkinList(this)

    }

    fun loadSkinImages(skinListJSON: JSONObject, eventListJSON : JSONObject)
    {
        val sharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
        sharedPreferences.edit().putString("SKINS_LIST", skinListJSON.toString()).commit()
        sharedPreferences.edit().putString("EVENT_LIST", eventListJSON.toString()).commit()
        Log.d("EVENT LIST (CACHE)", eventListJSON.toString())

        Log.d("LECACHE", skinListJSON.toString())

        val skinList = skinListJSON.getJSONArray("file_list")
        for(i in 0 until skinList.length())
        {
            val url="http://82.165.223.209:8080/skins/" + skinList[i] as String + ".png"
            Glide.with(this)
                .load(url)
        }

        val eventList = eventListJSON.getJSONArray("file_list")
        for(i in 0 until eventList.length())
        {
            val url="http://82.165.223.209:8080/events/" + eventList[i] as String + ".png"
            Glide.with(this)
                .load(url)
        }

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}