package com.example.paymentgateway

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors


class CheckNetworkConnection (private val connectivityManager: ConnectivityManager):LiveData<NetworkState>() {
    constructor(application: Application) : this(application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)

    private val handler = Handler(Looper.getMainLooper())
    private val executorService = Executors.newSingleThreadExecutor()
    private val networkCallback = @RequiresApi(Build.VERSION_CODES.Q)


    object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            checkActualInternetConnection()

        }

        override fun onLost(network: Network) {
            super.onLost(network)
            postValue(NetworkState.DISCONNECTED)

        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            checkActualInternetConnection()
        }

    }

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()
        val builder = NetworkRequest.Builder()
        connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
        checkActualInternetConnection()
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun checkActualInternetConnection() {

        if (isNetworkAvailable()) {

            executorService.execute {
                val isConnected = hasInternetAccess()
                handler.post {
                    if (isConnected) {
                        postValue(NetworkState.CONNECTED)
                    } else {
                        postValue(NetworkState.DISCONNECTED)
                        postValue(NetworkState.NO_INTERNET)

                    }

                }

            }
        } else {
            postValue(NetworkState.DISCONNECTED)
        }
    }

    @SuppressLint("MissingPermission")
    private fun isNetworkAvailable(): Boolean {
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities != null
    }

    private fun hasInternetAccess(): Boolean {
        try {

            val urlConnection = URL("https://8.8.8.8").openConnection() as HttpURLConnection
            urlConnection.setRequestProperty("User-Agent", "Android")
            urlConnection.setRequestProperty("Connection", "close")
            urlConnection.connectTimeout = 500
            urlConnection.connect()
            return urlConnection.responseCode == 200
        } catch (e: IOException) {
            return false
        }
    }
}


