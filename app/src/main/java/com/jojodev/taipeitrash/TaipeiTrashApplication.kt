package com.jojodev.taipeitrash

import android.app.Application

class TaipeiTrashApplication : Application() {
    companion object {
        lateinit var instance: TaipeiTrashApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}