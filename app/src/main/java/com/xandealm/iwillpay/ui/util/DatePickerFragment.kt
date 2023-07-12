package com.xandealm.iwillpay.ui.util

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private typealias OnDateSetListener = (DatePicker, Int, Int, Int) -> Unit
private typealias OnCancelListener = (DialogInterface) -> Unit

class DatePickerFragment: DialogFragment(), DatePickerDialog.OnDateSetListener {

    private var cbOnDateSet: OnDateSetListener = { _,_,_,_ -> }
    private var cbOnCancel: OnCancelListener = { _ -> }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(requireContext(), this, year, month, day)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        view?.let {
            cbOnDateSet(it,year,month,dayOfMonth)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        cbOnCancel(dialog)
    }

    fun setOnDateSetListener(listener: OnDateSetListener) {
        cbOnDateSet = listener
    }

    fun setOnCancelListener(listener: OnCancelListener) {
        cbOnCancel = listener
    }

}
