package com.xandealm.iwillpay

import android.app.Application
import com.xandealm.iwillpay.model.data.IwillpayDatabase

class IwillpayApplication: Application() {
    val database: IwillpayDatabase by lazy {
        IwillpayDatabase.getDatabase(this)
    }
}