package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.ownWarehouseDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentOwnWarehouseDetailBinding
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import kotlinx.android.synthetic.main.menu_header.*

class OwnWarehouseDetailFragment : BaseFragment() {
    private lateinit var binding: FragmentOwnWarehouseDetailBinding
    lateinit var viewModel: OwnWarehousesDetailFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        if (savedInstanceState == null) {
            viewModel =
                ViewModelProvider(this).get(OwnWarehousesDetailFragmentViewModel::class.java)
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

        //observer na warehouse object, který když se změní, tak se upraví title pro tento fragment
        viewModel.warehouseObject.observe(viewLifecycleOwner, Observer { profilePhoto ->
            (activity as MainActivity).supportActionBar!!.title =
                viewModel.warehouseObject.value!!.name
        })


        return binding.root
    }


}