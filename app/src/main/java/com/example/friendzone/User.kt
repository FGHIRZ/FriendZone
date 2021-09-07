package com.example.friendzone

import com.mapbox.mapboxsdk.plugins.annotation.Symbol

class User {
    lateinit var symbol: Symbol
    lateinit var id: String
    lateinit var pseudo: String
    var Lat: Double = 0.0
    var Lon: Double = 0.0

}