package com.example.friendzone

import com.mapbox.mapboxsdk.plugins.annotation.Symbol

class Event constructor(event_id: Int) {

    var event_id = event_id
    var type = ""
    var icon = ""
    var symbol : Symbol? = null
    var match=false

}