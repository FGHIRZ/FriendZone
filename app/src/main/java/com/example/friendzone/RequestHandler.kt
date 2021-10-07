package com.example.friendzone

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.auth0.android.jwt.JWT
import com.mapbox.mapboxsdk.geometry.LatLng
import org.json.JSONObject
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.ExecutionException


class RequestHandler {

    private lateinit var queue: RequestQueue
    val serverUrl = "https://www.meetgames.fr:8080/"

    var accessToken = ""
    var refreshToken = ""

    var userId = 0

    private var activity = Activity()

    fun initialize(context: Context, sharedPreferences: SharedPreferences) {
        queue = Volley.newRequestQueue(context)
        accessToken = sharedPreferences.getString("ACCESS_TOKEN", "null")!!
        refreshToken = sharedPreferences.getString("REFRESH_TOKEN", "null")!!
        userId = sharedPreferences.getInt("USER_ID", 0)
        this.activity = activity
    }

    fun requestAccountCreation(username: String, password: String, activity: Activity)
    {
        val json = JSONObject()
        val userJSON= JSONObject()
        userJSON.put("username", username)
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

    fun requestLogin(username: String, password: String, activity : Activity) {

        Log.d("INTERNET", "doing the request")

        val jsonRequest = JSONObject()
        val userJson = JSONObject()
        userJson.put("username", username)
        userJson.put("password", md5(password))
        jsonRequest.put("request", "login")
        jsonRequest.put("params", userJson)

        val requestUrl = serverUrl + "login"


        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, requestUrl, jsonRequest,
            { response ->
                Log.d("INTERNET", response.toString())
                if((response.get("status") as String) == "ok") {
                    userId = response.getJSONObject("params").getInt("user_id")
                    refreshToken = response.getJSONObject("params").getString("refresh_token")
                    (activity as Login).loginSuccess(userId, refreshToken)
                }
                else
                {
                    (activity as Login).loginError()
                    Toast.makeText(activity, ((response.get("params") as JSONObject).get("description") as String), Toast.LENGTH_SHORT).show()
                }
            },
            {
                Log.d("INTERNET", it.message.toString())
            })
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            4000,
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(jsonObjectRequest)
    }

    fun requestAccessToken(){

        val future = RequestFuture.newFuture<JSONObject>()

        val json= JSONObject()
        val paramsJSON = JSONObject()
        paramsJSON.put("user_id", userId)
        json.put("request", "get_access_token")
        json.put("params",paramsJSON)

        val requestURL = serverUrl + "get_access_token"

        val accessTokenRequest : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, requestURL, json, future, future) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                var params = HashMap(super.getHeaders())
                params.put("Authorization", "Bearer " + refreshToken)
                return params
            }
        }

        accessTokenRequest.retryPolicy = DefaultRetryPolicy(
            4000,
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        queue.add(accessTokenRequest)

        try {
            Log.d("INTERNET", "callback ?")
            val response : JSONObject = future.get()
            if(response.getString("status") == "ok")
            {
                 accessToken = response.getJSONObject("params").getString("access_token")
                 Log.d("INTERNET", "message reception token")
            }
        }
        catch (e : InterruptedException)
        {

        }
        catch (e : ExecutionException)
        {

        }
    }


    fun requestClientInfo(userId : Int)
    {
        Log.d("INTERNET", "message début de boucle")
        verify_access_token()
        Log.d("INTERNET", "message fin de boucle")
        val jsonRequest = JSONObject()
        val userJson = JSONObject()
        userJson.put("user_id", userId)
        jsonRequest.put("request", "get_my_info")
        jsonRequest.put("params", userJson)

        val requestURL = serverUrl + "app"

        val accessTokenRequest : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, requestURL, jsonRequest,
            { response ->
                Log.d("JWT", response.toString())
                if((response.get("status") as String) == "ok") {
                    Toast.makeText(activity, "You have been rickrolled", Toast.LENGTH_LONG).show()

                    val userId = response.getJSONObject("params").getInt("user_id")
                    val skin = response.getJSONObject("params").getString("user_skin")
                    val pseudo = response.getJSONObject("params").getString("user_pseudo")
                    val user = User(userId)
                    user.skin = skin
                    user.pseudo = pseudo
                    (activity as Login).startMapActivity(user)
                }
                else
                {
                    (activity as Login).loginError()
                    Toast.makeText(activity, ((response.get("params") as JSONObject).get("description") as String), Toast.LENGTH_SHORT).show()
                }
            }, { }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                var params = HashMap(super.getHeaders())
                params.put("Authorization", "Bearer " + accessToken)
                return params
            }
        }

        accessTokenRequest.retryPolicy = DefaultRetryPolicy(
            4000,
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        accessTokenRequest
        queue.add(accessTokenRequest)
    }


    fun requestUserList(location : Location, user : User, visibile : Boolean, activity: Activity){

        // Request a string response from the provided URL.

        val json= JSONObject()
        val locationJSON = JSONObject()
        locationJSON.put("lat", location.latitude)
        locationJSON.put("lon", location.longitude)
        val userJSON = JSONObject()
        userJSON.put("user_id", user.user_id)
        userJSON.put("user_location", locationJSON)
        userJSON.put("visible", visibile)
        json.put("request", "get_user_list")
        json.put("params",userJSON)

        val requestURL = serverUrl + "app"

        val accessTokenRequest : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, requestURL, json,
            { response ->
                Log.d("requestPage", response.toString())
                val userList = response.getJSONObject("params").getJSONArray("user_list")
                (activity as MainActivity).updateUserList(userList)
            }, { }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                var params = HashMap(super.getHeaders())
                params.put("Authorization", "Bearer " + accessToken)
                return params
            }
        }

        accessTokenRequest.retryPolicy = DefaultRetryPolicy(
            4000,
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)


        queue.add(accessTokenRequest)
    }

    fun requestEventList(location : Location, activity: Activity){

        // Request a string response from the provided URL.

        val json= JSONObject()
        val locationJSON = JSONObject()
        locationJSON.put("lat", location.latitude)
        locationJSON.put("lon", location.longitude)
        val userJSON = JSONObject()
        userJSON.put("user_location", locationJSON)
        json.put("request", "get_event_list")
        json.put("params",userJSON)

        val requestURL = serverUrl + "app"

        val accessTokenRequest : JsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, requestURL, json,
            { response ->
                Log.d("requestPage", response.toString())
                val eventList = response.getJSONObject("params").getJSONArray("event_list")
                (activity as MainActivity).updateEventList(eventList)
            },
            { }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                var params = HashMap(super.getHeaders())
                params.put("Authorization", "Bearer " + accessToken)
                return params
            }
        }
        accessTokenRequest.retryPolicy = DefaultRetryPolicy(
            4000,
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)


        queue.add(accessTokenRequest)
    }

    fun requestEventCreation(user_id : Int, type: String, location: LatLng, activity: Activity)
    {
        val json = JSONObject()
        json.put("request", "create_event")

        val paramsJSON= JSONObject()
        val locationJSON = JSONObject()
        locationJSON.put("lat", location.latitude)
        locationJSON.put("lon", location.longitude)
        paramsJSON.put("event_location", locationJSON)
        paramsJSON.put("user_id", user_id)
        paramsJSON.put("event_type", type)

        json.put("params", paramsJSON)


        val requestURL = serverUrl + "app"

        val accessTokenRequest : JsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, requestURL, json,
            { response->
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
        {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                var params = HashMap(super.getHeaders())
                params.put("Authorization", "Bearer " + accessToken)
                return params
            }
        }

        accessTokenRequest.retryPolicy = DefaultRetryPolicy(
            4000,
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        queue.add(accessTokenRequest)
    }


    fun requestUsernameChange(user_id: Int, new_username: String, password: String, activity: Activity) {
        val json = JSONObject()
        val userJSON= JSONObject()
        userJSON.put("user_id", user_id)
        userJSON.put("new_username", new_username)
        userJSON.put("password", password)
        json.put("request", "change_username")
        json.put("params", userJSON)

        val requestURL = serverUrl + "manage_account"

        val changeUsernameRequest = JsonObjectRequest(Request.Method.POST, requestURL, json, { response->
            Log.d("requestHandler", response.toString())
            if(response.getString("status") == "ok")
            {
                (activity as ChangeUsername).success(new_username)
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

        val requestURL = serverUrl + "manage_account"

        val changePasswordRequest= JsonObjectRequest(Request.Method.POST, requestURL, json, { response->
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

        val requestURL = serverUrl + "manage_account"
        val deleteAccountRequest = JsonObjectRequest(Request.Method.POST, requestURL, json, { response->
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

    fun requestPseudoChange(pseudo: String?, user_id: Int, activity: Activity) {
        val json = JSONObject()
        val userJSON= JSONObject()
        userJSON.put("user_id", user_id)
        userJSON.put("user_pseudo", pseudo)

        json.put("request", "change_pseudo")
        json.put("params", userJSON)

        val requestURL = serverUrl + "app"
        val changeSkinRequest = object : JsonObjectRequest(Request.Method.POST, requestURL, json, { response->
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
        {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                var params = HashMap(super.getHeaders())
                params.put("Authorization", "Bearer " + accessToken)
                return params
            }
        }

        changeSkinRequest.retryPolicy = DefaultRetryPolicy(
            4000,
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        queue.add(changeSkinRequest)
    }

    fun requestSkinList(activity : Activity)
    {
        val url = serverUrl + "skins/"
        Log.d("YOLO", "CA MARCHE OU PAS")
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                val skinList=JSONObject(response)
                this.requestEventList(activity, skinList)
            },
            {  })
// Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    fun requestEventList(activity : Activity, skinList : JSONObject)
    {
        val url = serverUrl + "events/"
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                val eventList=JSONObject(response)
                (activity as CacheActivity).loadSkinImages(skinList, eventList)
            },
            {  })
// Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    fun md5(input:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    fun check_access_token() : Boolean
    {
        val now = Date()
        if(accessToken == "null")
        {
            return false
        }
        else
        {
            val jwt = JWT(accessToken)
            if(jwt.expiresAt!! < now)
            {
                return false
            }
            return true
        }
    }

    fun verify_access_token()
    {
        if(!check_access_token())
        {
            requestAccessToken()
        }
    }

    fun requestSkinChange(selectedSkin: String?, user_id: Int, activity: Activity) {
        val json = JSONObject()
        val userJSON= JSONObject()
        userJSON.put("user_id", user_id)
        userJSON.put("user_skin", selectedSkin)

        json.put("request", "change_skin")
        json.put("params", userJSON)


        val requestURL = serverUrl + "app"
        val changeSkinRequest = object : JsonObjectRequest(
            Request.Method.POST, requestURL, json, { response->
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
        {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                var params = HashMap(super.getHeaders())
                params.put("Authorization", "Bearer " + accessToken)
                return params
            }
        }
        changeSkinRequest.retryPolicy = DefaultRetryPolicy(
            4000,
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        queue.add(changeSkinRequest)
    }
}