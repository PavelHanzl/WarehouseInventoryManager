package cz.pavelhanzl.warehouseinventorymanager

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_own_warehouses.*
import kotlinx.android.synthetic.main.fragment_own_warehouses.view.*

class ownWarehousesFragment : Fragment() {


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

        buttonToDashboard.setOnClickListener {
            val action = ownWarehousesFragmentDirections.actionOwnWarehouseFragmentToDashboardFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }


}