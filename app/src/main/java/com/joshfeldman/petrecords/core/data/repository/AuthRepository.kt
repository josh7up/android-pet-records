package com.joshfeldman.petrecords.core.data.repository

import com.joshfeldman.petrecords.core.data.auth.AuthTokenStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class AuthRepository @Inject constructor(
    private val tokenStore: AuthTokenStore,
) {
    val token: Flow<String> = tokenStore.token

    suspend fun saveToken(value: String) {
        tokenStore.saveToken(value)
    }
}
