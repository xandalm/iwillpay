package com.xandealm.iwillpay.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xandealm.iwillpay.databinding.FragmentExpensesListBinding

class ExpensesListFragment : Fragment() {

    private var _binding: FragmentExpensesListBinding? = null
    private val binding get() = _binding!!

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

}