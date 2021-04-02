package cz.pavelhanzl.warehouseinventorymanager.warehouse.createEditItem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentCreateEditItemBinding
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentItemDetailBinding
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import cz.pavelhanzl.warehouseinventorymanager.warehouse.itemDetail.ItemDetaiFragmentViewModel
import cz.pavelhanzl.warehouseinventorymanager.warehouse.itemDetail.ItemDetailFragmentArgs

class CreateEditItemFragment : BaseFragment() {

    private val args: CreateEditItemFragmentArgs by navArgs()
    private lateinit var binding: FragmentCreateEditItemBinding
    lateinit var viewModel: CreateEditItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //předá argumenty do viewmodelu
        if (savedInstanceState == null) {
            viewModel = ViewModelProvider(this).get(CreateEditItemViewModel::class.java)
            viewModel.setdata(args.warehouseId)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //binduje a přiřazuje viewmodel
        binding = FragmentCreateEditItemBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner








        return binding.root
    }

}