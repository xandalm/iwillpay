package com.xandealm.iwillpay.model.data

import com.xandealm.iwillpay.model.Expense
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.util.Calendar
import java.util.Date

private val current = Date()
private const val DAY_TIME: Long = 1000 * 60 * 60 * 24
private const val HOUR_TIME = 1000 * 60 * 60
private const val MIN_TIME = 1000 * 60

object DataSource {
    val expenses = listOf<Expense>(
        Expense(
            title = "Mercado",
            description = "Compras da semana",
            cost = 230.5,
            dueDate = Date(current.time - DAY_TIME)
        ),
        Expense(
            title = "Films and Series stream service",
            description = "Children school",
            cost = 45.0,
            dueDate = Date(current.time + MIN_TIME)
        ),
        Expense(
            title = "Luz",
            description = "Serviços fornecidos pela Energia Renovável",
            cost = 50.0,
            dueDate = Date(current.time + (12 * MIN_TIME))
        ),
        Expense(
            title = "Água",
            description = "Serviços fornecidos pela Água é Vida",
            cost = 40.0,
            dueDate = Date(current.time + (2 * HOUR_TIME))
        ),
        Expense(
            title = "Internet",
            description = "Conexão Eterna",
            cost = 90.0,
            dueDate = Date(current.time + (60 * DAY_TIME))
        ),
    )
}