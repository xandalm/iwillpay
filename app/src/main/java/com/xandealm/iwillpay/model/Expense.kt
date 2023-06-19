package com.xandealm.iwillpay.model

import java.text.NumberFormat
import java.util.Date

data class Expense (
    var id: Long,
    var title: String,
    var description: String? = null,
    var cost: Float,
    var dueDate: Date,
    var paidAt: Date? = null,
)

fun Expense.getFormattedCost(): String = NumberFormat.getCurrencyInstance().format(cost)