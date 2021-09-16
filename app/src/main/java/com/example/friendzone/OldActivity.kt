package com.example.friendzone

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.android.volley.DefaultRetryPolicy
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
import org.json.JSONArray
import org.json.JSONObject


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */

class OldActivity : AppCompatActivity(), PermissionsListener {

    private var mapView: MapView? = null
    private var permissionsManager: PermissionsManager = PermissionsManager(this)
    private lateinit var mapboxMap: MapboxMap
    private lateinit var symbolManager : SymbolManager
    private var myID : Int = 0
    private var myUsername : String = ""
    private lateinit var mySkin : String
    private lateinit var queue : RequestQueue
    private val url = "http://82.165.223.209:8080/"

    private var users = mutableListOf<User>()
    private var savedInstance: Bundle? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstance = savedInstanceState
        queue = Volley.newRequestQueue(this)
        showLoginScreen()

        //startMapActivity(savedInstanceState)
    }

    private fun startMapActivity(savedInstanceState: Bundle?){
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_map)
        mySkin = "skin1"

        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { mapboxMap ->

            mapboxMap.setStyle(Style.Builder().fromUri(resources.getString(R.string.mapbox_style_url))) {
                this.mapboxMap = mapboxMap
                enableLocationComponent(it)
                mapboxMap.setMinZoomPreference(2.00)

                // Create a SymbolManager.
                val mv : MapView = mapView as MapView
                symbolManager = SymbolManager(mv, mapboxMap, it)
                symbolManager.iconAllowOverlap = true
                symbolManager.iconIgnorePlacement = true

                mapboxMap.getStyle { style -> style.addImage("skin1", BitmapFactory.decodeResource(resources,R.drawable.skin1))
                    style.addImage("skin2", BitmapFactory.decodeResource(resources,R.drawable.skin2))
                    style.addImage("skin3", BitmapFactory.decodeResource(resources,R.drawable.skin3))}

                val settings_button : Button = findViewById(R.id.settings_button)
                settings_button.setOnClickListener {

                    showSettingsPage()
                }
                startScreenRefresh()

            }
        }

    }

    private fun showSettingsPage()
    {
        val settingspage : LinearLayout = findViewById(R.id.llayout)
        (mapView as MapView).isVisible=false
        settingspage.isVisible=true
        var back_button : Button = findViewById(R.id.back_button)
        back_button.setOnClickListener {
            (mapView as MapView).isVisible=true
            settingspage.isVisible=false
        }
    }

    private fun startScreenRefresh(){

        requestUserList(mapboxMap.locationComponent.lastKnownLocation!!)
        Handler(Looper.getMainLooper()).postDelayed({
            startScreenRefresh()
        }, 10000)
    }

    private fun login(username : String, password : String){

        // Request a string response from the provided URL.

        myUsername = username
        val json= JSONObject()
        val userJSON = JSONObject()
        userJSON.put("name", username)
        userJSON.put("password", password)
        json.put("request", "login")
        json.put("params",userJSON)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, json,
            { response ->
                Toast.makeText( this, response.get("status") as String, Toast.LENGTH_LONG).show()
                Log.d("yolo", response.toString())
                handleLogin(response, savedInstance)
            },
            { Toast.makeText( this, "no response", Toast.LENGTH_LONG).show() })
        jsonObjectRequest.setRetryPolicy(
            DefaultRetryPolicy(
                4000,
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        )
        queue.add(jsonObjectRequest)
    }

    private fun createAccount()
    {

        setContentView(R.layout.new_account_page)

        val createButton : Button = findViewById(R.id.create_button)
        val usernamePrompt : EditText = findViewById(R.id.chose_user_id)
        val passwordPrompt : EditText = findViewById(R.id.chose_password)
        val passwordCheck : EditText = findViewById(R.id.verify_password)

        createButton.setOnClickListener {
            if(passwordPrompt.text.toString() == passwordCheck.text.toString())
            {
                createAccountRequest(usernamePrompt.text.toString(), passwordPrompt.text.toString())
            }
            else
            {
                Toast.makeText(this, "password doesn't match", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createAccountRequest(username: String, password: String)
    {

        val json = JSONObject()
        val userJSON= JSONObject()
        userJSON.put("name", username)
        userJSON.put("password", password)
        json.put("request", "create_account")
        json.put("params", userJSON)

        val createAccountRequest= JsonObjectRequest(Request.Method.POST, url, json, {response->
            if(response.get("status") as String == "ok")
            {
                showLoginScreen()
                Toast.makeText(this, "Account successfully created, you can now log in with your password", Toast.LENGTH_SHORT ).show()
            }
            else
            {
                Toast.makeText(this, "Error : " + (response.get("params") as JSONObject).get("description"), Toast.LENGTH_SHORT ).show()
            }
        }, {
            Toast.makeText(this, "Server not responding", Toast.LENGTH_SHORT ).show()
        })
        createAccountRequest.setRetryPolicy(
            DefaultRetryPolicy(
                4000,
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))

        queue.add(createAccountRequest)

    }

    private fun showLoginScreen()
    {
        setContentView(R.layout.login_page)
        var u_name : EditText = findViewById(R.id.user_id)
        var pass : EditText = findViewById(R.id.password)
        var loginButton : Button = findViewById(R.id.login_button)
        var createAccountText : TextView = findViewById(R.id.create_account)

        loginButton.setOnClickListener {
            myUsername = u_name.text.toString()
            login(u_name.text.toString(), pass.text.toString())}

        createAccountText.setOnClickListener {
            createAccount()}

    }

    private fun handleLogin(response : JSONObject, savedInstanceState: Bundle?)
    {
        if(response.get("status")=="ok")
        {
            Log.d("yolo", response.toString())
            myID=(response.get("params") as JSONObject).getInt("user_id")
            mySkin=(response.get("params") as JSONObject).getString("skin")
            startMapActivity(savedInstanceState)
        }
    }

    private fun requestUserList(location : Location){

        // Request a string response from the provided URL.

        val json= JSONObject()
        val locationJSON = JSONObject()
        locationJSON.put("lat", location.latitude)
        locationJSON.put("lon", location.longitude)
        val userJSON = JSONObject()
        userJSON.put("id", myID)
        userJSON.put("location", locationJSON)
        json.put("request", "update")
        json.put("params",userJSON)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, json,
            { response ->
                Log.d("update_response", response.toString())
                val userList = response.get("params") as JSONArray
                updateUsers(userList)
                refreshScreen()
            },
            { Log.d("yolo", "no response")})
        queue.add(jsonObjectRequest)
    }

    //bb
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
                val symbol = symbolManager.create(SymbolOptions()
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

    private fun logout()
    {
        val json= JSONObject()
        val userJSON = JSONObject()
        userJSON.put("name", myUsername)
        json.put("request", "logout")
        json.put("params",userJSON)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, json,
            {
            },
            { })
        jsonObjectRequest.setRetryPolicy(
            DefaultRetryPolicy(
                4000,
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        )
        queue.add(jsonObjectRequest)

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
        logout()
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