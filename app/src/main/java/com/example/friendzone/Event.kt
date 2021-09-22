package com.example.friendzone

import com.mapbox.mapboxsdk.plugins.annotation.Symbol

class Event constructor(var event_id: Int) {

    var type = ""
    var symbol : Symbol? = null
    var match=false

}