package cz.pavelhanzl.warehouseinventorymanager.warehouse.itemDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentItemDetailBinding
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment

class ItemDetailFragment : BaseFragment() {
    private val args: ItemDetailFragmentArgs by navArgs()
    private lateinit var binding: FragmentItemDetailBinding
    lateinit var viewModel: ItemDetailFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        //předá argumenty do viewmodelu
        if (savedInstanceState == null) {
            viewModel = ViewModelProvider(this).get(ItemDetailFragmentViewModel::class.java)
            viewModel.setdata(args.selectedWarehouseItemObject)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //binduje a přiřazuje viewmodel
        binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner








        return binding.root
    }

}