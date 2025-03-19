package com.example.paymentgateway

import android.app.Application
import android.content.Intent
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {


    override fun onCreate() {
        super.onCreate()

        // Set up the global exception handler
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleUncaughtException(thread, throwable)
        }
    }

    private fun handleUncaughtException(thread: Thread, throwable: Throwable) {
        // Log the crash
        Log.e("AppCrash", "Uncaught exception: ", throwable)

        // Launch the CrashActivity
        val intent = Intent(this, CrashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)

        // Kill the process to prevent the app from hanging
        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(1)
    }

}