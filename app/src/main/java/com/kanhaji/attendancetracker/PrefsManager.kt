package com.kanhaji.attendancetracker

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

interface PreferencesDataStore {
    suspend fun getString(key: String): String?
    suspend fun saveString(key: String, value: String)
    suspend fun getInt(key: String): Int?
    suspend fun saveInt(key: String, value: Int)
    suspend fun getBoolean(key: String): Boolean?
    suspend fun saveBoolean(key: String, value: Boolean)
    suspend fun removeKey(key: String)
}

object PrefsManager {
    private var dataStore: PreferencesDataStore? = null
    private var initialized = false

    fun init(context: Context) {
        dataStore = AndroidPreferencesDataStore(context)
        initialized = true
    }

    private fun checkInit() {
        if (!initialized || dataStore == null) {
            throw IllegalStateException("PrefsManager is not initialized. Call PrefsManager.init() on each platform before use.")
        }
    }

    suspend fun getString(key: String): String? = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.getString(key)
    }

    suspend fun saveString(key: String, value: String) = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.saveString(key, value)
    }

    suspend fun getInt(key: String): Int? = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.getInt(key)
    }

    suspend fun saveInt(key: String, value: Int) = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.saveInt(key, value)
    }

    suspend fun getBoolean(key: String): Boolean = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.getBoolean(key) == true
    }

    suspend fun saveBoolean(key: String, value: Boolean) = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.saveBoolean(key, value)
    }

    suspend fun getDouble(key: String): Double? = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.getString(key)?.toDoubleOrNull()
    }

    suspend fun saveDouble(key: String, value: Double) = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.saveString(key, value.toString())
    }

    suspend fun removeKey(key: String) = withContext(Dispatchers.Default) {
        checkInit()
        dataStore!!.removeKey(key)
    }
}

private const val DATASTORE_NAME = "AttendanceRecords.prefs"

private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

class AndroidPreferencesDataStore(private val context: Context) : PreferencesDataStore {

    override suspend fun getString(key: String): String? {
        val prefsKey = stringPreferencesKey(key)
        val prefs = context.dataStore.data.first()
        return prefs[prefsKey]
    }

    override suspend fun saveString(key: String, value: String) {
        val prefsKey = stringPreferencesKey(key)
        context.dataStore.edit { it[prefsKey] = value }
    }

    override suspend fun getInt(key: String): Int? {
        val prefsKey = intPreferencesKey(key)
        val prefs = context.dataStore.data.first()
        return prefs[prefsKey]
    }

    override suspend fun saveInt(key: String, value: Int) {
        val prefsKey = intPreferencesKey(key)
        context.dataStore.edit { it[prefsKey] = value }
    }

    override suspend fun getBoolean(key: String): Boolean? {
        val prefsKey = booleanPreferencesKey(key)
        val prefs = context.dataStore.data.first()
        return prefs[prefsKey]
    }

    override suspend fun saveBoolean(key: String, value: Boolean) {
        val prefsKey = booleanPreferencesKey(key)
        context.dataStore.edit { it[prefsKey] = value }
    }

    override suspend fun removeKey(key: String) {
        val prefsKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences.remove(prefsKey)
        }
    }
}