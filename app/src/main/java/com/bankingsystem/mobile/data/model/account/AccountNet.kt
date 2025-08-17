package com.bankingsystem.mobile.data.model.account

import com.google.gson.annotations.SerializedName

data class AccountNet(
    @SerializedName("accountId")      val accountId: String,
    @SerializedName("accountNumber")  val accountNumber: String,
    @SerializedName("accountType")    val accountType: String,
    @SerializedName("accountStatus")  val accountStatus: String,
    @SerializedName("balance")        val balance: Double,

    @SerializedName("branchName")     val branchName: String? = null,
    @SerializedName("branchId")       val branchId: String? = null,

    @SerializedName("createdAt")      val createdAt: String? = null,
    @SerializedName("updatedAt")      val updatedAt: String? = null
)

data class BranchNet(
    val branchId: String,
    val branchName: String
)

data class AccountOpenRequest(
    val accountType: String,
    val initialDeposit: Double,
    val branchId: String
)

data class AccountResponseNet(
    val accountId: String,
    val accountNumber: String,
    val accountType: String,
    val accountStatus: String,
    val balance: Double
)

data class TransactionNet(
    val transactionId: String,
    val accountId: String,
    val type: String,
    val amount: Double,
    val balanceAfter: Double,
    val description: String?,
    val createdAt: List<Int>
)
