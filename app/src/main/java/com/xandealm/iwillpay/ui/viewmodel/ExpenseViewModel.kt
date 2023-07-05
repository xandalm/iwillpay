package com.xandealm.iwillpay.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.xandealm.iwillpay.model.Expense
import com.xandealm.iwillpay.model.data.DataSource
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date

class ExpenseException(val errno: String, message: String): RuntimeException(message) {

    companion object ExpenseExceptionErrorNumber {
        const val EMPTY_EXPENSE = "EN001"
        const val INVALID_TITLE = "EN002"
        const val INVALID_DESCRIPTION = "E003"
        const val INVALID_COST = "EN004"
        const val INVALID_DUE_DATE = "EN005"
    }
}

private const val TAG = "ExpenseViewModel"

class ExpenseViewModel: ViewModel() {

    private val _expenses: MutableLiveData<MutableList<Expense>> = MutableLiveData(DataSource.expenses)

    private var _id: MutableLiveData<Long> = MutableLiveData(0)
    val id: LiveData<Long> get() = _id

    private var _title: MutableLiveData<String> = MutableLiveData()
    val title: LiveData<String> get() = _title

    private var _description: MutableLiveData<String?> = MutableLiveData()
    val description: LiveData<String?> get() = _description

    private var _cost: MutableLiveData<Float> = MutableLiveData()
    val cost: LiveData<String> get() = Transformations.map(_cost) {
        NumberFormat.getCurrencyInstance().format(it)
    }

    private var _dueDate: MutableLiveData<Date> = MutableLiveData()
    val dueDate: LiveData<String> get() = Transformations.map(_dueDate) {
        SimpleDateFormat.getDateTimeInstance().format(it)
    }

    fun requestExpenseToEdit(id: Long?) {
        val found = retrieveExpense(id ?: 0)
        found?.let {
            _id.value = it.id
            _title.value = it.title
            _description.value = it.description
            _cost.value = it.cost
            _dueDate.value = it.dueDate
        }
    }

    private fun retrieveExpense(id: Long): Expense? {
        return _expenses.value?.find { it.id == id }
    }

    fun count() = _expenses.value!!.size

    fun setTitle(title: String) {
        _title.value = title
    }

    fun setDescription(description: String) {
        _description.value = description
    }

    fun setCost(cost: Float) {
        _cost.value = cost
    }

    fun setDueDate(dueDate: Date) {
        _dueDate.value = dueDate
    }

    private fun assertValidEntry() {
        if(_title.value.isNullOrEmpty() && _cost.value == null && _dueDate.value == null)
            throw ExpenseException(errno = ExpenseException.EMPTY_EXPENSE, "Empty expense")
        else {
            if (_title.value.isNullOrEmpty())
                throw ExpenseException(
                    errno = ExpenseException.INVALID_TITLE,
                    "Invalid title, it's required!"
                )
            if (_cost.value == null || _cost.value!!.compareTo(0.0) <= 0)
                throw ExpenseException(
                    errno = ExpenseException.INVALID_COST,
                    "Invalid cost, must be finite and great than ${NumberFormat.getCurrencyInstance().format(0.0)}"
                )
            if (_dueDate.value == null || _dueDate.value!!.before(Date()))
                throw ExpenseException(
                    errno = ExpenseException.INVALID_DUE_DATE,
                    "Cannot registry expense from the past!"
                )
        }
    }

    fun commit() {
        try {
            assertValidEntry()
            if (_id.value!! > 0) {
                _expenses.value?.apply {
                    val index = _id.value!!.toInt() - 1
                    this[index].apply {
                        title = _title.value!!
                        description = _description.value
                        cost = _cost.value!!
                        dueDate = _dueDate.value!!
                    }
                }
            } else {
                if(count() > 0) {
                    _id.value = _expenses.value!!.last().id.plus(1)
                    _expenses.value?.add(
                        Expense(
                            id = _id.value!!,
                            title = _title.value!!,
                            description = _description.value,
                            cost = _cost.value!!,
                            dueDate = _dueDate.value!!
                    ))
                } else {
                    _id.value = 1
                    _expenses.value?.add(Expense(
                        id = _id.value!!,
                        title = _title.value!!,
                        description = _description.value,
                        cost = _cost.value!!,
                        dueDate = _dueDate.value!!
                    ))
                }
            }
        } catch (e: ExpenseException) {
            if(e.errno != ExpenseException.EMPTY_EXPENSE)
                throw e
        }

    }
}