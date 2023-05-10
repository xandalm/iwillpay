package com.xandealm.iwillpay.ui.adapter

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xandealm.iwillpay.R
import com.xandealm.iwillpay.model.Expense
import com.xandealm.iwillpay.model.data.DataSource
import com.xandealm.iwillpay.model.getFormattedCost
import java.util.Date
import kotlin.math.floor

private const val TAG = "ExpenseAdapter"

private const val MIN_TIME = 1000 * 60
private const val HOUR_TIME = MIN_TIME * 60
private const val DAY_TIME = HOUR_TIME * 24

class ExpenseAdapter(
    private val ctx: Context
): RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private val dataset: List<Expense> = DataSource.expenses

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExpenseAdapter.ExpenseViewHolder {
        return ExpenseViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.expense_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val resources = ctx.resources
        val item = dataset[position]

        var headerText = SpannableString("${item.title} \u2022 ${item.getFormattedCost()}")
        headerText.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.red_700)),
            item.title.length + 3,
            headerText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        holder.expenseTitleCost?.text = headerText

        var diffTime = item.dueDate.time - Date().time
        val remaining = if(diffTime >= DAY_TIME) {
            val ddRemaining = floor((diffTime / DAY_TIME).toDouble()).toInt()
            resources.getQuantityString(R.plurals.dd_remaining,ddRemaining,ddRemaining)
        } else if(diffTime >= HOUR_TIME) {
            val hhRemaining = floor((diffTime / HOUR_TIME).toDouble()).toInt()
            resources.getQuantityString(R.plurals.hh_remaining,hhRemaining,hhRemaining)
        } else if(diffTime >= (MIN_TIME * 5)) {
            val mmRemaining = floor((diffTime / MIN_TIME).toDouble()).toInt()
            resources.getQuantityString(R.plurals.mm_remaining,mmRemaining,mmRemaining)
        } else if (diffTime > 0L) {
            resources.getString(R.string.pay_now)
        } else {
            resources.getString(R.string.youre_later)
        }

        holder.expenseRemaining?.text = remaining
        holder.expenseDescription?.text = item.description
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    class ExpenseViewHolder(view: View?): RecyclerView.ViewHolder(view!!) {
        val expenseTitleCost: TextView? = view?.findViewById(R.id.eli_title_cost)
        val expenseRemaining: TextView? = view?.findViewById(R.id.eli_remaining)
        val expenseDescription: TextView? = view?.findViewById(R.id.eli_description)
    }

}