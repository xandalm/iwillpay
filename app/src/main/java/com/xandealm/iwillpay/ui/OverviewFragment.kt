package com.xandealm.iwillpay.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xandealm.iwillpay.R
import com.xandealm.iwillpay.databinding.FragmentOverviewBinding
import com.xandealm.iwillpay.ui.adapter.ExpenseAdapter

class OverviewFragment : Fragment() {

    private var _binding: FragmentOverviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOverviewBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainNavController = (this.activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        expenseRecyclerView = binding.expenseRecyclerView
        expenseRecyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        expenseRecyclerView.adapter = ExpenseAdapter(this.requireContext()) {
            mainNavController.navigate(R.id.expenseFragment)
        }
    }

}