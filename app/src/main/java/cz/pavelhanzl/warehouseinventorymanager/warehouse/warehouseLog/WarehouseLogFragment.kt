package cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseLog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentCreateEditItemBinding
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentWarehouseLogBinding
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import cz.pavelhanzl.warehouseinventorymanager.warehouse.createEditItem.CreateEditItemFragmentArgs
import cz.pavelhanzl.warehouseinventorymanager.warehouse.createEditItem.CreateEditItemFragmentViewModel

class WarehouseLogFragment : BaseFragment() {

    private val args: WarehouseLogFragmentArgs by navArgs()
    private lateinit var binding: FragmentWarehouseLogBinding
    lateinit var viewModel: WarehouseLogFragmentViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //předá argumenty do viewmodelu
        if (savedInstanceState == null) {
            viewModel = ViewModelProvider(this).get(WarehouseLogFragmentViewModel::class.java)
            viewModel.setdata(args.warehouseObject)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        //binduje a přiřazuje viewmodel
        binding = FragmentWarehouseLogBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner






        // Inflate the layout for this fragment
        return binding.root
    }

}