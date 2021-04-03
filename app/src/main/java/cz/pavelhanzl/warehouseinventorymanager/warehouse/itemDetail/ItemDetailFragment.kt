package cz.pavelhanzl.warehouseinventorymanager.warehouse.itemDetail

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentItemDetailBinding
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseDetail.WarehouseDetailFragmentDirections

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


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        if (args.ownWarehouse) {
            inflater.inflate(R.menu.own_warehouse_detail_menu_admin, menu)
        } else {
            inflater.inflate(R.menu.own_warehouse_detail_menu_user, menu)
        }


        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item!!.itemId) {
            R.id.miOwnWarehouseEdit -> {
                Toast.makeText(context, "Edit", Toast.LENGTH_SHORT).show()
                //var action =  WarehouseDetailFragmentDirections.actionWarehouseDetailFragmentToCreateWarehouseFragment(viewModel.warehouseObject.value)
                //findNavController().navigate(action)
            }

            //R.id.miOwnWarehouseDelete -> deleteOfOwnWarehouse()

            //R.id.miOwnWarehouseLeave -> leaveSharedWarehouse()

        }
        return super.onOptionsItemSelected(item)
    }

}