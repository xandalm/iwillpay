package com.xandealm.iwillpay.ui

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Annotation
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.style.StyleSpan
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xandealm.iwillpay.IwillpayApplication
import com.xandealm.iwillpay.NavGraphDirections
import com.xandealm.iwillpay.R
import com.xandealm.iwillpay.databinding.FragmentExpensesListBinding
import com.xandealm.iwillpay.ui.adapter.ExpenseAdapter
import com.xandealm.iwillpay.ui.util.PaymentDialogFragment
import com.xandealm.iwillpay.ui.util.SwipeDecoration
import com.xandealm.iwillpay.ui.util.SwipeItemHelper
import com.xandealm.iwillpay.ui.viewmodel.ExpensesListViewModel
import com.xandealm.iwillpay.ui.viewmodel.ExpensesListViewModelFactory
import kotlin.experimental.and

class ExpensesListFragment : Fragment() {

    private var _binding: FragmentExpensesListBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseRecyclerView: RecyclerView

    private val viewModel: ExpensesListViewModel by activityViewModels {
        ExpensesListViewModelFactory(
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
        _binding = FragmentExpensesListBinding.inflate(inflater,container,false)
        return binding.root
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()
        val supportFragmentManager = activity.supportFragmentManager

        val mainNavController = activity.findNavController(R.id.nav_host_fragment)
        binding.newExpenseBtn.setOnClickListener {
            val action = NavGraphDirections.actionGlobalExpenseFragment(0)
            mainNavController.navigate(action)
        }

        expenseRecyclerView = binding.expenseRecyclerView
        expenseRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = ExpenseAdapter(
            requireContext(),
        ) {
            val action = NavGraphDirections.actionGlobalExpenseFragment(it.id)
            mainNavController.navigate(action)
        }
        expenseRecyclerView.adapter = adapter

        val sdPayingLayout = LayoutInflater.from(requireContext()).inflate(R.layout.expense_list_item_paying,null)
        val sdDeletingLayout = LayoutInflater.from(requireContext()).inflate(R.layout.expense_list_item_deleting,null)
        val sd = SwipeDecoration(requireContext())
        val sih = object: SwipeItemHelper(
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
                                        StyleSpan(Typeface.BOLD),
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
        ) {
            override fun onSelectToSwipe(viewHolder: RecyclerView.ViewHolder, direction: Byte): Boolean {
                return when(direction) {
                    SwipeItemHelper.TO_RIGHT -> {
                        if((viewHolder as ExpenseAdapter.ExpenseViewHolder).expenseIsPaid) {
                            Toast.makeText(context,"Expense is already paid",Toast.LENGTH_SHORT).show()
                            return false
                        }
                        true
                    }
                    else -> true
                }
            }
        }
        sih.attachToRecyclerView(expenseRecyclerView)
        expenseRecyclerView.addItemDecoration(sd)

        viewModel.expenses.observe(viewLifecycleOwner) { items ->
            items?.let {
                adapter.submitList(it)
            }
        }

    }

}