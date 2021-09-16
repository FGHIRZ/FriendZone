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
import org.json.JSONArray
import org.json.JSONObject

class RequestHandler {

    private lateinit var queue: RequestQueue
    private val server_url = "http://82.165.223.209:8080/"


    fun initialize(context: Context) {
        queue = Volley.newRequestQueue(context)
    }

    fun requestLogin(username: String, password: String, activity : Activity) {

        val user : User

        val json_request = JSONObject()
        val user_json = JSONObject()
        user_json.put("name", username)
        user_json.put("password", password)
        json_request.put("request", "login")
        json_request.put("params", user_json)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, server_url, json_request,
            { response ->
                if((response.get("status") as String) == "ok") {
                    Toast.makeText(activity, response.get("status") as String, Toast.LENGTH_SHORT).show()
                    val user_id = ((response.get("params") as JSONObject).get("user_id")) as Int
                    val skin = ((response.get("params") as JSONObject).get("skin")) as String

                    val user = User(user_id)
                    user.username = username
                    user.skin = skin
                    (activity as Login).returnLogin(user)
                }
                else
                {
                    Toast.makeText(activity, ((response.get("params") as JSONObject).get("description") as String), Toast.LENGTH_SHORT).show()
                }
            },
            { })
        jsonObjectRequest.setRetryPolicy(
            DefaultRetryPolicy(
                4000,
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        )
        queue.add(jsonObjectRequest)
    }

    private fun requestCreateAccount(username: String, password: String, activity: Activity)
    {
        val json = JSONObject()
        val userJSON= JSONObject()
        userJSON.put("name", username)
        userJSON.put("password", password)
        json.put("request", "create_account")
        json.put("params", userJSON)

        val createAccountRequest= JsonObjectRequest(Request.Method.POST, server_url, json, {response->
            if(response.get("status") as String == "ok")
            {
                Toast.makeText(activity, "ahha", Toast.LENGTH_SHORT).show()
            }
            else
            {
            }
        }, {
        })
        createAccountRequest.setRetryPolicy(
            DefaultRetryPolicy(
                4000,
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))

        queue.add(createAccountRequest)
    }

    private fun requestUserList(location : Location, user : User, activity: Activity){

        // Request a string response from the provided URL.

        val json= JSONObject()
        val locationJSON = JSONObject()
        locationJSON.put("lat", location.latitude)
        locationJSON.put("lon", location.longitude)
        val userJSON = JSONObject()
        userJSON.put("user_id", user.id)
        userJSON.put("location", locationJSON)
        json.put("request", "update")
        json.put("params",userJSON)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, server_url, json,
            { response ->

            },
            { })
        queue.add(jsonObjectRequest)
    }
}