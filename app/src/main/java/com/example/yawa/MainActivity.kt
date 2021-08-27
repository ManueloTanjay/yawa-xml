package com.example.yawa

import GetUserMediaListOptionsQuery
import GetViewerQuery
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
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
            if (!(sessionToken === null) && !sharedPreferences.contains("session_token")) {
                //get userInfo
                val userInfo = getUserInfo(sessionToken.toString())

                editor.apply {
                    putString("session_token", sessionToken)
                    putString("session_token_expiry", sessionTokenExpiration)
                    putString("username", userInfo?.username)
                    putString("userID", userInfo?.userID)
                    putString("user_media_list_options", userInfo?.userMediaListOptions)
                    apply()
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

    //function to get userID, username, mediaListOptions using GetViewer and GetUserMediaListOptions and pass into MainListFragment.newInstance()
    suspend fun getUserInfo(session_token: String): User? {
        var userInfo = User()

        val apolloClient = ApolloClient(
                networkTransport = HttpNetworkTransport(
                        serverUrl = "https://graphql.anilist.co/",
                        interceptors = listOf(MainListFragment.AuthorizationInterceptor(session_token))
                )
        )
        //get username and userID
        val viewerQueryResponse = try {
            apolloClient.query(GetViewerQuery())
        } catch (e: ApolloException) {
            Log.d("GETUSERINFO", e.toString())
            return userInfo
        }

        val viewer = viewerQueryResponse.data?.viewer
        if (viewer == null || viewerQueryResponse.hasErrors())
            return userInfo

        userInfo?.username = viewer.name
        userInfo?.userID = viewer.id.toString()
        Log.d("DEEZ NUTS", "QQQQQ ${viewer.id} ${viewer.name}")
        //get userMediaListOptions
        val userMediaListOptionsResponse = try {
            apolloClient.query(GetUserMediaListOptionsQuery(userInfo?.userID.toInt()))
        } catch (e: ApolloException) {
            Log.d("GETUSERMEDISLISTOPTIONS", e.toString())
            return userInfo
        }

        val user = userMediaListOptionsResponse.data?.user
        if (user == null || userMediaListOptionsResponse.hasErrors())
            return userInfo

        userInfo.userMediaListOptions = user.mediaListOptions?.scoreFormat?.rawValue.toString()
        return userInfo
    }
}