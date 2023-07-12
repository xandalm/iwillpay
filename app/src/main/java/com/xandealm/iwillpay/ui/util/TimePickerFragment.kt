package com.xandealm.iwillpay.ui.util

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private typealias OnTimeSetListener = (TimePicker, Int, Int) -> Unit

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private var cbOnTimeSet: OnTimeSetListener = { _,_,_ -> }
    private var cbOnCancel: (DialogInterface) -> Unit = { _ -> }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        view?.let {
            cbOnTimeSet(view,hourOfDay,minute)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        cbOnCancel(dialog)
    }

    fun setOnTimeSetListener(listener: OnTimeSetListener) {
        cbOnTimeSet = listener
    }

    fun setOnCancelListener(listener: (DialogInterface) -> Unit) {
        cbOnCancel = listener
    }

}
