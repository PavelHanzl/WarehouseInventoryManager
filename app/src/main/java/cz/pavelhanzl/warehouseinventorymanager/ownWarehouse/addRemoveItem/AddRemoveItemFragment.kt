package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.addRemoveItem

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentAddRemoveItemBinding
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentScannerBinding
import cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.ownWarehouseDetail.OwnWarehousesDetailFragmentViewModel
import cz.pavelhanzl.warehouseinventorymanager.repository.hideKeyboard
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment

class AddRemoveItemFragment : BaseFragment() {
    private lateinit var binding: FragmentAddRemoveItemBinding
    private val sharedViewModel: OwnWarehousesDetailFragmentViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAddRemoveItemBinding.inflate(inflater, container, false)
        binding.sharedViewmodel = sharedViewModel
        binding.lifecycleOwner = viewLifecycleOwner


        binding.testAddRemTextView.text = sharedViewModel.addRemoveButtonEnabled.value.toString()
        registerObservers()

        val dropDownItems = listOf("Material", "Design", "Components", "Android","Material", "Design", "Components", "Android","Material", "Design", "Components", "Android","Material", "Design", "Components", "Android")

        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item_layout,dropDownItems)
        (binding.dropdownItemSelectorAddRemoveFragment.editText as? AutoCompleteTextView)?.setAdapter(adapter)



        return binding.root
    }

    private fun registerObservers() {
        sharedViewModel.goBackToPreviousScreen.observe(viewLifecycleOwner, Observer {
            if (it) {
                findNavController().navigateUp()
                hideKeyboard(activity as MainActivity)
            }
        })

    }

}