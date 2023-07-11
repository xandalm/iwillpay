package com.xandealm.iwillpay.model.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.xandealm.iwillpay.model.Expense
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expense ORDER BY id ASC")
    fun getAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE paid_at IS NULL ORDER BY due_date ASC")
    fun getAllNotPaid(): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE paid_at IS NULL AND due_date < :datetime ORDER BY id ASC")
    fun getAllPendingUntil(datetime: Date): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE id = :id")
    fun getById(id: Long): Flow<Expense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)
}