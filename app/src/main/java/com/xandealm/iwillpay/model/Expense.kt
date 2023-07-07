package com.xandealm.iwillpay.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.text.NumberFormat
import java.util.Date

class DateConverter {
    @TypeConverter
    fun fromDate2Timestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromTimestamp2Date(time: Long?): Date? = if(time != null) Date(time) else null
}

@Entity(tableName = "expense")
data class Expense (
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var title: String,
    var description: String? = null,
    var cost: Float,
    @ColumnInfo(name = "due_date")
    var dueDate: Date,
    @ColumnInfo(name = "paid_at")
    var paidAt: Date? = null,
)

fun Expense.getFormattedCost(): String = NumberFormat.getCurrencyInstance().format(cost)