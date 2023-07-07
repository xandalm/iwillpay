package com.xandealm.iwillpay.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Canvas
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Annotation
import android.text.SpannedString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xandealm.iwillpay.IwillpayApplication
import com.xandealm.iwillpay.NavGraphDirections
import com.xandealm.iwillpay.R
import com.xandealm.iwillpay.databinding.FragmentOverviewBinding
import com.xandealm.iwillpay.model.Expense
import com.xandealm.iwillpay.ui.adapter.ExpenseAdapter
import com.xandealm.iwillpay.ui.util.SwipeDecoration
import com.xandealm.iwillpay.ui.util.SwipeItemHelper
import com.xandealm.iwillpay.ui.viewmodel.OverviewViewModel
import com.xandealm.iwillpay.ui.viewmodel.OverviewViewModelFactory
import kotlin.experimental.and

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

private const val TAG = "OverviewFragment"
class OverviewFragment : Fragment() {

    private var _binding: FragmentOverviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseRecyclerView: RecyclerView

    private val viewModel: OverviewViewModel by activityViewModels {
        OverviewViewModelFactory(
            (activity?.application as IwillpayApplication).database.expenseDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOverviewBinding.inflate(inflater,container,false)
        return binding.root
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val supportFragmentManager = activity?.supportFragmentManager
        val mainNavController = (supportFragmentManager?.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        expenseRecyclerView = binding.expenseRecyclerView
        val layoutManager = LinearLayoutManager(context)
        expenseRecyclerView.layoutManager = layoutManager
        val adapter = ExpenseAdapter(requireContext()) {
            val action = NavGraphDirections.actionGlobalExpenseFragment(it.id)
            mainNavController.navigate(action)
        }
        expenseRecyclerView.adapter = adapter

//        val ith = ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
//            private val v = LayoutInflater.from(requireContext()).inflate(R.layout.expense_list_item_paying,expenseRecyclerView,false)
//            override fun onMove(
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                target: RecyclerView.ViewHolder
//            ): Boolean {
//                Log.d(TAG,"MOVE")
//                return true
//            }
//
//            override fun onChildDraw(
//                c: Canvas,
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                dX: Float,
//                dY: Float,
//                actionState: Int,
//                isCurrentlyActive: Boolean
//            ) {
//
//                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
//                    c.save()
//                    viewHolder.itemView.let {
//                        val width = abs(it.x).toInt()
//                        val height = it.height
//                        v.measure(width,height)
//                        v.layout(
//                            0,
//                            0,
//                            width,
//                            height
//                        )
//                        c.translate(if(it.left < 0) it.right.toFloat() else 0f, it.y)
//                        v.draw(c)
//                    }
//                    c.restore()
//                }
//
//                super.onChildDraw(
//                    c,
//                    recyclerView,
//                    viewHolder,
//                    dX,
//                    dY,
//                    actionState,
//                    isCurrentlyActive
//                )
//            }
//
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                when(direction) {
//                    ItemTouchHelper.RIGHT -> {
//                        val position = viewHolder.layoutPosition
//                        val item = adapter.currentList[position]
//                        PaymentDialogFragment(
//                            "Pay expense",
//                            "Want to pay 'Placeholder' expense?",
//                            {
//                                payExpense(item.id)
//                            },
//                            {
//                                adapter.notifyItemChanged(position)
//                            })
//                            .show(supportFragmentManager, "Pay expense")
//                    }
//                }
//            }
//        })
//        ith.attachToRecyclerView(expenseRecyclerView)

        val sdPayingLayout = LayoutInflater.from(requireContext()).inflate(R.layout.expense_list_item_paying,null)
        val sdDeletingLayout = LayoutInflater.from(requireContext()).inflate(R.layout.expense_list_item_deleting,null)
        val sd = SwipeDecoration(requireContext())
        val sih = SwipeItemHelper(
            object: SwipeItemHelper.SimpleCallback(SwipeItemHelper.TO_RIGHT) {

                var prevDir: Byte = 0x00

                override fun beforeSliding(viewHolder: RecyclerView.ViewHolder, direction: Byte) {
                    super.beforeSliding(viewHolder, direction)
                    when(direction) {
                        SwipeItemHelper.TO_RIGHT -> {
                            prevDir = direction
                            sd.setLayout(sdPayingLayout)
                            sd.setViewHolder(viewHolder)
                        }
                        else -> {
                            prevDir = direction
                            sd.setLayout(sdDeletingLayout)
                            sd.setViewHolder(viewHolder)
                        }
                    }
                }

                override fun onSliding(viewHolder: RecyclerView.ViewHolder, direction: Byte) {
                    super.onSliding(viewHolder, direction)
                    if(direction and prevDir == 0.toByte()) {
                        if(direction == SwipeItemHelper.TO_LEFT) {
                            sd.setLayout(sdDeletingLayout)
                            sd.setViewHolder(viewHolder)
                        } else {
                            sd.setLayout(sdPayingLayout)
                            sd.setViewHolder(viewHolder)
                        }
                    }
                    prevDir = direction
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Byte) {
                    when(direction) {
                        SwipeItemHelper.TO_RIGHT -> {

                            val position = viewHolder.layoutPosition
                            val item = adapter.currentList[position]

                            val message = getText(R.string.set_as_paid_message) as SpannedString
                            val spannableMessage = SpannableStringBuilder(message)

                            val annotations = message.getSpans(0, message.length, Annotation::class.java)
                            for(annotation in annotations) {
                                if(annotation.key == "textStyle" && annotation.value == "title_emphasis") {
                                    val from = message.getSpanStart(annotation)
                                    val to = message.getSpanEnd(annotation)
                                    spannableMessage.setSpan(
                                        StyleSpan(android.graphics.Typeface.BOLD),
                                        from,
                                        to,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                    spannableMessage.replace(from,to,item.title)
                                }
                            }
                            PaymentDialogFragment()
                                .setTitle("Pay expense")
                                .setMessage(spannableMessage)
                                .setOnYes { viewModel.setAsPaid(item) }
                                .setOnNo {
                                    sd.setViewHolder(null)
                                    adapter.notifyItemChanged(position)
                                }
                                .show(supportFragmentManager, "PayExpense")
                        }
                    }
                }
            }
        )
        sih.attachToRecyclerView(expenseRecyclerView)
        expenseRecyclerView.addItemDecoration(sd)

        val dividerItemDecoration = object: DividerItemDecoration(context,layoutManager.orientation) {
            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                super.onDraw(c, parent, state)
                val left = parent.paddingLeft
                val right = parent.width - parent.paddingRight

                val childCount = parent.childCount
                drawable?.let {
                    for (i in 0 until (childCount - 1)) {
                        val child = parent.getChildAt(i)
                        val params = child.layoutParams as RecyclerView.LayoutParams
                        val top = child.bottom + params.bottomMargin
                        val bottom: Int = top + it.intrinsicHeight
                        it.setBounds(left, top, right, bottom)
                        it.draw(c)
                    }
                }

            }
        }
        expenseRecyclerView.addItemDecoration(dividerItemDecoration)

        viewModel.highlightedExpenses.observe(this.viewLifecycleOwner) { items ->
            items.let {
                adapter.submitList(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}