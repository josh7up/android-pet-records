package com.joshfeldman.petrecords.core.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.joshfeldman.petrecords.BuildConfig
import com.joshfeldman.petrecords.core.data.local.AppDatabase
import com.joshfeldman.petrecords.core.data.local.dao.PendingUploadDao
import com.joshfeldman.petrecords.core.data.local.dao.PetDao
import com.joshfeldman.petrecords.core.data.local.dao.SearchVisitDao
import com.joshfeldman.petrecords.core.data.local.dao.WeightDao
import com.joshfeldman.petrecords.core.network.AuthInterceptor
import com.joshfeldman.petrecords.core.network.PetRecordsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BASIC) })
        .build()

    @Provides
    @Singleton
    fun provideApi(client: OkHttpClient): PetRecordsApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PetRecordsApi::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "pet-records.db",
    ).build()

    @Provides fun providePetDao(database: AppDatabase): PetDao = database.petDao()
    @Provides fun provideSearchVisitDao(database: AppDatabase): SearchVisitDao = database.searchVisitDao()
    @Provides fun provideWeightDao(database: AppDatabase): WeightDao = database.weightDao()
    @Provides fun providePendingUploadDao(database: AppDatabase): PendingUploadDao = database.pendingUploadDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)
}
