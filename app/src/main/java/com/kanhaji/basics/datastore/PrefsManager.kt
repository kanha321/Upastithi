package com.kanhaji.basics.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

interface PreferencesDataStore {
    suspend fun <T> get(key: Preferences.Key<T>): T?
    suspend fun <T> set(key: Preferences.Key<T>, value: T)
    suspend fun <T> remove(key: Preferences.Key<T>)
    suspend fun clear()
}

private var DATASTORE_NAME = "mwi_prefs"
private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

class AndroidPreferencesDataStore(private val context: Context) : PreferencesDataStore {
    override suspend fun <T> get(key: Preferences.Key<T>): T? =
        context.dataStore.data.first()[key]

    override suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }

    override suspend fun <T> remove(key: Preferences.Key<T>) {
        context.dataStore.edit { it.remove(key) }
    }

    override suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}

object PrefsManager {
    private var store: PreferencesDataStore? = null

    fun init(context: Context) {
        store = AndroidPreferencesDataStore(context)
    }

    private fun requireStore() =
        store ?: error("PrefsManager not initialized. Call PrefsManager.init() first.")

    // String
    suspend fun getString(key: String) =
        withContext(Dispatchers.Default) { requireStore().get(stringPreferencesKey(key)) }

    suspend fun saveString(key: String, value: String) =
        withContext(Dispatchers.Default) { requireStore().set(stringPreferencesKey(key), value) }

    suspend fun deleteString(key: String) =
        withContext(Dispatchers.Default) { requireStore().remove(stringPreferencesKey(key)) }

    // Int
    suspend fun getInt(key: String) =
        withContext(Dispatchers.Default) { requireStore().get(intPreferencesKey(key)) }

    suspend fun saveInt(key: String, value: Int) =
        withContext(Dispatchers.Default) { requireStore().set(intPreferencesKey(key), value) }

    suspend fun deleteInt(key: String) =
        withContext(Dispatchers.Default) { requireStore().remove(intPreferencesKey(key)) }

    // Boolean
    suspend fun getBoolean(key: String) : Boolean? =
        withContext(Dispatchers.Default) { requireStore().get(booleanPreferencesKey(key)) }

    suspend fun saveBoolean(key: String, value: Boolean) =
        withContext(Dispatchers.Default) { requireStore().set(booleanPreferencesKey(key), value) }

    suspend fun deleteBoolean(key: String) =
        withContext(Dispatchers.Default) { requireStore().remove(booleanPreferencesKey(key)) }

    // Double
    suspend fun getDouble(key: String) =
        withContext(Dispatchers.Default) { requireStore().get(doublePreferencesKey(key)) }

    suspend fun saveDouble(key: String, value: Double) =
        withContext(Dispatchers.Default) { requireStore().set(doublePreferencesKey(key), value) }

    suspend fun deleteDouble(key: String) =
        withContext(Dispatchers.Default) { requireStore().remove(doublePreferencesKey(key)) }

    // Any Object (stored as String)
    suspend fun saveObject(key: String, value: Any) =
        withContext(Dispatchers.Default) { saveString(key, value.toString()) }

    suspend inline fun <reified T> getObject(
        key: String,
        crossinline parser: (String) -> T
    ): T? = withContext(Dispatchers.Default) {
        getString(key)?.let { parser(it) }
    }

    // Clear all
    suspend fun clear() =
        withContext(Dispatchers.Default) { requireStore().clear() }
}
