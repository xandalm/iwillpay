package com.xandealm.iwillpay.ui.viewmodel

import androidx.lifecycle.*
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.xandealm.iwillpay.IwillpayApplication
import com.xandealm.iwillpay.model.Expense
import com.xandealm.iwillpay.util.MIN_TIME
import com.xandealm.iwillpay.worker.ExpenseReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.lang.Long.max
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

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

class ExpenseViewModel(application: IwillpayApplication): ViewModel() {

    private val expenseDao = application.database.expenseDao()
    private val workManager = WorkManager.getInstance(application)

    private var _id: MutableLiveData<Long> = MutableLiveData()
    val id: LiveData<Long> get() = _id

    private var _title: MutableLiveData<String?> = MutableLiveData()
    val title: LiveData<String?> get() = _title

    private var _description: MutableLiveData<String?> = MutableLiveData()
    val description: LiveData<String?> get() = _description

    private var _cost: MutableLiveData<Float?> = MutableLiveData()
    val cost: LiveData<String> get() = Transformations.map(_cost) {
        it?.let {
            NumberFormat.getCurrencyInstance().format(it)
        }
    }

    private var _dueDate: MutableLiveData<Date?> = MutableLiveData()
    val dueDate: LiveData<String?> get() = Transformations.map(_dueDate) {
        it?.let {
            SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(it)
        }
    }

    private var _previousDueDate: MutableLiveData<Date?> = MutableLiveData()

    private var _paidAt: MutableLiveData<Date?> = MutableLiveData()
    val paidAt: LiveData<String?> get() = Transformations.map(_paidAt) {
        it?.let {
            SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(it)
        }
    }

    fun permissionToEdit(id: Long): LiveData<Boolean> {
        val permission = MutableLiveData<Boolean>()
        viewModelScope.launch {
            getExpense(id).let {
                if(it == null) {
                    permission.postValue(true)
                } else {
                    _id.value = it.id
                    _title.value = it.title
                    _description.value = it.description
                    _cost.value = it.cost
                    _dueDate.value = it.dueDate
                    _previousDueDate.value = it.dueDate
                    _paidAt.value = it.paidAt
                    permission.postValue(it.paidAt == null)
                }
            }
        }
        return permission
    }

    private suspend fun getExpense(id: Long): Expense? {
        return expenseDao.getById(id).firstOrNull()
    }

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
        dueDate.time -= (dueDate.time % (60000)) // ignore seconds
        _dueDate.value = dueDate
    }

    init {
        reset()
    }

    private fun reset() {
        _id.value = 0L
        _title.value = null
        _description.value = null
        _cost.value = null
        _dueDate.value = null
        _paidAt.value = null
        _previousDueDate.value = null
    }

    private fun scheduleReminder(expense: Expense) {
        val data = Data.Builder()
            .putLong(ExpenseReminderWorker.idKey,expense.id)
            .putString(ExpenseReminderWorker.titleKey,expense.title)
            .build()

        val request = OneTimeWorkRequestBuilder<ExpenseReminderWorker>()
            .setInitialDelay(
                max(0,expense.dueDate.time - Date().time - (MIN_TIME * 5)),
                TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        workManager.enqueueUniqueWork("ExpenseReminder-${expense.id}",ExistingWorkPolicy.REPLACE,request)
    }

    private fun assertValidEntry() {
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

    private fun createExpense(): Expense {
        return Expense(
            id = _id.value!!,
            title = _title.value!!,
            description = _description.value,
            cost = _cost.value!!,
            dueDate = _dueDate.value!!,
            paidAt = _paidAt.value
        )
    }

    fun saveExpense() {
        try {
            assertValidEntry()
            val expense = createExpense()
            viewModelScope.launch(Dispatchers.IO) {
                var id = expense.id
                if(id == 0L)
                    id = expenseDao.insert(expense)
                else
                    expenseDao.update(expense)
                scheduleReminder(expense.copy(id = id))
            }
        } catch (e: ExpenseException) {
            // user tried to save expense without title, cost or due date
            // forward error if not all is empty
            if(!_title.value.isNullOrEmpty() || !_description.value.isNullOrEmpty() || _cost.value != null || _dueDate.value != null) {
                when (e.errno) {
                    ExpenseException.INVALID_TITLE, ExpenseException.INVALID_COST, ExpenseException.INVALID_DESCRIPTION -> {
                        throw e
                    }
                    ExpenseException.INVALID_DUE_DATE -> {
                        if (_dueDate.value != _previousDueDate.value)
                            throw e
                    }
                }
            }
        } finally {
            reset()
        }
    }

}

class ExpenseViewModelFactory(private val application: IwillpayApplication): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}