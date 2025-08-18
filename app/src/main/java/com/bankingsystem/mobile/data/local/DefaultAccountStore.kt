package com.bankingsystem.mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface DefaultAccountStore {
    val defaultAccountId: kotlinx.coroutines.flow.Flow<String?>
    suspend fun setDefaultAccountId(id: String)
    suspend fun clear()
}
private val Context.defaultAccountDataStore by preferencesDataStore(name = "default_account")

class DefaultAccountStoreImpl @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) :DefaultAccountStore {
    private val KEY_DEFAULT_ID = stringPreferencesKey("default_account_id")

    override val defaultAccountId: Flow<String?> =
        context.defaultAccountDataStore.data.map { it[KEY_DEFAULT_ID] }

    override suspend fun setDefaultAccountId(id: String) {
        context.defaultAccountDataStore.edit { it[KEY_DEFAULT_ID] = id }
    }

    override suspend fun clear() {
        context.defaultAccountDataStore.edit { it.remove(KEY_DEFAULT_ID) }
    }
}
