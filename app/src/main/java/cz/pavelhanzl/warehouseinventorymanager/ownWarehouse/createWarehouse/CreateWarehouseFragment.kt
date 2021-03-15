package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.createWarehouse

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import androidx.lifecycle.ViewModelProvider
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentCreateWarehouseBinding

class CreateWarehouseFragment : Fragment() {
    private lateinit var binding: FragmentCreateWarehouseBinding
    lateinit var viewModel: CreateWarehouseFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_warehouse, container, false)
        viewModel = ViewModelProvider(this).get(CreateWarehouseFragmentViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner





        // Inflate the layout for this fragment
        return binding.root
    }

}