package cz.pavelhanzl.warehouseinventorymanager

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import cz.pavelhanzl.warehouseinventorymanager.repository.MainRepository
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import kotlinx.android.synthetic.main.fragment_own_warehouses.*
import kotlinx.android.synthetic.main.fragment_own_warehouses.view.*

class OwnWarehousesFragment : Fragment() {

    private val MainRepository = MainRepository()
    private var ownWarehousesList: List<Warehouse> = ArrayList()
    private  val ownWarehousesAdapter: OwnWarehousesAdapter = OwnWarehousesAdapter(ownWarehousesList)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_own_warehouses, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab_ownWarehouses_addNewOwnWarehouse.setOnClickListener {
            val action = OwnWarehousesFragmentDirections.actionOwnWarehouseFragmentToDashboardFragment()
            Navigation.findNavController(view).navigate(action)
        }

        getOwnWarehousesData()
        rv_ownWarehousesList.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = ownWarehousesAdapter
        }
    }

    private fun getOwnWarehousesData() {
        MainRepository.getOwnWarehouses().addOnCompleteListener {
            if (it.isSuccessful) {
                ownWarehousesList = it.result!!.toObjects(Warehouse::class.java)
                ownWarehousesAdapter.ownWarehousesItems = ownWarehousesList
                ownWarehousesAdapter.notifyDataSetChanged()

            } else {
                Log.d("OwnWarehousesRecycle", "Error: ${it.exception!!.message}")
            }
        }
    }


}