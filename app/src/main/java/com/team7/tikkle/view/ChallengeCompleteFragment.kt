package com.team7.tikkle.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.team7.tikkle.R
import com.team7.tikkle.databinding.FragmentChallengeCompleteBinding

class ChallengeCompleteFragment : Fragment() {

    lateinit var binding: FragmentChallengeCompleteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_challenge_complete, container, false)
        binding = FragmentChallengeCompleteBinding.inflate(inflater, container, false)

        binding.btnNext.setOnClickListener {
            val home = HomeFragment()
            fragmentManager?.beginTransaction()?.apply {
                replace(R.id.View_constraint_layout, home)
                addToBackStack(null)
                commit()
            }
        }

        return binding.root
    }
}