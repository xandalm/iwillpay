package com.xandealm.iwillpay.model

import java.text.NumberFormat
import java.util.Date

data class Expense (
    val id: Long? = null,
    val title: String,
    val description: String,
    val cost: Double,
    val dueDate: Date
)

fun Expense.getFormattedCost(): String = NumberFormat.getCurrencyInstance().format(cost)