package com.example.friendzone

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity(), PermissionsListener {



    val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
            result: ActivityResult ->
        if(result.resultCode==Activity.RESULT_CANCELED)
        {
            finish()
        }
        this.updateSettings()
    }

    private lateinit var textview : TextView
    private val requestHandler = RequestHandler()
    var permissionsManager: PermissionsManager = PermissionsManager(this)

    private lateinit var symbolManager : SymbolManager
    var mapView: MapView? = null
    lateinit var mapboxMap: MapboxMap
    private lateinit var activity : Activity
    private var savedInstance : Bundle? = null
    
    private var users = mutableListOf<User>()
    private var events = mutableListOf<Event>()


    private var mapUrl = "mapbox://styles/meetgameproject/ckt8pxo7y28vs19v1qlyjrk8v"

    private lateinit var client : User

    private lateinit var mapStyle : Style

    private var userIsVisible = true
    private var viewOthers = true

    private var PRIVATE_MODE = 0
    private val PREF_NAME = "friendzone-app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestHandler.initialize(this)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map)
        val intent = intent
        val userString = intent.getStringExtra("USER_INFO")
        val userJSON = JSONObject(userString)
        client= User(userJSON.getInt("user_id"))
        client.skin = userJSON.getString("skin")
        mapView = findViewById(R.id.mapView);
        mapView?.onCreate(savedInstanceState);
        loadMap()
        val settingsButton : Button = findViewById(R.id.settings_button)

        val sharedPref = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        updateSettings()

        settingsButton.setOnClickListener {
            openSettingsPage()
        }
    }

    private fun openSettingsPage()
    {
        val settingsIntent = Intent(this, Settings::class.java)
        settingsLauncher.launch(settingsIntent)
    }

    private fun loadMap()
    {
        mapView?.getMapAsync { mapboxMap ->

            mapboxMap.setStyle(Style.Builder().fromUri(resources.getString(R.string.mapbox_style_url))) {
                this.mapboxMap = mapboxMap
                this.mapStyle = it
                mapboxMap.setMinZoomPreference(2.00)

                symbolManager = SymbolManager(mapView!!, mapboxMap, it)
                symbolManager.iconAllowOverlap = true
                symbolManager.iconIgnorePlacement = true

                //loadSkins(mapboxMap)
                enableLocationComponent(it)
                updateLoop()

                mapboxMap.addOnMapLongClickListener {
                    showEventCreationWindow(it)
                }


            }
        }
    }

    fun updateSettings()
    {
        val sharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        userIsVisible = sharedPreferences.getBoolean("USER_VISIBILITY", true)
        viewOthers = sharedPreferences.getBoolean("VIEW_OTHERS", true)

        if(!viewOthers)
        {
            delete_user_list()
        }

    }

    private fun delete_user_list()
    {
        for(user in users)
        {
            symbolManager.delete(user.symbol)
            users.remove(user)
        }
    }

    private fun updateLoop(){
        val location = mapboxMap.locationComponent.lastKnownLocation!!
        if(viewOthers)
        {
            requestHandler.requestUserList(location, client, userIsVisible, this)
        }
        requestHandler.requestEventList(location, this)
        Handler(Looper.getMainLooper()).postDelayed({
            updateLoop()
        }, 10000)
    }

    fun updateEventList(eventList : JSONArray)
    {
        for(i in 0 until eventList.length())
        {
            //Check if user is already in list & update its position

            val new_event : JSONObject = eventList[i] as JSONObject
            var eventFound = false
            for (event in events)
            {
                if(event.event_id==new_event.getInt("event_id"))
                {
                    event.symbol!!.latLng = LatLng(new_event.getDouble("lat"), new_event.getDouble("lon"))
                    eventFound = true
                    event.match = true
                }
            }

            //if not, create a new user
            if(!eventFound)
            {
                val symbol = symbolManager.create(
                    SymbolOptions()
                        .withLatLng(LatLng(new_event.getDouble("lat"), new_event.getDouble("lon")))
                        .withIconImage(new_event.getString("type"))
                        .withIconSize( 1.2f))

                val event = Event(new_event.getInt("event_id"))
                event.type=new_event.getString("type")
                event.symbol=symbol
                event.match= true
                events.add(event)
            }
        }

        for(event in events)
        {
            if(!event.match)
            {
                symbolManager.delete(event.symbol)
                users.remove(event)
            }
            else
            {
                symbolManager.update(event.symbol)
                event.match=false
            }
        }
    }


    fun updateUserList(userList : JSONArray)
    {
        for(i in 0 until userList.length())
        {
            //Check if user is already in list & update its position

            val new_user : JSONObject = userList[i] as JSONObject
            var userFound = false
            for (user in users)
            {
                if(user.user_id==new_user.getInt("user_id"))
                {
                    user.symbol!!.latLng = LatLng(new_user.getDouble("lat"), new_user.getDouble("lon"))
                    userFound = true
                    user.match = true
                }
            }

            //if not, create a new user
            if(!userFound)
            {
                val symbol = symbolManager.create(
                    SymbolOptions()
                        .withLatLng(LatLng(new_user.getDouble("lat"), new_user.getDouble("lon")))
                        .withIconImage(new_user.getString("skin"))
                        .withIconSize( 1.0f))

                val user = User(new_user.getInt("user_id"))
                user.symbol=symbol
                user.match= true
                users.add(user)
            }
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
                symbolManager.update(user.symbol)
                user.match=false
            }
        }
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {

// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {


// Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(this, R.color.light_blue_600))
                .minZoomIconScale(1.0f)
                .bearingTintColor(R.color.black)
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
                renderMode = RenderMode.NORMAL


            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    private fun showEventCreationWindow(location : LatLng): Boolean {
    val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    // Inflate a custom view using layout inflater
    val view = inflater.inflate(R.layout.pop_event,mapView,false)

    // Initialize a new instance of popup window
    val popupWindow = PopupWindow(
        view, // Custom view to show in popup window
        LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
        LinearLayout.LayoutParams.WRAP_CONTENT// Window height
    )
    val button= view.findViewById<Button>(R.id.button_popup)
    button.setOnClickListener {
        createEvent(location)
        popupWindow.dismiss()
    }

    popupWindow.showAtLocation(mapView
        ,
        1,
        0,0)
    return true

}

    private fun createEvent(location : LatLng)
    {
        requestHandler.requestEventCreation(client.user_id, "event_test_icon", location, this)
    }

    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

}
