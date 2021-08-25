package com.example.yawa

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_main_list.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.api.http.withHeader
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import com.apollographql.apollo3.network.http.HttpNetworkTransport
import kotlinx.android.synthetic.main.fragment_main_list.view.*
import kotlinx.coroutines.runBlocking

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private var sessionToken = ""
private var username = ""
private var userID = ""
private var sessionTokenExpiry = ""

/**
 * A simple [Fragment] subclass.
 * Use the [MainListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }

        //shared preferences for session token
        val sharedPreferences = activity?.getSharedPreferences("yawa", FragmentActivity.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        username = sharedPreferences?.getString("username", null).toString()
        userID = sharedPreferences?.getString("userID", null).toString()
        sessionTokenExpiry = sharedPreferences?.getString("session_token_expiry", null).toString()


        val s = sessionToken
        val i = 0

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString("session_token")?.let {
            sessionToken = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //fetch and change value of resource from layout
        view.sTokenTV.text = sessionToken

        //val apolloClient = ApolloClient(serverUrl = "https://graphql.anilist.co/")
        val apolloClient = ApolloClient(
                networkTransport = HttpNetworkTransport(
                        serverUrl = "https://graphql.anilist.co/",
                        interceptors = listOf(AuthorizationInterceptor(sessionToken))
                )
        )
        runBlocking {
            view.sTokenTV.text = "username: " + username + "\nuserID: " + userID + "\nsession token expiration: " + sessionTokenExpiry + "\nstoken: " + sessionToken
                    Log.d("DEEZ NUTS", "QQQQQ $userID $username")
            /////////////////////////////////////////////
//            val response2 = try {
//                apolloClient.query(GetUserMediaListOptionsQuery(userID.toInt()))
//            } catch (e: ApolloException) {
//                Log.d("qwerty", e.toString())
//                val toast = Toast.makeText(context, e.toString(), Toast.LENGTH_LONG)
//                toast.show()
//                return@runBlocking
//            }
//            val launch2 = response2.data?.user
//            if (launch2 == null || response2.hasErrors()) {
//                return@runBlocking
//            }
//
//            var op = launch2.mediaListOptions?.scoreFormat?.rawValue
//
//            //view.s_token.text = "username: " + username + "\nuserID: " + userID
//            Log.d("2ND QUERY", "QQQQQ" + op + "\n")
        }
    }

    //write function to get user mediaListOptions using GetUserMediaListOptions

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String?) =
            MainListFragment().apply {
                arguments = Bundle().apply {
                    putString("session_token", param1)
                }
            }
    }

    class AuthorizationInterceptor(val token: String) : HttpInterceptor {
        override suspend fun intercept(request: HttpRequest,  chain: HttpInterceptorChain): HttpResponse {
            return chain.proceed(request.withHeader("Authorization", "Bearer $token"))
        }
    }
}