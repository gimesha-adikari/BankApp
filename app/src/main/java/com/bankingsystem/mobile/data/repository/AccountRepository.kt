package com.bankingsystem.mobile.data.repository

import com.bankingsystem.mobile.data.model.account.AccountNet
import com.bankingsystem.mobile.data.model.account.AccountOpenRequest
import com.bankingsystem.mobile.data.model.account.AccountResponseNet
import com.bankingsystem.mobile.data.model.account.BranchNet
import com.bankingsystem.mobile.data.model.account.TransactionNet
import com.bankingsystem.mobile.data.service.ApiService
import javax.inject.Inject   // <-- use javax, not jakarta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

class AccountRepository @Inject constructor(
    private val api: ApiService
) {

    suspend fun getBranches(): List<BranchNet> = ioWrap { api.getBranches() }

    suspend fun openAccount(body: AccountOpenRequest): AccountResponseNet =
        ioWrap { api.openAccount(body) }

    suspend fun getAccountTransactions(accountId: String): List<TransactionNet> =
        ioWrap { api.getAccountTransactions(accountId) }

    suspend fun getMyAccounts(): List<AccountNet> = ioWrap { api.getMyAccounts() }

    private suspend inline fun <T> ioWrap(crossinline block: suspend () -> T): T =
        withContext(Dispatchers.IO) {
            try {
                block()
            } catch (e: HttpException) {
                val code = e.code()
                val raw = e.response()?.errorBody()?.string().orEmpty()
                val msg = runCatching { JSONObject(raw).optString("message") }
                    .getOrNull().orEmpty()

                if (code == 404 && msg.contains("Customer not found", ignoreCase = true)) {
                    throw CustomerMissingException(
                        "We couldn’t find your customer profile yet. Please create it to view or open accounts."
                    )
                }
                throw ApiCallException(msg.ifBlank { "Network error ($code)" })
            } catch (e: Exception) {
                throw ApiCallException(e.message ?: "Unexpected error")
            }
        }
}

class ApiCallException(message: String) : Exception(message)
class CustomerMissingException(message: String = "Customer profile missing") : Exception(message)
