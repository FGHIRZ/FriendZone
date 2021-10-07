package com.example.friendzone

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONObject


class InfosPage : AppCompatActivity() {

    private var PRIVATEMODE = 0
    private val PREFNAME = "friendzone-app"

    private val requestHandler = RequestHandler()

    private var ogSkin = ""
    private var newSkin = ""

    private var pseudo = ""

    private var skinListArray = JSONArray()

    private var editing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infos_page)

        val sharedPreferences: SharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
        requestHandler.initialize(this, sharedPreferences)

        ogSkin = sharedPreferences.getString("USER_SKIN", "default_skin")!!
        newSkin = ogSkin
        val skinPreview : ImageView = findViewById(R.id.skin_preview_imageview)

        val skinListJSON = JSONObject(sharedPreferences.getString("SKINS_LIST", "{}"))
        val userId = sharedPreferences.getInt("USER_ID", 0)

        skinListArray = skinListJSON.getJSONArray("file_list")
        var index = 0

        Log.d("ProfilePage", skinListArray.toString())
        Log.d("ProfilePage", "My skin : "  + ogSkin)

        for(i in 0 until skinListArray.length()-1)
        {
            if(ogSkin == skinListArray[i])
            {
                index = i
                Log.d("ProfilePage", "index found : " + index.toString())
                Log.d("ProfilePage", skinListArray.getString(index))
            }
        }

        val pseudoTV : TextView = findViewById(R.id.pseudo_textview)
        val pseudoEdit : EditText = findViewById(R.id.pseudo_edittext)
        pseudo = sharedPreferences.getString("USER_PSEUDO", "pseudo")!!
        pseudoTV.text = pseudo

        val skinUrl = requestHandler.serverUrl + "skins/" + ogSkin + ".png"
        Glide.with(this)
            .load(skinUrl)
            .into(skinPreview)

        val leftArrow : ImageView = findViewById(R.id.left_arrow)
        val rightArrow : ImageView = findViewById(R.id.right_arrow)

        leftArrow.setOnClickListener {

            if(index==0)
            {
                index=skinListArray.length()-1
            }
            else
            {
                index = index - 1
            }
            update_skin(index,skinPreview)
        }

        rightArrow.setOnClickListener {
            if(index==skinListArray.length()-1)
            {
                index=0
            }
            else
            {
                index += 1
            }
            update_skin(index,skinPreview)
        }

        val editPen : ImageView = findViewById(R.id.edit_pseudo)
        editPen.setImageResource(R.drawable.edit_icon)
        editPen.setOnClickListener {
            if(!editing)
            {
                pseudoTV.isVisible = false
                pseudoEdit.isVisible = true
                pseudoEdit.setText(pseudo)
                editPen.setImageResource(R.drawable.validate)
                editing = true
            }
            else
            {
                pseudoTV.isVisible = true
                pseudoEdit.isVisible = false
                pseudo = pseudoEdit.text.toString()
                pseudoTV.text = pseudo
                editPen.setImageResource(R.drawable.edit_icon )
                editing = false

                val thread = Thread { requestHandler.requestPseudoChange(pseudo, userId, this) }
                thread.start()
                sharedPreferences.edit().putString("USER_PSEUDO", pseudo).commit()
            }
        }
    }

    private fun update_skin(index : Int, skinPreview : ImageView)
    {
        val sharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
        newSkin = skinListArray.getString(index)
        Log.d("ProfilePage", skinListArray.getString(index))
        Log.d("ProfilePage", index.toString())
        val skinUrl = requestHandler.serverUrl + "skins/" + newSkin + ".png"
        Glide.with(this)
            .load(skinUrl)
            .into(skinPreview)
        sharedPreferences.edit().putString("USER_SKIN", newSkin).apply()
    }
    private fun ApplyChanges()
    {
        val sharedPreferences  = getSharedPreferences(PREFNAME, MODE_PRIVATE)
        val userId = sharedPreferences.getInt("USER_ID", 0)
        Log.d("ProfilePage", ogSkin + " ,  " + newSkin)
        if (ogSkin != newSkin){
            val thread = Thread { requestHandler.requestSkinChange(newSkin,userId, this)}
            thread.start()
        }
    }

     override fun onDestroy() {
         ApplyChanges()
         super.onDestroy()
    }
}