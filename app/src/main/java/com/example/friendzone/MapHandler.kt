package com.example.friendzone

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import androidx.core.content.ContextCompat
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import org.json.JSONArray
import org.json.JSONObject

class MapHandler {

    private lateinit var symbolManager : SymbolManager
    var mapView: MapView? = null
    lateinit var mapboxMap: MapboxMap
    private lateinit var activity : Activity
    private var savedInstance : Bundle? = null

    fun initMap(activity: Activity, savedInstanceState: Bundle?)
    {
        this.activity=activity
        savedInstance=savedInstanceState
        Mapbox.getInstance(activity, activity.getString(R.string.mapbox_access_token))
        mapView= activity.findViewById(R.id.mapView)
        mapView?.onCreate(savedInstance)
        mapView?.getMapAsync { mapboxMap ->

            mapboxMap.setStyle(Style.Builder().fromUri(activity.resources.getString(R.string.mapbox_style_url))) {
                this.mapboxMap = mapboxMap
                enableLocationComponent(it)
                Log.d("mapHAndler", "enabling location component")
                mapboxMap.setMinZoomPreference(2.00)

                // Create a SymbolManager.
                val mv : MapView = mapView as MapView
                symbolManager = SymbolManager(mv, mapboxMap, it)
                symbolManager.iconAllowOverlap = true
                symbolManager.iconIgnorePlacement = true
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun enableLocationComponent(loadedMapStyle: Style) {

// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(activity)) {

// Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(activity)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(activity, R.color.light_blue_600))

                .minZoomIconScale(2.0f)
                .bearingTintColor(R.color.black)
                .backgroundDrawable(R.drawable.skin1)
                .foregroundDrawable(R.drawable.skin1)
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(activity, loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

// Get an instance of the LocationComponent and then adjust its settings
            mapboxMap.locationComponent.apply {

// Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

// Enable to make the LocationComponent visible
                isLocationComponentEnabled = true

// Set the LocationComponent's camera mode
                cameraMode = CameraMode.TRACKING

// Set the LocationComponent's render mode
                renderMode = RenderMode.COMPASS

            }
        } else {
            (activity as MainActivity).permissionsManager = PermissionsManager(activity as MainActivity)
            (activity as MainActivity).permissionsManager.requestLocationPermissions(activity)
        }
    }

    /*
    private fun updateUsers(userList : JSONArray)
    {

        for(i in 0 until userList.length())
        {
            //Check if user is already in list & update its position
            var userFound = false
            for (user in users)
            {
                if(user.id==(userList[i] as JSONObject).get("user_id"))
                {
                    user.symbol.latLng = LatLng((userList[i] as JSONObject).get("lat") as Double, (userList[i] as JSONObject).get("lon") as Double)
                    userFound = true
                    user.match = true
                }
            }

            //if not, create a new user
            if(!userFound)
            {
                val symbol = symbolManager.create(
                    SymbolOptions()
                    .withLatLng(LatLng((userList[i] as JSONObject).get("lat") as Double, (userList[i] as JSONObject).get("lon") as Double))
                    .withIconImage((userList[i] as JSONObject).get("skin") as String)
                    .withIconSize(1.3f)
                    .withTextOpacity(0.0f))
                val user = User((userList[i] as JSONObject).get("user_id") as Int, symbol)
                user.match= true
                users.add(user)
            }

            for(user in users)
            {
                if(!user.match)
                {
                    symbolManager.delete(user.symbol)
                    Log.d("user deleted", "a")
                    users.remove(user)
                }
                else
                {
                    user.match=false
                }
            }
        }
    }*/






}