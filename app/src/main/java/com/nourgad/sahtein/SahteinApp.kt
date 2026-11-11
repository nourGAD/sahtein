package com.nourgad.sahtein

import android.app.Application
import com.nourgad.sahtein.notify.Alarms

class SahteinApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Alarms.createChannel(this)
    }
}
