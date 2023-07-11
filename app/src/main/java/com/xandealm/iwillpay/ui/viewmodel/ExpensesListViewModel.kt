package com.xandealm.iwillpay.ui.viewmodel

import androidx.lifecycle.*
import com.xandealm.iwillpay.model.Expense
import com.xandealm.iwillpay.model.data.ExpenseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ExpensesListViewModel(private val expenseDao: ExpenseDao): ViewModel() {

    val expenses: LiveData<List<Expense>> = expenseDao.getAll().asLiveData()

    fun setAsPaid(expense: Expense) {
        val updated = expense.copy(paidAt = Date())
        viewModelScope.launch(Dispatchers.IO) {
            expenseDao.update(updated)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseDao.delete(expense)
        }
    }
}

class ExpensesListViewModelFactory(private val expenseDao: ExpenseDao): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ExpensesListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpensesListViewModel(expenseDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}