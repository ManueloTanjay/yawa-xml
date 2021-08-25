package com.example.yawa

import GetViewerQuery
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.apollographql.apollo3.network.http.HttpNetworkTransport
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
        var sessionToken: String? = data?.fragment?.split("=")?.get(1)?.split("&")?.get(0);
        var sessionTokenExpiration = data?.fragment?.split("=")?.get(3)


        //shared preferences for session token
        val sharedPreferences = getSharedPreferences("yawa", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        runBlocking {
            //check if session token exists
            if (!(sessionToken === null)) {
                if (!sharedPreferences.contains("session_token")) {

                    //get userInfo
                    val userInfo = getUserInfo(sessionToken.toString())

                    editor.apply {
                        putString("session_token", sessionToken)
                        putString("session_token_expiry", sessionTokenExpiration)
                        putString("username", userInfo?.username)
                        putString("userID", userInfo?.userID)
                        apply()
                    }
                }
            }
            if (sharedPreferences.contains("session_token")) {
                sessionToken = sharedPreferences.getString("session_token", null)

                //create and inflate MainListFragment
                val mainListFragment = MainListFragment.newInstance(sessionToken)
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