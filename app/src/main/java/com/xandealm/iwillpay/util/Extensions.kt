package com.xandealm.iwillpay.util

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

fun Activity.hideSoftKeyboard() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    currentFocus?.let {
        inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

fun Activity.removeFocus() {
    currentFocus?.clearFocus()
}

fun Activity.hideSoftKeyboardAndRemoveFocus() {
    hideSoftKeyboard()
    removeFocus()
}