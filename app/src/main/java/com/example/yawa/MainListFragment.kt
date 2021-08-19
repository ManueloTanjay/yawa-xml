package com.example.yawa

import GetCurrentAuthenticatedUserQuery
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_main_list.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import kotlinx.android.synthetic.main.fragment_main_list.view.*
import kotlinx.coroutines.runBlocking

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private var session_token = ""

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

        val s = session_token
        val i = 0

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString("session_token")?.let {
            session_token = it
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
        view.s_token.text = session_token

        val apolloClient = ApolloClient(serverUrl = "https://graphql.anilist.co/")
        runBlocking {
            val response = try {
                apolloClient.query(GetCurrentAuthenticatedUserQuery())
            } catch (e: ApolloException) {
                val toast = Toast.makeText(context, e.toString(), Toast.LENGTH_LONG)
                return@runBlocking
            }

            val launch = response.data?.media
            if (launch == null || response.hasErrors()) {
                return@runBlocking
            }

            println("Launch site: ${launch.title}")
            Log.d("TAG", "QQQQQ ${launch.title}")
        }
    }

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
}