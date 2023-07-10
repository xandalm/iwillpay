package com.xandealm.iwillpay.ui.util

import android.app.Dialog
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment


private typealias DialogCallBack = () -> Unit

class PaymentDialogFragment: DialogFragment() {

    private lateinit var mTitle: Any
    private lateinit var mMessage: Any
    private lateinit var mOnYes: DialogCallBack
    private lateinit var mOnNo: DialogCallBack

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        val builder = activity?.let {
            AlertDialog.Builder(it)
        }
        if(builder != null) {
            builder.apply {
                setTitle(mTitle as CharSequence)
                setMessage(mMessage as CharSequence)
                setPositiveButton("OK") { _, _ ->
                    mOnYes()
                }
                setNegativeButton("Cancel") { _, _ ->
                    mOnNo()
                }
            }
            return builder.create()
        }
        throw IllegalStateException("Activity cannot be null")
    }

    fun setTitle(title: String): PaymentDialogFragment {
        mTitle = title
        return this
    }

    fun setTitle(title: SpannableString): PaymentDialogFragment {
        mTitle = title
        return this
    }

    fun setMessage(message: String): PaymentDialogFragment {
        mMessage = message
        return this
    }

    fun setMessage(message: Spannable): PaymentDialogFragment {
        mMessage = message
        return this
    }

    fun setOnYes(fn: DialogCallBack): PaymentDialogFragment {
        mOnYes = fn
        return this
    }

    fun setOnNo(fn: DialogCallBack): PaymentDialogFragment {
        mOnNo = fn
        return this
    }
}
