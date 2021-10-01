package com.example.friendzone

import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import org.json.JSONObject

class InfosPage : AppCompatActivity() {
    private var PRIVATEMODE = 0
    private val PREFNAME = "friendzone-app"

    private val requestHandler = RequestHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infos_page)

        requestHandler.initialize(this)
        val sharedPreferences  = getSharedPreferences(PREFNAME, MODE_PRIVATE)
        val skin = sharedPreferences.getString("USER_SKIN", "default_skin")
        val skinPreview : ImageView = findViewById(R.id.skin_preview_imageview)

        val skinListJSON = JSONObject(sharedPreferences.getString("SKINS_LIST", "{}"))
        val skinListArray = skinListJSON.getJSONArray("file_list")
        var index = 0

        for(i in 0 until skinListArray.length())
        {
            if(skin == skinListArray[i])
            {
                index = i
            }
        }

        val pseudoTV : TextView = findViewById(R.id.pseudo_textview)
        val pseudoEdit : EditText = findViewById(R.id.pseudo_edittext)
        pseudoTV.text = sharedPreferences.getString("USER_PSEUDO", "pseudo")
        val skinUrl = requestHandler.serverUrl + "skins/" + skin + ".png"
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
            val new_skin = skinListArray[index]
            val skinUrl = requestHandler.serverUrl + "skins/" + new_skin + ".png"
            Glide.with(this)
                .load(skinUrl)
                .into(skinPreview)
        }

        rightArrow.setOnClickListener {
            if(index==skinListArray.length()-1)
            {
                index=0
            }
            else
            {
                index = index + 1
            }
            val new_skin = skinListArray[index]
            val skinUrl = requestHandler.serverUrl + "skins/" + new_skin + ".png"
            Glide.with(this)
                .load(skinUrl)
                .into(skinPreview)
        }

        val editPen : ImageView = findViewById(R.id.edit_pseudo)
        editPen.setOnClickListener {
            pseudoTV.isVisible = false
            pseudoEdit.isVisible = true
        }

    }
}