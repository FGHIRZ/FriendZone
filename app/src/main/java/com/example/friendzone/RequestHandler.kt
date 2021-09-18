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
import java.math.BigInteger
import java.security.MessageDigest

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
        user_json.put("password", md5(password))
        json_request.put("request", "login")
        json_request.put("params", user_json)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, server_url, json_request,
            { response ->
                Log.d("requestHandler", response.toString())
                if((response.get("status") as String) == "ok") {
                    Toast.makeText(activity, response.get("status") as String, Toast.LENGTH_SHORT).show()
                    val user_id = ((response.get("params") as JSONObject).get("user_id")) as Int
                    val skin = ((response.get("params") as JSONObject).get("skin")) as String

                    val user = User(user_id)
                    user.username = username
                    user.skin = skin
                    (activity as Login).startMapActivity(user)
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

    fun requestAccountCreation(username: String, password: String, activity: Activity)
    {
        val json = JSONObject()
        val userJSON= JSONObject()
        userJSON.put("name", username)
        userJSON.put("password", md5(password))
        json.put("request", "create_account")
        json.put("params", userJSON)

        val createAccountRequest= JsonObjectRequest(Request.Method.POST, server_url, json, {response->
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
        createAccountRequest.setRetryPolicy(
            DefaultRetryPolicy(
                4000,
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))

        queue.add(createAccountRequest)
    }

    fun requestUserList(location : Location, user : User, visibile : Boolean, activity: Activity){

        // Request a string response from the provided URL.

        val json= JSONObject()
        val locationJSON = JSONObject()
        locationJSON.put("lat", location.latitude)
        locationJSON.put("lon", location.longitude)
        val userJSON = JSONObject()
        userJSON.put("user_id", user.id)
        userJSON.put("location", locationJSON)
        userJSON.put("visible", visibile)
        json.put("request", "update")
        json.put("params",userJSON)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, server_url, json,
            { response ->
                Log.d("requestPage", response.toString())
                val userList = response.getJSONObject("params").getJSONArray("user_list")
                (activity as MainActivity).updateUserList(userList)
            },
            { })
        queue.add(jsonObjectRequest)
    }

    fun md5(input:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }
}