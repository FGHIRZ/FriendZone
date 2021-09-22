package com.example.friendzone

import android.app.Activity
import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.mapbox.mapboxsdk.geometry.LatLng
import org.json.JSONObject
import java.math.BigInteger
import java.security.MessageDigest

class RequestHandler {

    private lateinit var queue: RequestQueue
    private val serverUrl = "http://82.165.223.209:8080/"


    fun initialize(context: Context) {
        queue = Volley.newRequestQueue(context)
    }


    fun requestLogin(username: String, password: String, activity : Activity) {

        val jsonRequest = JSONObject()
        val userJson = JSONObject()
        userJson.put("name", username)
        userJson.put("password", md5(password))
        jsonRequest.put("request", "login")
        jsonRequest.put("params", userJson)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, serverUrl, jsonRequest,
            { response ->
                Log.d("requestHandler", response.toString())
                if((response.get("status") as String) == "ok") {
                    Toast.makeText(activity, "You have been rickrolled", Toast.LENGTH_LONG).show()

                    val userId = response.getJSONObject("params").getInt("user_id")
                    val skin = response.getJSONObject("params").getString("skin")
                    val pseudo = response.getJSONObject("params").getString("pseudo")

                    val user = User(userId)
                    user.username = username
                    user.skin = skin
                    user.pseudo = pseudo
                    (activity as Login).startMapActivity(user)
                }
                else
                {
                    (activity as Login).loginError()
                    Toast.makeText(activity, ((response.get("params") as JSONObject).get("description") as String), Toast.LENGTH_SHORT).show()
                }
            },
            { })
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            4000,
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(jsonObjectRequest)
    }

    fun requestAutoLogin(username: String, password: String, activity : Activity) {

        val jsonRequest = JSONObject()
        val userJson = JSONObject()
        userJson.put("name", username)
        userJson.put("password", password)
        jsonRequest.put("request", "login")
        jsonRequest.put("params", userJson)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, serverUrl, jsonRequest,
            { response ->
                Log.d("requestHandler", response.toString())
                if((response.get("status") as String) == "ok") {
                    Toast.makeText(activity, "Bienvenue maître", Toast.LENGTH_LONG).show()

                    val user = User(response.getJSONObject("params").getInt("user_id"))
                    user.skin = response.getJSONObject("params").getString("skin")
                    user.pseudo = response.getJSONObject("params").getString("pseudo")
                    (activity as Login).startMapActivity(user)
                }
                else
                {
                    (activity as Login).loginError()
                    Toast.makeText(activity, ((response.get("params") as JSONObject).get("description") as String), Toast.LENGTH_SHORT).show()
                }
            },
            { })
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            4000,
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(jsonObjectRequest)
    }

    fun requestAccountCreation(username: String, password: String, activity: Activity)
    {
        val json = JSONObject()
        val userJSON= JSONObject()
        userJSON.put("name", username)
        userJSON.put("password", md5(password))
        json.put("request", "create_account")
        json.put("params", userJSON)

        val createAccountRequest= JsonObjectRequest(Request.Method.POST, serverUrl, json, { response->
            Log.d("requestHandler", response.toString())
            if(response.getString("status") == "ok")
            {
                (activity as AccountCreation).success()
            }
            else
            {
                Toast.makeText(activity, response.getJSONObject("params").getString("description"), Toast.LENGTH_LONG).show()
            }
        }, {
        })
        createAccountRequest.retryPolicy = DefaultRetryPolicy(
            4000,
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        queue.add(createAccountRequest)
    }



    fun requestUserList(location : Location, user : User, visibile : Boolean, activity: Activity){

        // Request a string response from the provided URL.

        val json= JSONObject()
        val locationJSON = JSONObject()
        locationJSON.put("lat", location.latitude)
        locationJSON.put("lon", location.longitude)
        val userJSON = JSONObject()
        userJSON.put("user_id", user.user_id)
        userJSON.put("location", locationJSON)
        userJSON.put("visible", visibile)
        json.put("request", "get_user_list")
        json.put("params",userJSON)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, serverUrl, json,
            { response ->
                Log.d("requestPage", response.toString())
                val userList = response.getJSONObject("params").getJSONArray("user_list")
                (activity as MainActivity).updateUserList(userList)
            },
            { })
        queue.add(jsonObjectRequest)
    }

    fun requestEventList(location : Location, activity: Activity){

        // Request a string response from the provided URL.

        val json= JSONObject()
        val locationJSON = JSONObject()
        locationJSON.put("lat", location.latitude)
        locationJSON.put("lon", location.longitude)
        val userJSON = JSONObject()
        userJSON.put("location", locationJSON)
        json.put("request", "get_event_list")
        json.put("params",userJSON)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, serverUrl, json,
            { response ->
                Log.d("requestPage", response.toString())
                val eventList = response.getJSONObject("params").getJSONArray("event_list")
                (activity as MainActivity).updateEventList(eventList)
            },
            { })
        queue.add(jsonObjectRequest)
    }

    fun requestEventCreation(user_id : Int, type: String, location: LatLng, activity: Activity)
    {
        val requestJSON = JSONObject()
        requestJSON.put("request", "create_event")

        val paramsJSON= JSONObject()
        val locationJSON = JSONObject()
        locationJSON.put("lat", location.latitude)
        locationJSON.put("lon", location.longitude)
        paramsJSON.put("location", locationJSON)
        paramsJSON.put("user_id", user_id)
        paramsJSON.put("type", type)

        requestJSON.put("params", paramsJSON)


        Log.d("request", requestJSON.toString())

        val createEventRequest= JsonObjectRequest(Request.Method.POST, serverUrl, requestJSON, { response->
            Log.d("requestHandler", response.toString())
            if(response.getString("status") == "ok")
            {
                Toast.makeText(activity, response.getJSONObject("params").getString("description"), Toast.LENGTH_LONG).show()
            }
            else
            {
                Toast.makeText(activity, response.getJSONObject("params").getString("description"), Toast.LENGTH_LONG).show()
            }
        }, {
        })
        createEventRequest.retryPolicy = DefaultRetryPolicy(
            4000,
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        queue.add(createEventRequest)
    }

    fun requestUsernameChange(user_id: Int, new_username: String, password: String, activity: Activity) {
        val json = JSONObject()
        val userJSON= JSONObject()
        userJSON.put("user_id", user_id)
        userJSON.put("new_name", new_username)
        userJSON.put("password", password)
        json.put("request", "change_username")
        json.put("params", userJSON)

        val changeUsernameRequest = JsonObjectRequest(Request.Method.POST, serverUrl, json, { response->
            Log.d("requestHandler", response.toString())
            if(response.getString("status") == "ok")
            {
                (activity as ChangeUsername).success()
            }
            else
            {
                Toast.makeText(activity, response.getJSONObject("params").getString("description"), Toast.LENGTH_LONG).show()
            }
        }, {
        })
        changeUsernameRequest.retryPolicy = DefaultRetryPolicy(
            4000,
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        queue.add(changeUsernameRequest)
    }

    fun requestPasswordChange(user_id: Int,username: String, password: String, new_password: String,activity: Activity) {

        val json = JSONObject()
        val userJSON= JSONObject()

        userJSON.put("user_id", user_id)
        userJSON.put("username", username)
        userJSON.put("password", password)
        userJSON.put("new_password", new_password)
        json.put("request", "change_password")
        json.put("params", userJSON)

        val changePasswordRequest= JsonObjectRequest(Request.Method.POST, serverUrl, json, { response->
            Log.d("requestHandler", response.toString())
            if(response.getString("status") == "ok")
            {
                (activity as ChangePassword).success()
            }
            else
            {
                Toast.makeText(activity, response.getJSONObject("params").getString("description"), Toast.LENGTH_LONG).show()
            }
        }, {
        })
        changePasswordRequest.retryPolicy = DefaultRetryPolicy(
            4000,
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        queue.add(changePasswordRequest)
    }

    fun requestAccountDeletion(user_id: Int,username: String, password: String, activity: Activity) {
        val json = JSONObject()
        val userJSON= JSONObject()
        userJSON.put("user_id", user_id)
        userJSON.put("username", username)
        userJSON.put("password", password)
        json.put("request", "delete_account")
        json.put("params", userJSON)

        val deleteAccountRequest = JsonObjectRequest(Request.Method.POST, serverUrl, json, { response->
            Log.d("requestHandler", response.toString())
            if(response.getString("status") == "ok")
            {
                (activity as DeleteAccount).success()
            }
            else
            {
                Toast.makeText(activity, response.getJSONObject("params").getString("description"), Toast.LENGTH_LONG).show()
            }
        }, {
        })
        deleteAccountRequest.retryPolicy = DefaultRetryPolicy(
            4000,
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        queue.add(deleteAccountRequest)
    }

    fun md5(input:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }
}