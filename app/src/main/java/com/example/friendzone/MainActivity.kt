package com.example.friendzone

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import com.bumptech.glide.Glide
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : AppCompatActivity(), LocationListener{

    private val requestHandler = RequestHandler()

    private lateinit var userSymbolManager : SymbolManager
    private lateinit var eventSymbolManager : SymbolManager
    private lateinit var flagSymbolManager : SymbolManager
    private lateinit var clientSymbolManager : SymbolManager

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


    private val skinList = mutableListOf<String>()

    private var eventMenuExpanded : Boolean = false
    private var flag : Flag = Flag(false)

    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        this.updateSettings()
    }

    private val profileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        this.updateProfile()

    }

    @SuppressLint("MissingPermission", "ResourceAsColor")
    //Lorsque l'activité est lancée :
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Initialisation handler
        requestHandler.initialize(this)

        //Lire les infos de l'utilisateur
        val sharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
        client= User(sharedPreferences.getInt("USER_ID", 0))
        client.skin = sharedPreferences.getString("USER_SKIN", "default_skin")!!
        client.pseudo = sharedPreferences.getString("USER_PSEUDO", "ERROR")!!

        readSkinList()

        //Initialisation de la mapbox & mise en page
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_map)
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)

        //Chargement des paramètres de la map (Asynchrone)
        loadMap()

        //Chargement des settings
        updateSettings()

        //Initialiser bouton settingsuser.skin
        val settingsButton : Button = findViewById(R.id.settings_button)
        settingsButton.setOnClickListener {
            openSettingsPage()
        }

        val profileButton : Button = findViewById(R.id.custom_button)
        profileButton.setOnClickListener {
            openProfilePage()
        }
        val dropPinView = ImageView(this)
        dropPinView.setImageResource(R.drawable.ic_skin_sourismorte)


        val optionsMenu : LinearLayout = findViewById(R.id.options_menu_layout)
        val dropDownArrow : Button = findViewById(R.id.event_dropdown_arrow)

        val cancelButton : Button = findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener {
            cancelFlag()
        }

        val centerButton : Button = findViewById(R.id.center_view_button)

        centerButton.setOnClickListener {
            val position = CameraPosition.Builder()
                .target(client.symbol!!.latLng)
                .zoom(16.0)
                .build()

            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 500);
        }

        dropDownArrow.setOnClickListener {

            if(eventMenuExpanded)
            {
                optionsMenu.isEnabled = false
                optionsMenu.isVisible = false
                eventMenuExpanded = false
            }
            else
            {
                optionsMenu.isEnabled = true
                optionsMenu.isVisible = true
                eventMenuExpanded = true
            }
        }
        fillScrollView()
    }


//======================SETTINGS=======================
    //Ouvre la page des paramètres
    private fun openSettingsPage()
    {
        val settingsIntent = Intent(this, Settings::class.java)
        settingsLauncher.launch(settingsIntent)
    }

    private fun openProfilePage()
    {
        val settingsIntent = Intent(this, InfosPage::class.java)
        profileLauncher.launch(settingsIntent)
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

    private fun updateProfile()
    {
        val sharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)
        val new_skin = sharedPreferences.getString("USER_SKIN", "default_skin")
        val new_pseudo = sharedPreferences.getString("USER_PSEUDO", "dummy")

        Log.d("ProfilePage", "activity skin :" + new_skin!!)
        client.skin = new_skin!!
        client.pseudo = new_pseudo!!

        client.symbol!!.iconImage = new_skin!!
        clientSymbolManager.update(client.symbol)


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
                val symbol = eventSymbolManager.create(
                    SymbolOptions()
                        .withLatLng(LatLng(newEvent.getDouble("lat"), newEvent.getDouble("lon")))
                        .withIconImage(newEvent.getString("event_type"))
                        .withIconSize( 0.3f))

                val event = Event(newEvent.getInt("event_id"))
                event.type=newEvent.getString("event_type")
                event.symbol=symbol
                event.match= true
                events.add(event)
            }
        }

        for(event in events)
        {
            if(!event.match)
            {
                eventSymbolManager.delete(event.symbol)
                @Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
                users.remove(event)
            }
            else
            {
                eventSymbolManager.update(event.symbol)
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
                    user.skin=newUser.getString("user_skin")
                    user.pseudo = newUser.getString("user_pseudo")
                    user.symbol!!.iconImage=user.skin
                    userFound = true
                    user.match = true
                }
            }

            //if not, create a new user
            if(!userFound)
            {
                val symbol = userSymbolManager.create(
                    SymbolOptions()
                        .withLatLng(LatLng(newUser.getDouble("lat"), newUser.getDouble("lon")))
                        .withIconImage(newUser.getString("user_skin"))
                        .withIconSize( 1.0f)
                        .withIconOpacity(1.0f))

                val user = User(newUser.getInt("user_id"))

                user.pseudo = newUser.getString("user_pseudo")
                user.symbol=symbol
                user.match= true
                users.add(user)
            }
        }

        for(user in users)
        {
            if(!user.match)
            {
                userSymbolManager.delete(user.symbol)
                users.remove(user)
            }
            else
            {
                userSymbolManager.update(user.symbol)
                user.match=false
            }
        }
    }

    private fun deleteUserList()
    {
        for(user in users)
        {
            userSymbolManager.delete(user.symbol)
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

    private fun readSkinList()
    {
        val sharedPref = getSharedPreferences(PREFNAME, PRIVATEMODE)
        val skinListJSON = JSONObject(sharedPref.getString("SKINS_LIST", ""))
        val skinListArray = skinListJSON.getJSONArray("file_list")
        for(i in 0 until skinListArray.length())
        {
            skinList.add(skinListArray.getString(i))
        }
    }

    private fun displayUserInfo(symbol : Symbol) : Boolean
    {
        var user = User(0)

        for (u in users)
        {
            if(symbol == u.symbol)
            {
                user = u
            }
        }

            val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            // Inflate a custom view using layout inflater
            val view = inflater.inflate(R.layout.user_info, mapView, false)

            val skinPreviewImageview : ImageView = view.findViewById(R.id.skin_preview_imageview)

            val skinUrl = requestHandler.serverUrl + "skins/" + user.skin + ".png"
            Glide.with(this)
                .load(skinUrl)
                .into(skinPreviewImageview)

            // Initialize a new instance of popup window
            val popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
                LinearLayout.LayoutParams.MATCH_PARENT// Window height
            )

            popupWindow.showAtLocation(
                mapView,
                1,
                0, 0
            )

            val pseudoDisplay = view.findViewById<TextView>(R.id.user_info_pseudo)
            pseudoDisplay.text = user.pseudo
            val button = view.findViewById<Button>(R.id.user_info_quit)
            button.setOnClickListener {
                popupWindow.dismiss()
            }
        return true
    }


    private fun displayEventInfo(symbol : Symbol) : Boolean
    {
        var event = Event(0)

        for (e in events)
        {
            if(symbol == e.symbol)
            {
                event = e
            }
        }

        val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // Inflate a custom view using layout inflater
        val view = inflater.inflate(R.layout.event_info, mapView, false)

        val eventImageview : ImageView = view.findViewById(R.id.event_imageview)

        val skinUrl = requestHandler.serverUrl + "events/" + event.type + ".png"
        Glide.with(this)
            .load(skinUrl)
            .into(eventImageview)

        // Initialize a new instance of popup window
        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.MATCH_PARENT// Window height
        )

        popupWindow.showAtLocation(
            mapView,
            1,
            0, 0
        )

        val button = view.findViewById<Button>(R.id.event_quit)
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


//======================== FLAG =============================


private fun centerOnFlag()
{
    val position = CameraPosition.Builder()
        .target(flag.symbol!!.latLng)
        .zoom(16.0)
        .build()

    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
}


private fun handleLongClick( clickedPoint : LatLng)
{
    var actorClicked = false
    val clientDistance = clickedPoint.distanceTo(client.symbol!!.latLng)
    if(clientDistance < 10)
    {
        actorClicked = true
        //TODO : CLIENT CLICKED
    }
    if(!actorClicked)
    {
        if(!flag.enabled)
        {
            mapboxMap.uiSettings.isZoomGesturesEnabled = false
            mapboxMap.uiSettings.isScrollGesturesEnabled = false


            val symbol = flagSymbolManager.create(
                SymbolOptions()
                    .withLatLng(clickedPoint)
                    .withIconImage("MeetGameFlag")
                    .withIconSize( 3.0f)
                    .withIconOpacity(1.0f))

            flag.symbol = symbol
            flag.enabled = true
            flag.symbol!!.isDraggable = true

            centerOnFlag()
            val eventMenu : LinearLayout = findViewById(R.id.event_menu)
            eventMenu.isVisible = true

            val cancelButton : Button = findViewById(R.id.cancel_button)
            cancelButton.isVisible=true

        }
    }
}

    private fun cancelFlag()
    {
        val eventMenu : LinearLayout = findViewById(R.id.event_menu)
        val cancelButton : Button = findViewById(R.id.cancel_button)
        eventMenu.isVisible = false
        cancelButton.isVisible = false
        flagSymbolManager.delete(flag.symbol)
        flag.enabled=false
        mapboxMap.uiSettings.isZoomGesturesEnabled = true
        mapboxMap.uiSettings.isScrollGesturesEnabled = true

    }


//====================================================================

//======================== CONFIGURATION =============================


    //Récupère la map sur mapbox studio
    private fun loadMap() {

        mapView?.getMapAsync { mapboxMap ->

            //Lorsque la map est chargée, on éxecute ce code
            mapboxMap.setStyle(Style.Builder().fromUri(resources.getString(R.string.mapbox_style_url))) { it ->
                this.mapboxMap = mapboxMap
                this.mapStyle = it
                mapboxMap.setMinZoomPreference(2.00)

                enableLocationComponent(it)

                userSymbolManager = SymbolManager(mapView!!, mapboxMap, it)
                eventSymbolManager = SymbolManager(mapView!!, mapboxMap, it)
                flagSymbolManager = SymbolManager(mapView!!, mapboxMap, it)
                clientSymbolManager = SymbolManager(mapView!!, mapboxMap, it)

                userSymbolManager.iconAllowOverlap = true
                userSymbolManager.iconIgnorePlacement = true

                eventSymbolManager.iconAllowOverlap = true
                eventSymbolManager.iconIgnorePlacement = true

                flagSymbolManager.iconAllowOverlap = true
                flagSymbolManager.iconIgnorePlacement = true

                clientSymbolManager.iconAllowOverlap = true
                clientSymbolManager.iconIgnorePlacement = true

                val symbol = clientSymbolManager.create(
                    SymbolOptions()
                        .withLatLng(LatLng(mapboxMap.locationComponent.lastKnownLocation!!.latitude, mapboxMap.locationComponent.lastKnownLocation!!.longitude))
                        .withIconImage(client.skin)
                        .withIconSize( 1.2f)
                        .withIconOpacity(1.0f))


                client.symbol = symbol

                updateClientSymbolLoop()

                userSymbolManager.addClickListener { clickedSymbol ->
                    displayUserInfo(clickedSymbol)
                }

                eventSymbolManager.addClickListener { clickedSymbol ->
                    displayEventInfo(clickedSymbol)
                }
                //Activer le tracking de l'utilisateur et la balise de localisation


                mapboxMap.addOnMapLongClickListener { clickLocation ->
                    handleLongClick(clickLocation)
                    true
                }
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
            .bearingTintColor(R.color.black)
            .pulseEnabled(true)
            .minZoomIconScale(0.0f)
            .maxZoomIconScale(0.0f)
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
    }

    private fun updateClientSymbolLoop(){
        val location = mapboxMap.locationComponent.lastKnownLocation!!
        if(client!=null && location!=null)
        {
            client.symbol!!.latLng=LatLng(location.latitude, location.longitude)
            userSymbolManager.update(client.symbol)
        }
        Handler(Looper.getMainLooper()).postDelayed({
            updateClientSymbolLoop()

        }, 100)
    }

    private fun fillScrollView()
    {

        val layout : LinearLayout = findViewById(R.id.scroll_view)

        val sharedPreferences = getSharedPreferences(PREFNAME, PRIVATEMODE)

        val eventListJSON = JSONObject(sharedPreferences.getString("EVENT_LIST", "{}"))
        Log.d("EVENT LIST (MAP)", eventListJSON.toString())
        val eventListArray = eventListJSON.getJSONArray("file_list")

        for(i in 0 until eventListArray.length())
        {
            Log.d("EVENT LIST (MAP)", "adding icon")
            val img = ImageView(this)
            img.layoutParams=LinearLayout.LayoutParams(150,150)
            img.id = View.generateViewId()
            var lp = LinearLayout.LayoutParams(img.layoutParams)
            lp.setMargins(10)
            img.layoutParams = lp
            layout.addView(img)

            img.setOnClickListener {
                requestHandler.requestEventCreation(client.user_id, eventListArray[i] as String, flag.symbol!!.latLng, this )
                val location = mapboxMap.locationComponent.lastKnownLocation!!
                requestHandler.requestEventList(location,this)
                cancelFlag()
            }
            val skinUrl = requestHandler.serverUrl + "events/" + eventListArray[i] + ".png"
            Glide.with(this)
                .load(skinUrl)
                .into(img)
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

    override fun onLocationChanged(location : Location) {
        /*if(client.symbol!= null)
        {
            client.symbol!!.latLng = LatLng(location.latitude, location.longitude)
            symbolManager.update(client.symbol)
        }*/
    }

}

