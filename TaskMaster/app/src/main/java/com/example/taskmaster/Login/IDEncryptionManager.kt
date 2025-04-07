package com.example.taskmaster.APIService

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.IOException
import java.security.GeneralSecurityException

class IDEncryptionManager(private val context: Context) {

    private val masterKeyAlias = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "MyAppPrefs",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: GeneralSecurityException) {
        throw IllegalStateException("Error initializing EncryptedSharedPreferences", e)
    } catch (e: IOException) {
        throw IllegalStateException("Error initializing EncryptedSharedPreferences", e)
    }

    fun saveID(clientID: String) {
        with(sharedPreferences.edit()) {
            putString("clientID", clientID)
            apply()
        }
        println("El ID guardado es: $clientID")
    }

    fun getID(): String? {
        return sharedPreferences.getString("clientID", "")
    }
}