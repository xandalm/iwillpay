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
            "Mercado",
            "Compras da semana",
            230.5,
            Date(current.time - DAY_TIME)
        ),
        Expense(
            "Films and Series stream service",
            "Children school",
            45.0,
            Date(current.time + MIN_TIME)
        ),
        Expense(
            "Luz",
            "Serviços fornecidos pela Energia Renovável",
            50.0,
            Date(current.time + (12 * MIN_TIME))
        ),
        Expense(
            "Água",
            "Serviços fornecidos pela Água é Vida",
            40.0,
            Date(current.time + (2 * HOUR_TIME))
        ),
        Expense(
            "Internet",
            "Conexão Eterna",
            90.0,
            Date(current.time + (60 * DAY_TIME))
        ),
    )
}