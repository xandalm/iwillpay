package com.xandealm.iwillpay.model.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.xandealm.iwillpay.model.DateConverter
import com.xandealm.iwillpay.model.Expense

@Database(entities = [Expense::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class IwillpayDatabase: RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: IwillpayDatabase? = null

        fun getDatabase(ctx: Context): IwillpayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    ctx.applicationContext,
                    IwillpayDatabase::class.java,
                    "iwillpay_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}