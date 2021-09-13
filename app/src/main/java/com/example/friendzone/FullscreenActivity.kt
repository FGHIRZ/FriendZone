package com.example.friendzone

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.mapbox.android.core.permissions.PermissionsListener
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
import org.json.JSONObject


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity(), PermissionsListener {

    private var mapView: MapView? = null
    private var permissionsManager: PermissionsManager = PermissionsManager(this)
    private lateinit var mapboxMap: MapboxMap
    private lateinit var symbolManager : SymbolManager
    private var myId : Int = 108
    private lateinit var mySkin : String
    private lateinit var queue : RequestQueue
    private val url = "http://82.165.223.209:8080/"

    private var users = mutableListOf<User>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_fullscreen)
        mySkin = "skin1"

        queue = Volley.newRequestQueue(this)
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { mapboxMap ->

            mapboxMap.setStyle(Style.Builder().fromUri(resources.getString(R.string.mapbox_style_url))) {
                this.mapboxMap = mapboxMap
                enableLocationComponent(it)
                mapboxMap.setMinZoomPreference(13.00)
                
                // Create a SymbolManager.
                val mv : MapView = mapView as MapView
                symbolManager = SymbolManager(mv, mapboxMap, it)
                symbolManager.iconAllowOverlap = true
                symbolManager.iconIgnorePlacement = true

                mapboxMap.getStyle { style -> style.addImage("skin1", BitmapFactory.decodeResource(resources,R.drawable.skin1))
                    style.addImage("skin2", BitmapFactory.decodeResource(resources,R.drawable.skin2))
                    style.addImage("skin3", BitmapFactory.decodeResource(resources,R.drawable.skin3))}

                requestLogIn()

            }
        }
    }

    private fun startScreenRefresh(){

        requestUserList(mapboxMap.locationComponent.lastKnownLocation!!)
        Handler(Looper.getMainLooper()).postDelayed({
            startScreenRefresh()
        }, 1000)
    }

    private fun requestLogIn(){

        // Request a string response from the provided URL.

        val json= JSONObject()
        val userJSON = JSONObject()
        userJSON.put("id", myId)
        json.put("request", "login")
        json.put("params",userJSON)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, json,
            { response ->
                Toast.makeText( this, response.get("status") as String, Toast.LENGTH_LONG).show()
                startScreenRefresh()
            },
            { Toast.makeText( this, "no response", Toast.LENGTH_LONG).show() })

        queue.add(jsonObjectRequest)
    }

    private fun requestUserList(location : Location){


        // Request a string response from the provided URL.

        val json= JSONObject()
        val locationJSON = JSONObject()
        locationJSON.put("lat", location.latitude)
        locationJSON.put("lon", location.longitude)
        val userJSON = JSONObject()
        userJSON.put("id", myId)
        userJSON.put("location", locationJSON)
        json.put("request", "update")
        json.put("params",userJSON)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, json,
            { response ->
                // Display the first 500 characters of the response string.
                Toast.makeText( this, "received", Toast.LENGTH_LONG).show()
                val params = response.get("params") as JSONObject
                val userList = params.get("users") as JSONObject
                //updateUsers(userList)
                //refreshScreen()
            },
            { Toast.makeText( this, "no response", Toast.LENGTH_LONG).show() })

        queue.add(jsonObjectRequest)
    }

    private fun updateUsers(userList : JSONObject)
    {
        val keys: Iterator<String> = userList.keys()
        while (keys.hasNext())
        {
            val key = keys.next()

            //Check if user is already in list & update its position
            var userFound = false
            for (user in users)
            {
                if(user.id==(userList.get(key) as JSONObject).get("id"))
                {
                    user.symbol.latLng = LatLng((userList.get(key) as JSONObject).get("lat") as Double, (userList.get(key) as JSONObject).get("lon") as Double)
                    userFound = true
                    user.match = true
                }
            }

            //if not, create a new user
            if(!userFound)
            {
                val symbol = symbolManager.create(SymbolOptions()
                    .withLatLng(LatLng((userList.get(key) as JSONObject).get("lat") as Double, (userList.get(key) as JSONObject).get("lon") as Double))
                    .withIconImage((userList.get(key) as JSONObject).get("skin") as String)
                    .withIconSize(1.3f)
                    .withTextOpacity(0.0f))
                val user = User((userList.get(key) as JSONObject).get("id") as String, symbol)
                users.add(user)
            }

            //remove users no more in list
            for (user in users)
            {
                if(!user.match)
                {
                    symbolManager.delete(user.symbol)
                    users.remove(user)
                }
                else
                {
                    user.match=false
                }
            }
        }
    }

    private fun refreshScreen()
    {
        for(user in users)
        {
            symbolManager.update(user.symbol)
        }
    }


    private fun showEventWindow()
    {
        val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Inflate a custom view using layout inflater
        val view = inflater.inflate(R.layout.pop_event,mapView,false)

        // Initialize a new instance of popup window
        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        )
        val button= view.findViewById<Button>(R.id.button_popup)
        button.setOnClickListener {
            popupWindow.dismiss()
        }

        popupWindow.showAtLocation(mapView
            ,
            1,
            0,0)
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {

// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

// Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(this, R.color.light_blue_600))

                .minZoomIconScale(2.0f)
                .bearingTintColor(R.color.black)
                .backgroundDrawable(R.drawable.skin1)
                .foregroundDrawable(R.drawable.skin1)
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(this, loadedMapStyle)
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
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()

    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap.style!!)
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show()
            finish()
        }
    }

}