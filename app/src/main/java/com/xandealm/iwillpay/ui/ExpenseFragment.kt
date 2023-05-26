package com.xandealm.iwillpay.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.xandealm.iwillpay.databinding.FragmentExpenseBinding

class ExpenseFragment : Fragment() {

    private var _binding: FragmentExpenseBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExpenseBinding.inflate(inflater,container,false)
        return binding.root
    }

}