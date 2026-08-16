package com.alantech.boardgame.onboarding.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.alantech.boardgame.databinding.FragmentOnboardingBinding
import com.alantech.boardgame.onboarding.OnboardingItem
import com.alantech.boardgame.onboarding.listOnboardingFill

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private val position: Int by lazy {
        requireArguments().getInt(ARG_POSITION)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind(listOnboardingFill[position])
        val isLast = position == listOnboardingFill.lastIndex
        binding.btnGetStarted.visibility = if (isLast) View.VISIBLE else View.GONE
        binding.btnGetStarted.setOnClickListener {
            (requireActivity() as? OnboardingCompleteListener)?.onOnboardingComplete()
        }
    }

    private fun bind(item: OnboardingItem) {
        binding.imageView.setImageResource(item.image)
        binding.tvTitle.setText(item.title)
        binding.tvDescription.setText(item.description)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun interface OnboardingCompleteListener {
        fun onOnboardingComplete()
    }

    companion object {
        private const val ARG_POSITION = "arg_position"

        fun newInstance(position: Int): OnboardingFragment =
            OnboardingFragment().apply {
                arguments = bundleOf(ARG_POSITION to position)
            }
    }
}
