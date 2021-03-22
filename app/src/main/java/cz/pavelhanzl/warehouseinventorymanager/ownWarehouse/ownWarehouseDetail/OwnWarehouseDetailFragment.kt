package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.ownWarehouseDetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentCreateWarehouseBinding
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentOwnWarehouseDetailBinding
import cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.createWarehouse.CreateWarehouseFragmentViewModel
import kotlinx.android.synthetic.main.fragment_own_warehouse_detail.*

class OwnWarehouseDetailFragment : Fragment() {
    private lateinit var binding: FragmentOwnWarehouseDetailBinding
    lateinit var viewModel: OwnWarehousesDetailFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null){
            viewModel = ViewModelProvider(this).get(OwnWarehousesDetailFragmentViewModel::class.java)
            val args: OwnWarehouseDetailFragmentArgs by navArgs()
            viewModel.setData(args.warehouseID)
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //binduje a přiřazuje viewmodel
        binding = FragmentOwnWarehouseDetailBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner



        return binding.root
    }




}