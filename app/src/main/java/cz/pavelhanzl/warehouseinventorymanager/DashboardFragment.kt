package cz.pavelhanzl.warehouseinventorymanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_dashboard.view.*

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        llButtonOwnWarehouse.setOnClickListener {
            val action = DashboardFragmentDirections.actionDashboardFragmentToOwnWarehouseFragment(true)
            Navigation.findNavController(view).navigate(action)
        }

        llButtonSharedWarehouse.setOnClickListener {
            val action = DashboardFragmentDirections.actionDashboardFragmentToOwnWarehouseFragment(false)
            Navigation.findNavController(view).navigate(action)
        }

        llButtonSettings.setOnClickListener {
            val action = DashboardFragmentDirections.navigateDashboardToSettings(5)
            Navigation.findNavController(view).navigate(action)

        }

    }

}