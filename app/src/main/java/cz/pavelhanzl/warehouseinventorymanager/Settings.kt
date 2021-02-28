package cz.pavelhanzl.warehouseinventorymanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*

class Settings : Fragment() {

    val args: SettingsArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val myNumber = args.number

        view.textviewsettings.setOnClickListener(){Navigation.findNavController(view).navigate(R.id.navigate_settings_to_dashboard)}
        view.textviewsettings.setText("${view.textviewsettings.text} ${myNumber.toString()}")
        return view
    }

}