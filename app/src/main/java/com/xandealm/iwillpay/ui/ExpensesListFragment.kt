package com.xandealm.iwillpay.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xandealm.iwillpay.NavGraphDirections
import com.xandealm.iwillpay.R
import com.xandealm.iwillpay.databinding.FragmentExpensesListBinding
import com.xandealm.iwillpay.ui.adapter.ExpenseAdapter

class ExpensesListFragment : Fragment() {

    private var _binding: FragmentExpensesListBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExpensesListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()
        val mainNavController = activity.findNavController(R.id.nav_host_fragment)
        binding.newExpenseBtn.setOnClickListener {
            val action = NavGraphDirections.actionGlobalExpenseFragment(0)
            mainNavController.navigate(action)
        }

        expenseRecyclerView = binding.expenseRecyclerView
        expenseRecyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        expenseRecyclerView.adapter = ExpenseAdapter(
            this.requireContext(),
        ) {
            val action = NavGraphDirections.actionGlobalExpenseFragment(it.id)
            mainNavController.navigate(action)
        }

    }

}