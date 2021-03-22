package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import kotlinx.android.synthetic.main.fragment_own_warehouses.*


class OwnWarehousesFragment : BaseFragment() {


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

        //tlačítko na vytvoření nového skladu
        fab_ownWarehouses_addNewOwnWarehouse.setOnClickListener {
            val action =
                OwnWarehousesFragmentDirections.actionOwnWarehouseFragmentToCreateWarehouseFragment()
            Navigation.findNavController(view).navigate(action)
        }

        //nastaví recycleview
        val query = db.collection("warehouses")
        val options = FirestoreRecyclerOptions.Builder<Warehouse>().setQuery(query, Warehouse::class.java).setLifecycleOwner(this).build()
        val ownWarehousesAdapter = OwnWarehousesAdapter(options)

        rv_ownWarehousesList.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = ownWarehousesAdapter
        }

    }


}