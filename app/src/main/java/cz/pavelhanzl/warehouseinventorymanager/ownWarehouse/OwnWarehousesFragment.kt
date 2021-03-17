package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import kotlinx.android.synthetic.main.fragment_own_warehouses.*
import kotlinx.android.synthetic.main.fragment_own_warehouses.view.*

class OwnWarehousesFragment : Fragment() {

    private var ownWarehousesList: List<Warehouse> = ArrayList()
    private  val ownWarehousesAdapter: OwnWarehousesAdapter = OwnWarehousesAdapter(ownWarehousesList)
    val db = Firebase.firestore

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
            val action =
                OwnWarehousesFragmentDirections.actionOwnWarehouseFragmentToCreateWarehouseFragment()
            Navigation.findNavController(view).navigate(action)
        }

        getOwnWarehousesData()
        rv_ownWarehousesList.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = ownWarehousesAdapter
        }
    }

    private fun getOwnWarehousesData() {
        db.collection("warehouses").addSnapshotListener { snapshot, error ->
            if(error != null){
                Log.d("OwnWarehousesRecycle", "Error: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                ownWarehousesList = snapshot.toObjects(Warehouse::class.java)
                ownWarehousesAdapter.ownWarehousesItems = ownWarehousesList
                ownWarehousesAdapter.notifyDataSetChanged()

            } else {
                Log.d("OwnWarehousesRecycle", "Error.")
            }
        }
    }


}