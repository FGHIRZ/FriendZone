package com.example.friendzone

import com.mapbox.mapboxsdk.plugins.annotation.Symbol

class User constructor(user_id : Int)
{

    var user_id = user_id
    var username = ""
    var skin = ""
    var pseudo = ""
    var symbol : Symbol? = null
    var match=false

}