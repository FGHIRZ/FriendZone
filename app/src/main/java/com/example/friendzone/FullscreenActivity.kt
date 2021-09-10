package com.example.friendzone

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.JsonElement
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
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity(), PermissionsListener {

    private var mapView: MapView? = null
    private var permissionsManager: PermissionsManager = PermissionsManager(this)
    private lateinit var mapboxMap: MapboxMap
    private lateinit var symbolManager : SymbolManager

    private var users = mutableListOf<User>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_fullscreen)

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

// Set non-data-driven properties.
                symbolManager.iconAllowOverlap = true
                symbolManager.iconIgnorePlacement = true

                mapboxMap.getStyle { style -> style.addImage("skin1", resources.getDrawable(R.drawable.skin1))
                    style.addImage("skin2", resources.getDrawable(R.drawable.skin2))
                    style.addImage("skin3", resources.getDrawable(R.drawable.skin3))}

                init_users()
                start_refresh_screen()
                Toast.makeText( this, "yoyo", Toast.LENGTH_LONG).show()
                ShowEventWindow()
                SendHttpRequest()



            }
        }
    }

    private fun ShowEventWindow()
    {
        val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Inflate a custom view using layout inflater
        val view = inflater.inflate(R.layout.pop_event,null)

        // Initialize a new instance of popup window
        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        )

        popupWindow.showAtLocation(mapView
            ,
            1,
            0,0)
    }

    private fun SendHttpRequest(){

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val url = "https://www.google.com"
        val textView = findViewById<TextView>(R.id.text_view)

// Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                // Display the first 500 characters of the response string.
                Toast.makeText( this, "Response is: ${response.substring(0, 500)}", Toast.LENGTH_LONG).show()
            },
            Response.ErrorListener { Toast.makeText( this, "no response", Toast.LENGTH_LONG).show() })

// Add the request to the RequestQueue.
        queue.add(stringRequest)
    }


    private fun start_refresh_screen(){

        update_user_data()
        Handler(Looper.getMainLooper()).postDelayed({
            start_refresh_screen()
        }, 1000)
    }

    private fun init_users()
    {
        var symbol = symbolManager.create(SymbolOptions()
            .withLatLng(LatLng(48.6938406181887, 6.183570629782477))
            .withIconImage("skin1")
            .withIconSize(1.3f)
            .withTextOpacity(0.0f)
            .withTextField("Jerome"))

        var user1 : User = User("abc", "Jerome", symbol)

        symbol = symbolManager.create(SymbolOptions()
            .withLatLng(LatLng(48.693262228886, 6.1831571692140965))
            .withIconImage("skin2")
            .withIconSize(1.3f)
            .withTextOpacity(0.0f)
            .withTextField("Jean"))

        var user2 : User = User("adc", "Jean", symbol)
        users.add(user1)
        users.add(user2)

        symbolManager.addClickListener(OnSymbolClickListener {
            clickedsymbol ->
            clickedsymbol.textOpacity=1.0f
            var point : PointF = PointF(0.0f,-1.5f)
            clickedsymbol.textOffset = point
            symbolManager.update(clickedsymbol)
            true
        })
    }

    private fun update_user_data()
    {
        for(user in users)
        {
            user.symbol.latLng= LatLng(user.symbol.latLng.latitude+0.0001, user.symbol.latLng.longitude)
            symbolManager.update(user.symbol)
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