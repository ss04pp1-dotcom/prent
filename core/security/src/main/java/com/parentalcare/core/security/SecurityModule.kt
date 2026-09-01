package com.parentalcare.core.security

import com.parentalcare.core.security.crypto.ScreenshotEncryptor
import com.parentalcare.core.security.keystore.KeystoreManager
import com.parentalcare.core.security.pairing.PairingTokenFactory
import com.parentalcare.core.security.pairing.PairingSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides @Singleton
    fun provideKeystoreManager(@ApplicationContext ctx: android.content.Context): KeystoreManager =
        KeystoreManager(ctx)

    @Provides @Singleton
    fun provideEncryptor(ks: KeystoreManager): ScreenshotEncryptor =
        ScreenshotEncryptor(ks)

    @Provides @Singleton
    fun providePairingFactory(): PairingTokenFactory = PairingTokenFactory()

    @Provides @Singleton
    fun providePairingSerializer(): PairingSerializer = PairingSerializer()
}
