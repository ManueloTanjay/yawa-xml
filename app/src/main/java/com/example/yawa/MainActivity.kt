package com.example.yawa

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity


class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get url with session token and bearer from intent
        val action: String? = intent?.action;
        val data: Uri? = intent?.data;
        //get session token from url
        var session_token: String? = data?.fragment?.split("=")?.get(1)?.split("&")?.get(0);
        //check if session token exists
        if(!(session_token === null)) {
            val tv_s_token = findViewById<View>(R.id.token_text) as TextView;
            tv_s_token.text = tv_s_token.text.toString() + session_token;


        } else {

        }

        //button
        val b_login = findViewById<View>(R.id.login_button) as Button
        b_login.setOnClickListener {
            val i = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://anilist.co/api/v2/oauth/authorize?client_id=5828&response_type=token")
            )
            startActivity(i);
        };
    }
}