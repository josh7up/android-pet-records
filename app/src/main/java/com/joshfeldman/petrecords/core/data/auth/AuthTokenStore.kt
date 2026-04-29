package com.joshfeldman.petrecords.core.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.authDataStore by preferencesDataStore(name = "auth")

@Singleton
class AuthTokenStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val tokenKey = stringPreferencesKey("jwt_token")

    val token: Flow<String> = context.authDataStore.data.map { preferences ->
        preferences[tokenKey].orEmpty()
    }

    fun currentToken(): String = runBlocking {
        token.first().trim()
    }

    suspend fun saveToken(value: String) {
        context.authDataStore.edit { preferences ->
            preferences[tokenKey] = value.trim()
        }
    }
}
