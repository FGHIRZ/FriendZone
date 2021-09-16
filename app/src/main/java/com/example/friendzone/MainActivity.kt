package com.example.friendzone

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager

class MainActivity : AppCompatActivity(), PermissionsListener {

    private lateinit var textview : TextView
    private val requestHandler = RequestHandler()
    var permissionsManager: PermissionsManager = PermissionsManager(this)

    private lateinit var symbolManager : SymbolManager
    var mapView: MapView? = null
    lateinit var mapboxMap: MapboxMap
    private lateinit var activity : Activity
    private var savedInstance : Bundle? = null

    private var mapUrl = "mapbox://styles/meetgameproject/ckt8pxo7y28vs19v1qlyjrk8v"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestHandler.initialize(this)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.mapView);
        mapView?.onCreate(savedInstanceState);

        login()
    }

    private fun login() {
        var getLogin = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                startMap()
            }
        }
        val intent: Intent = Intent(this, Login::class.java).apply {
            putExtra(EXTRA_MESSAGE, "yoyo")
            getLogin.launch(intent)
        }
        startActivity(intent)
    }

    private fun startMap()
    {
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

    /*
    private fun refreshMap(){
        val location = MapHandler.mapboxMap.locationComponent.lastKnownLocation!!
        requestHandler.requestUserList(location)
        Handler(Looper.getMainLooper()).postDelayed({
            startScreenRefresh()
        }, 10000)
    }*/

    /*
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
*/
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
