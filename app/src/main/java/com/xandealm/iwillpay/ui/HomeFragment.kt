package com.xandealm.iwillpay.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xandealm.iwillpay.R
import com.xandealm.iwillpay.databinding.FragmentHomeBinding
import com.xandealm.iwillpay.ui.adapter.ExpenseAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expenseRecyclerView = binding.expenseRecyclerView
        expenseRecyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        expenseRecyclerView.adapter = ExpenseAdapter(this.requireContext())
    }

}