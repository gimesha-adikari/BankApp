package com.bankingsystem.mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.defaultAccountDataStore by preferencesDataStore(name = "default_account")

class DefaultAccountStore(private val context: Context) {
    private val KEY_DEFAULT_ID = stringPreferencesKey("default_account_id")

    val defaultAccountId: Flow<String?> =
        context.defaultAccountDataStore.data.map { it[KEY_DEFAULT_ID] }

    suspend fun setDefaultAccountId(id: String) {
        context.defaultAccountDataStore.edit { it[KEY_DEFAULT_ID] = id }
    }

    suspend fun clear() {
        context.defaultAccountDataStore.edit { it.remove(KEY_DEFAULT_ID) }
    }
}
