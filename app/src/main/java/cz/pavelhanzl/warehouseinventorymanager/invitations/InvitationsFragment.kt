package cz.pavelhanzl.warehouseinventorymanager.invitations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentInvitationsBinding
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentWarehouseLogBinding
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseLog.WarehouseLogFragmentViewModel

class InvitationsFragment : BaseFragment() {

    private lateinit var binding: FragmentInvitationsBinding
    lateinit var viewModel: InvitationsFragmentViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //binduje a přiřazuje viewmodel
        binding = FragmentInvitationsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(InvitationsFragmentViewModel::class.java)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner







        // Inflate the layout for this fragment
        return binding.root
    }

   
}