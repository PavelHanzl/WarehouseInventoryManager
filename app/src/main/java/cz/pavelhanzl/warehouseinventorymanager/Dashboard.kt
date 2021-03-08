package cz.pavelhanzl.warehouseinventorymanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_dashboard.view.*

class Dashboard : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        view.llButtonSettings.setOnClickListener{
            val action = DashboardDirections.navigateDashboardToSettings(2459)
            Navigation.findNavController(view).navigate(action)
        }


        return view
    }

}