package cz.pavelhanzl.warehouseinventorymanager.warehouse.peopleInWarehouse

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentPeopleInWarehouseBinding
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentWarehouseLogBinding
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseDetail.WarehouseDetailFragmentDirections
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseLog.WarehouseLogFragmentArgs
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseLog.WarehouseLogFragmentViewModel

class PeopleInWarehouseFragment : BaseFragment() {

    private val args: PeopleInWarehouseFragmentArgs by navArgs()
    private lateinit var binding: FragmentPeopleInWarehouseBinding
    private val viewModel: PeopleInWarehouseFragmentViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //předá argumenty do viewmodelu
        if (savedInstanceState == null) {
          viewModel.setData(args.warehouseObject)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //binduje a přiřazuje viewmodel
        binding = FragmentPeopleInWarehouseBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner



        binding.fabAddUserToThisWhPeopleInWarehouseFragment.setOnClickListener {
            val action =  PeopleInWarehouseFragmentDirections.actionPeopleInWarehouseFragmentToInviteUserFragment()
            findNavController().navigate(action)
        }

        // Inflate the layout for this fragment
        return binding.root
    }

}