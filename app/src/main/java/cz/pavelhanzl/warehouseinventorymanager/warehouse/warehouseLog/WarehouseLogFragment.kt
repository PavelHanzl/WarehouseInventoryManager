package cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseLog

import android.app.DownloadManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentCreateEditItemBinding
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentWarehouseLogBinding
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import cz.pavelhanzl.warehouseinventorymanager.repository.WarehouseLogItem
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import cz.pavelhanzl.warehouseinventorymanager.warehouse.createEditItem.CreateEditItemFragmentArgs
import cz.pavelhanzl.warehouseinventorymanager.warehouse.createEditItem.CreateEditItemFragmentViewModel
import cz.pavelhanzl.warehouseinventorymanager.warehouse.listOfWarehouses.ListOfWarehousesAdapter

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



        setUpRecycleView()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun setUpRecycleView() {

        //nastaví recycleview query pro recycle view
        val query = db.collection(Constants.WAREHOUSES_STRING).document(args.warehouseObject.warehouseID).collection(Constants.LOG_STRING).orderBy("date", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<WarehouseLogItem>().setQuery(query, WarehouseLogItem::class.java).setLifecycleOwner(this).build()
        val warehouseLogAdapter = WarehouseLogAdapter(options)


        binding.rvLogListWarehouseLogFragment.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = warehouseLogAdapter
        }
    }


}