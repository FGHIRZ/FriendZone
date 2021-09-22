package com.example.friendzone

import com.mapbox.mapboxsdk.plugins.annotation.Symbol

class User constructor(var user_id: Int)
{

    var username = ""
    var skin = ""
    var pseudo = ""
    var symbol : Symbol? = null
    var match=false

}