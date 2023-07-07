package com.xandealm.iwillpay.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.xandealm.iwillpay.model.Expense
import com.xandealm.iwillpay.model.data.ExpenseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

private const val TAG = "OverviewViewModel"

class OverviewViewModel(private val expenseDao: ExpenseDao): ViewModel() {

    val highlightedExpenses: LiveData<List<Expense>> = expenseDao.getAllNotPaid().asLiveData()

    fun setAsPaid(expense: Expense) {
        val updated = expense.copy(paidAt = Date())
        viewModelScope.launch(Dispatchers.IO) {
            expenseDao.update(updated)
        }
    }
}

class OverviewViewModelFactory(private val expenseDao: ExpenseDao): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(OverviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OverviewViewModel(expenseDao) as  T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}