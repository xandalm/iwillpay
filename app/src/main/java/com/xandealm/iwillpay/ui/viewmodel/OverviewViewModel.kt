package com.xandealm.iwillpay.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xandealm.iwillpay.model.Expense
import com.xandealm.iwillpay.model.data.DataSource
import kotlinx.coroutines.launch
import java.util.Date

class OverviewViewModel: ViewModel() {

    private val _expenses: MutableLiveData<MutableList<Expense>> = MutableLiveData()
    val expenses get() = _expenses

    init {
        retrieve()
    }

    fun pay(id: Long) {
        val expense = _expenses.value?.find { it.id == id }
        expense?.apply {
            paidAt = Date()
        }
        retrieve()
    }

    private fun retrieve() {
        viewModelScope.launch {
            _expenses.value = DataSource.expenses.filter { expense ->
                expense.paidAt == null
            } as MutableList<Expense>
        }
    }
}