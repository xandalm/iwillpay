package com.xandealm.iwillpay.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.xandealm.iwillpay.IwillpayApplication
import com.xandealm.iwillpay.R
import com.xandealm.iwillpay.databinding.FragmentExpenseBinding
import com.xandealm.iwillpay.ui.util.DatePickerFragment
import com.xandealm.iwillpay.ui.util.TimePickerFragment
import com.xandealm.iwillpay.ui.viewmodel.ExpenseException
import com.xandealm.iwillpay.ui.viewmodel.ExpenseViewModel
import com.xandealm.iwillpay.ui.viewmodel.ExpenseViewModelFactory
import com.xandealm.iwillpay.util.*
import java.util.*

private const val TAG = "ExpenseFragment"

class ExpenseFragment : Fragment() {

    private var _binding: FragmentExpenseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpenseViewModel by activityViewModels {
        ExpenseViewModelFactory(
            (activity?.application as IwillpayApplication)
        )
    }

    private val navigationArgs: ExpenseFragmentArgs by navArgs()

    private var expenseId = 0L

    private val dueDate: Date = Date(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseBinding.inflate(inflater,container,false)
        createMenu()
        return binding.root
    }

    private fun createMenu() {
        requireActivity().addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_in_expense_fragment,menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_item_mark_as_paid -> {
                        saveExpense()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableEditing(false)

        expenseId = navigationArgs.expenseId

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.permissionToEdit(expenseId).observe(viewLifecycleOwner) {
            enableEditing(it)
        }

        binding.apply {
            expenseTitle.setOnFocusChangeListener { v, hasFocus ->
                if(!hasFocus) {
                    try {
                        this@ExpenseFragment.viewModel.setTitle((v as EditText).text.toString())
                    } catch (e: ExpenseException) {
                        Toast.makeText(context,e.message.toString(),Toast.LENGTH_SHORT).show()
                    }
                }
            }
            expenseDescription.setOnFocusChangeListener { v, hasFocus ->
                if(!hasFocus) {
                    try {
                        this@ExpenseFragment.viewModel.setDescription((v as EditText).text.toString())
                    } catch (e: ExpenseException) {
                        Toast.makeText(context,e.message.toString(),Toast.LENGTH_SHORT).show()
                    }
                }
            }
            expenseCost.apply {
                setOnTouchListener { v, _ ->
                    performClick()
                    val editText = v as EditText
                    editText.setSelection(editText.text.length,editText.text.length)
                    if(v.requestFocus()) {
                        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
                    }
                    true
                }
                setOnKeyListener { _, keyCode, _ ->
                    return@setOnKeyListener keyCode in listOf(
                        KeyEvent.KEYCODE_DPAD_LEFT,
                        KeyEvent.KEYCODE_DPAD_UP,
                        KeyEvent.KEYCODE_DPAD_RIGHT,
                        KeyEvent.KEYCODE_DPAD_DOWN
                    )
                }
                setOnFocusChangeListener { v, hasFocus ->
                    if(hasFocus) {
                        (v as EditText).apply {
                            setSelection(text.length,text.length)
                        }
                    }
                }

                addTextChangedListener(object: TextWatcher {
                    private var formatted: String? = null

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) { }

                    override fun onTextChanged(
                        s: CharSequence,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        this@ExpenseFragment.binding.expenseCost.apply {
                            setSelection(text.length,text.length)
                        }
                    }

                    private fun onlyDigits(value: CharSequence?): String {
                        return if(value.isNullOrBlank())
                            "0"
                        else
                            value.replace(Regex("\\D"),"")
                    }

                    override fun afterTextChanged(s: Editable) {
                        val currentValue = onlyDigits(s.toString())
                        val formattedValue = onlyDigits(formatted)
                        if(formattedValue != currentValue) {
                            s.apply {
                                val currentValueAsDouble = currentValue.padStart(3,'0').toLong() / 100.0
                                val partial = onlyDigits("%.2f".format(currentValueAsDouble))
                                    .replace(Regex("^(\\d{1,3})((?:\\d{3})+)?(\\d{2})$")) {
                                        val groups = it.groupValues
                                        "${groups[1]}${groups[2]}.${groups[3]}"
                                    }
                                formatted = partial
                                this@ExpenseFragment.viewModel.setCost(partial.toFloat())
                            }
                        }
                    }

                })
            }
            expenseDueDate.apply {
                setOnFocusChangeListener { _, hasFocus ->
                    if(hasFocus) {
                        val activity = requireActivity()
                        activity.hideSoftKeyboard()
                        val datePickerFragment = DatePickerFragment()
                        datePickerFragment.apply {
                            setOnDateSetListener { _, year, month, dayOfMonth ->
                                handleDateSet(year,month, dayOfMonth)
                            }
                            setOnCancelListener {
                                activity.removeFocus()
                            }
                        }
                        datePickerFragment.show(activity.supportFragmentManager,"datePicker")
                    }
                }
                setOnTouchListener { _, me ->
                    if(me.action == MotionEvent.ACTION_UP) {
                        val activity = requireActivity()
                        performClick()
                        activity.hideSoftKeyboard()
                        val datePickerFragment = DatePickerFragment()
                        datePickerFragment.apply {
                            setOnDateSetListener { _, year, month, dayOfMonth ->
                                handleDateSet(year,month, dayOfMonth)
                            }
                            setOnCancelListener {
                                activity.removeFocus()
                            }
                        }
                        datePickerFragment.show(activity.supportFragmentManager,"datePicker")
                    }
                    true
                }
            }
        }

    }

    private fun handleDateSet(year: Int, month: Int, dayOfMonth: Int) {
        val date = Calendar.getInstance()
        date.set(year, month, dayOfMonth)
        date.set(Calendar.HOUR_OF_DAY,0)
        date.set(Calendar.MINUTE,0)
        date.set(Calendar.MILLISECOND,0)
        try {
            dueDate.time = date.time.time
            val timePickerFragment = TimePickerFragment()
            timePickerFragment.setOnTimeSetListener { _, hourOfDay, minute ->
                handleTimeSet(hourOfDay, minute)
            }
            timePickerFragment.show(requireActivity().supportFragmentManager,"timePicker")
        } catch (e: Exception) {
            Toast.makeText(context,e.message.toString(),Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleTimeSet(hourOfDay: Int, minute: Int) {
        try {
            val time = hourOfDay.toLong() * HOUR_TIME + minute * MIN_TIME
            dueDate.time += time
            viewModel.setDueDate(this.dueDate)
        } catch (e: ExpenseException) {
            Toast.makeText(context,e.message.toString(),Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableEditing(value: Boolean) {
        binding.apply {
            expenseTitle.isEnabled = value
            expenseCost.isEnabled = value
            expenseDescription.isEnabled = value
            expenseDueDate.isEnabled = value
        }
    }

    override fun onStop() {
        super.onStop()
        activity?.hideSoftKeyboardAndRemoveFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hide keyboard.
        activity?.hideSoftKeyboardAndRemoveFocus()
        try {
            saveExpense()
        } catch(e: ExpenseException) {
            Toast.makeText(context,e.message.toString(),Toast.LENGTH_SHORT).show()
        }
        _binding = null
    }

    private fun saveExpense() {
        viewModel.saveExpense()
    }
}