package com.example.friendzone

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

class MainActivity : AppCompatActivity(){




    private val requestHandler = RequestHandler()

    private lateinit var symbolManager : SymbolManager
    private var mapView: MapView? = null
    private lateinit var mapboxMap: MapboxMap

    private var users = mutableListOf<User>()
    private var events = mutableListOf<Event>()


    private lateinit var client : User

    private lateinit var mapStyle : Style

    private var userIsVisible = true
    private var viewOthers = true

    private var PRIVATEMODE = 0
    private val PREFNAME = "friendzone-app"

    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        this.updateSettings()
    }


    //Lorsque l'activité est lancée :
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Initialisation handler
        requestHandler.initialize(this)



        //Lire les infos de l'utilisateur
        val sharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
        client= User(sharedPreferences.getInt("USER_ID", 0))
        client.skin = sharedPreferences.getString("USER_SKIN", "default_skin")!!
        client.pseudo = sharedPreferences.getString("USER_PSEUDO", "none")!!

        //Initialisation de la mapbox & mise en page
        setContentView(R.layout.activity_map)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)

        //Chargement des paramètres de la map (Asynchrone)
        loadMap()

        //Chargement des settings
        updateSettings()

        //Initialiser bouton settings
        val settingsButton : Button = findViewById(R.id.settings_button)
        settingsButton.setOnClickListener {
            openSettingsPage()
        }
    }


//======================SETTINGS=======================
    //Ouvre la page des paramètres
    private fun openSettingsPage()
    {
        val settingsIntent = Intent(this, Settings::class.java)
        settingsLauncher.launch(settingsIntent)
    }

    private fun updateSettings()
    {
        val sharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
        userIsVisible = sharedPreferences.getBoolean("USER_VISIBILITY", true)
        viewOthers = sharedPreferences.getBoolean("VIEW_OTHERS", true)

        if(!viewOthers)
        {
            deleteUserList()
        }
    }
//=========================================================



//============================= Updates ========================

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

            val newEvent : JSONObject = eventList[i] as JSONObject
            var eventFound = false
            for (event in events)
            {
                if(event.event_id==newEvent.getInt("event_id"))
                {
                    event.symbol!!.latLng = LatLng(newEvent.getDouble("lat"), newEvent.getDouble("lon"))
                    eventFound = true
                    event.match = true
                }
            }

            //if not, create a new user
            if(!eventFound)
            {
                val symbol = symbolManager.create(
                    SymbolOptions()
                        .withLatLng(LatLng(newEvent.getDouble("lat"), newEvent.getDouble("lon")))
                        .withIconImage(newEvent.getString("type"))
                        .withIconSize( 1.2f))

                val event = Event(newEvent.getInt("event_id"))
                event.type=newEvent.getString("type")
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
                @Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
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

            val newUser : JSONObject = userList[i] as JSONObject
            var userFound = false
            for (user in users)
            {
                if(user.user_id==newUser.getInt("user_id"))
                {
                    user.symbol!!.latLng = LatLng(newUser.getDouble("lat"), newUser.getDouble("lon"))
                    user.skin=newUser.getString("skin")
                    user.pseudo = newUser.getString("pseudo")
                    user.symbol!!.iconImage=user.skin
                    userFound = true
                    user.match = true
                }
            }

            //if not, create a new user
            if(!userFound)
            {
                val symbol = symbolManager.create(
                    SymbolOptions()
                        .withLatLng(LatLng(newUser.getDouble("lat"), newUser.getDouble("lon")))
                        .withIconImage(newUser.getString("skin"))
                        .withIconSize( 1.0f)
                        .withIconOpacity(1.0f))

                val user = User(newUser.getInt("user_id"))

                symbolManager.addClickListener {
                    displayUserMenu(user)
                }
                user.pseudo = newUser.getString("pseudo")
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

    private fun deleteUserList()
    {
        for(user in users)
        {
            symbolManager.delete(user.symbol)
            users.remove(user)
        }
    }

    /*
    private fun createEvent(location : LatLng)
    {
        requestHandler.requestEventCreation(client.user_id, "event_test_icon", location, this)
    }*/
//=====================================================




//========================  User Menus & Popups =============================

    private fun displayUserMenu(user : User) : Boolean
    {
        val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // Inflate a custom view using layout inflater
        val view = inflater.inflate(R.layout.user_info,mapView,false)

        // Initialize a new instance of popup window
        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT// Window height
        )

        popupWindow.showAtLocation(mapView
            ,
            1,
            0,0)

        val pseudoDisplay = view.findViewById<TextView>(R.id.user_info_pseudo)
        pseudoDisplay.text = user.pseudo
        val button= view.findViewById<Button>(R.id.user_info_quit)
        button.setOnClickListener {
            popupWindow.dismiss()
        }

        return true

    }

    private fun displayMyMenu() : Boolean
    {
        val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // Inflate a custom view using layout inflater
        val view = inflater.inflate(R.layout.custom_menu,mapView,false)

        // Initialize a new instance of popup window
        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT// Window height
        )

        popupWindow.showAtLocation(mapView
            ,
            1,
            0,0)

        val pseudoDisplay = view.findViewById<TextView>(R.id.user_info_pseudo)
        pseudoDisplay.text = client.pseudo
        val button= view.findViewById<Button>(R.id.user_info_quit)
        button.setOnClickListener {
            popupWindow.dismiss()
        }
        return true
    }

/*
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

        //Paramètre le bouton de retour
        val button= view.findViewById<Button>(R.id.button_popup)
        button.setOnClickListener {
            createEvent(location)
            popupWindow.dismiss()
        }

        //Montre la fenêtre
        popupWindow.showAtLocation(mapView
            ,
            1,
            0,0)
        return true
    }*/

//=====================================================


//======================== CONFIGURATION =============================


    //Récupère la map sur mapbox studio
    private fun loadMap() {

        mapView?.getMapAsync { mapboxMap ->

            //Lorsque la map est chargée, on éxecute ce code
            mapboxMap.setStyle(Style.Builder().fromUri(resources.getString(R.string.mapbox_style_url))) {
                this.mapboxMap = mapboxMap
                this.mapStyle = it
                mapboxMap.setMinZoomPreference(2.00)

                symbolManager = SymbolManager(mapView!!, mapboxMap, it)
                symbolManager.iconAllowOverlap = true
                symbolManager.iconIgnorePlacement = true

                //Activer le tracking de l'utilisateur et la balise de localisation
                enableLocationComponent(it)

                //Commencer la boucle de contrôle principale
                updateLoop()

            }
        }
    }


    @SuppressLint("MissingPermission", "ResourceAsColor")
    private fun enableLocationComponent(loadedMapStyle: Style) {

// Create and customize the LocationComponent's options
        val customLocationComponentOptions = LocationComponentOptions.builder(this)
            .trackingGesturesManagement(true)
            .accuracyColor(ContextCompat.getColor(this, R.color.light_blue_600))
            .minZoomIconScale(1.0f)
            .bearingTintColor(R.color.black)
            .pulseEnabled(true)
            .backgroundDrawable(R.drawable.ic_shadow)
            .backgroundDrawableStale(R.drawable.ic_shadow)
            .foregroundDrawableStale(R.drawable.ic_skin_sourismorte)
            .foregroundDrawable(R.drawable.ic_skin_sourismorte)
            .minZoomIconScale(1.2f)
            .maxZoomIconScale(1.7f)
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
        mapboxMap.locationComponent.addOnLocationLongClickListener {
            displayMyMenu()
        }
    }

//===============================================================



//Fonctions de l'activité
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

