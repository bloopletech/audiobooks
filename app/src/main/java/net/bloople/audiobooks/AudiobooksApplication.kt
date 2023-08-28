package net.bloople.audiobooks

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.StrictMode

class AudiobooksApplication: Application() {
    init {
        if(BuildConfig.DEBUG) StrictMode.enableDefaults()
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set
    }
}