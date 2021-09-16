package com.example.friendzone

import com.mapbox.mapboxsdk.plugins.annotation.Symbol

class User constructor(id: Int) {
    var id=id
    var username = ""
    var skin = ""
    var symbol : Symbol? = null
    var match=false

}