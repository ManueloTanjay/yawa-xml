package com.example.yawa

import GetViewerQuery
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.apollographql.apollo3.network.http.HttpNetworkTransport
import kotlinx.android.synthetic.main.fragment_main_list.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking


class MainActivity : FragmentActivity() {

    val logInFragment = LogInFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //set to login by default
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.login, logInFragment)
            commit()
        }

        //get url with session token and bearer from intent
        val action: String? = intent?.action;
        val data: Uri? = intent?.data;
        //get session token from url
        var session_token: String? = data?.fragment?.split("=")?.get(1)?.split("&")?.get(0);
        var session_token_expiry = data?.fragment?.split("=")?.get(3)


        //shared preferences for session token
        val sharedPreferences = getSharedPreferences("yawa", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        runBlocking {
            //check if session token exists
            if (!(session_token === null)) {
                if (!sharedPreferences.contains("session_token")) {

                    //get userInfo
                    val userInfo = getUserInfo(session_token.toString())

                    editor.apply {
                        putString("session_token", session_token)
                        putString("session_token_expiry", session_token_expiry)
                        putString("username", userInfo?.username)
                        putString("userID", userInfo?.userID)
                        apply()
                    }
                }
            }
            if (sharedPreferences.contains("session_token")) {
                session_token = sharedPreferences.getString("session_token", null)

                //create and inflate MainListFragment
                val mainListFragment = MainListFragment.newInstance(session_token)
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.login, mainListFragment)
                    commit()
                }
            }
        }
    }

    //function to get userID and username using GetViewer and pass into MainListFragment.newInstance()
    suspend fun getUserInfo(session_token: String): User? {
        var userInfo = User()

        val apolloClient = ApolloClient(
                networkTransport = HttpNetworkTransport(
                        serverUrl = "https://graphql.anilist.co/",
                        interceptors = listOf(MainListFragment.AuthorizationInterceptor(session_token))
                )
        )

        val response = try {
            apolloClient.query(GetViewerQuery())
        } catch (e: ApolloException) {
            Log.d("GETUSERINFO", e.toString())
            return userInfo
        }

        val launch = response.data?.viewer
        if (launch == null || response.hasErrors()) {
            return userInfo
        }

        userInfo?.username = launch.name
        userInfo?.userID = launch.id.toString()

        Log.d("DEEZ NUTS", "QQQQQ ${launch.id} ${launch.name}")

        return userInfo
    }
}