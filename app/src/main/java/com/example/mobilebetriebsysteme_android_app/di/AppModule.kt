package com.example.mobilebetriebsysteme_android_app.di

import android.app.Application
import android.content.Context
import com.example.mobilebetriebsysteme_android_app.bluetooth.AndroidBluetoothController
import com.example.mobilebetriebsysteme_android_app.bluetooth.BluetoothController
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

}

@Module
@InstallIn(SingletonComponent::class)
object ContextModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }
}
