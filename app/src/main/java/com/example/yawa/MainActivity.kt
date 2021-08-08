package com.example.yawa

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity


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
        //check if session token exists
        if(!(session_token === null)) {
            val bundle = Bundle()
            bundle.putString("session_token", session_token)

            val mainListFragment = MainListFragment.newInstance(session_token)
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.login, mainListFragment)
                commit()
            }
        }
    }
}