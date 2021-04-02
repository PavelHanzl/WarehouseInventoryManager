package cz.pavelhanzl.warehouseinventorymanager.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.settings.SettingsFragmentArgs
import kotlinx.android.synthetic.main.fragment_settings.view.*

class SettingsFragment : Fragment() {

    val args: SettingsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val myNumber = args.number

        view.textviewsettings.setOnClickListener(){Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_aboutFragment)}
        view.textviewsettings.setText("${view.textviewsettings.text} ${myNumber.toString()}")
        return view
    }

}