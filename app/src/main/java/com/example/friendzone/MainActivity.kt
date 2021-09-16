package com.example.friendzone

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    private val maphandler : MapHandler = MapHandler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button : Button = findViewById(R.id.button)
        requestHandler.initialize(this)
        textview = findViewById(R.id.textView)

        button.setOnClickListener {
            requestHandler.requestLogin("FGHIRZ", "lasalade", this)
            maphandler.initMap(this, savedInstanceState)
            startMap()
            /*val intent : Intent = Intent(this, Login::class.java).apply {
                putExtra(EXTRA_MESSAGE, "yoyo")
            }
            getLogin.launch(intent)*/
        }
    }

    fun test()
    {
        textview.setText("Alain Ouakbar")
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
    }


    private fun startMap() {
        setContentView(R.layout.activity_map)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            maphandler.enableLocationComponent(maphandler.mapboxMap.style!!)
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        maphandler.mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        maphandler.mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        maphandler.mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        maphandler.mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        maphandler.mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        maphandler.mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        maphandler.mapView?.onDestroy()

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
}