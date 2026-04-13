package com.example.mobilebetriebsysteme_android_app.bluetooth

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BluetoothModule {

    @Binds
    @Singleton
    abstract fun bindBluetoothController(
        androidBluetoothController: AndroidBluetoothController
    ): BluetoothController
}

