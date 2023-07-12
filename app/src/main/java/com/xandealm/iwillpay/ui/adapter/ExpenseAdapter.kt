package com.xandealm.iwillpay.ui.adapter

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xandealm.iwillpay.R
import com.xandealm.iwillpay.databinding.ExpenseListItemBinding
import com.xandealm.iwillpay.model.Expense
import com.xandealm.iwillpay.model.getFormattedCost
import com.xandealm.iwillpay.util.DAY_TIME
import com.xandealm.iwillpay.util.HOUR_TIME
import com.xandealm.iwillpay.util.MIN_TIME
import java.util.Date
import kotlin.math.floor

private const val TAG = "ExpenseAdapter"
class ExpenseAdapter(
    private val ctx: Context,
    private val onDetailsClicked: (Expense) -> Unit,
): ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(DiffCallback) {

    class ExpenseViewHolder(private val ctx: Context, private val binding: ExpenseListItemBinding): RecyclerView.ViewHolder(binding.root) {

        var expenseIsPaid = false

        private fun getTitleCost(expense: Expense): SpannableString {
            val titleCost = SpannableString("${expense.title} \u2022 ${expense.getFormattedCost()}")
            titleCost.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(ctx,R.color.red_700)
                ),
                expense.title.length + 3,
                titleCost.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return titleCost
        }

        private fun getRemaining(expense: Expense): String {
            val diffTime = expense.dueDate.time - Date().time
            val resources = ctx.resources
            return if(expenseIsPaid) {
                resources.getString(R.string.paid)
            } else if(diffTime >= DAY_TIME) {
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
        }

        fun bind(expense: Expense, onDetailsClicked: (Expense) -> Unit) {
            expenseIsPaid = expense.paidAt != null
            binding.apply {
                eliTitleCost.text = getTitleCost(expense)
                eliRemaining.text = getRemaining(expense)
                val placeholderDescription = SpannableString("No description")
                placeholderDescription.setSpan(
                    ForegroundColorSpan(Color.parseColor("#22000000")),
                    0,
                    placeholderDescription.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                eliDescription.text = expense.description ?: placeholderDescription
            }
        }

    }

    companion object {
        private val DiffCallback = object: DiffUtil.ItemCallback<Expense>() {
            override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExpenseViewHolder {
        return ExpenseViewHolder(
            ctx,
            ExpenseListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, onDetailsClicked)
        holder.itemView.setOnClickListener {
            onDetailsClicked(current)
        }
    }

}
