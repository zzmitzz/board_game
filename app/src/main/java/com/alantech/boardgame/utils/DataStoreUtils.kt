package com.alantech.boardgame.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.alantech.boardgame.utils.fromJsonWithTypeToken
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.jvm.java


class DataStoreUtils @Inject constructor(
    private val mDataStore: DataStore<Preferences>
) {

    private val gson by lazy {
        Gson()
    }


    suspend fun <PrimitiveData> getPrimitiveData(key: Preferences.Key<PrimitiveData>): PrimitiveData? {
        return mDataStore.data.map { preferences ->
            preferences[key]
        }.first()
    }

    fun <PrimitiveData> getFlow(key: Preferences.Key<PrimitiveData>): Flow<PrimitiveData?> {
        return mDataStore.data.map { preferences ->
            preferences[key]
        }
    }


    // typeOfT = User::class.java
    suspend fun <T> getSerializedData(key: Preferences.Key<String>, typeOfT: Class<T>): T? {
        return mDataStore.data.map { preferences ->
            preferences[key]?.let { gson.fromJson(it, typeOfT) }
        }.first()
    }

    fun <T> getFlow(key: Preferences.Key<String>, typeOfT: Class<T>): Flow<T?> {
        return mDataStore.data.map { preferences ->
            preferences[key]?.let { gson.fromJson(it, typeOfT) }
        }
    }

    suspend fun setSerializedData(key: Preferences.Key<String>, value: Any) {
        mDataStore.edit { preferences ->
            preferences[key] = gson.toJson(value)
        }
    }
}