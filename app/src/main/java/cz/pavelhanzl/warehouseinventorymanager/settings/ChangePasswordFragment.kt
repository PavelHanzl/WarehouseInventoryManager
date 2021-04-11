package cz.pavelhanzl.warehouseinventorymanager.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentChangePasswordBinding
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentInviteUserBinding
import cz.pavelhanzl.warehouseinventorymanager.repository.hideKeyboard
import cz.pavelhanzl.warehouseinventorymanager.service.observeInLifecycle
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseDetail.WarehousesDetailFragmentViewModel
import kotlinx.coroutines.flow.onEach

/**
 * Change password fragment
 * Lets user to change his password.
 * @constructor Create empty Change password fragment
 */
class ChangePasswordFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentChangePasswordBinding
    private val viewModel: SettingsFragmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //binduje a přiřazuje viewmodel
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.wipeData()
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        binding.viewmodel!!.eventsFlow
            .onEach {
                when (it) {
                    SettingsFragmentViewModel.Event.NavigateBack -> {
                        findNavController().navigateUp()
                        Toast.makeText(context, getString(R.string.passwordWasChanged), Toast.LENGTH_LONG).show()
                        hideKeyboard(activity as MainActivity)
                    }
                }
            }.observeInLifecycle(viewLifecycleOwner)
    }


}