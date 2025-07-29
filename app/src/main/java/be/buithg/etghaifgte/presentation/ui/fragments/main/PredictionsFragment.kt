package be.buithg.etghaifgte.presentation.ui.fragments.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import be.buithg.etghaifgte.R
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import be.buithg.etghaifgte.databinding.FragmentPredictionsBinding
import androidx.core.view.isVisible
import be.buithg.etghaifgte.presentation.ui.adapters.PredictionsAdapter
import be.buithg.etghaifgte.presentation.viewmodel.PredictionsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PredictionsFragment : Fragment() {

    private lateinit var binding: FragmentPredictionsBinding
    private val viewModel: PredictionsViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPredictionsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.matchScheduleFragment)
            }
        })

        binding.btnHelp.setOnClickListener {
            findNavController().navigate(R.id.tutorialFragment)
        }

        viewModel.predictions.observe(viewLifecycleOwner) { list ->
            binding.recyclerView.adapter = PredictionsAdapter(list)
        }
        viewModel.loadPredictions()
    }
}