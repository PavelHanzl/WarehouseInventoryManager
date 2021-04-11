package cz.pavelhanzl.warehouseinventorymanager.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.EmailAuthProvider
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.dashboard.DashboardFragmentDirections
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentSettingsBinding
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentWarehouseDetailBinding
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import cz.pavelhanzl.warehouseinventorymanager.settings.SettingsFragmentArgs
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseDetail.WarehousesDetailFragmentViewModel
import kotlinx.android.synthetic.main.fragment_settings.view.*

/**
 * Settings fragment
 * Show user available settings for his account.
 * @constructor Create empty Settings fragment
 */
class SettingsFragment : BaseFragment() {

    val args: SettingsFragmentArgs by navArgs()
    private lateinit var binding: FragmentSettingsBinding
    private val viewModel: SettingsFragmentViewModel by activityViewModels()
    private var authByPasswordAndEmail:Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //binduje a přiřazuje viewmodel
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.fragmentClass = this
        binding.lifecycleOwner = viewLifecycleOwner

        //zobrazí možnost pro změnu hesla a emailu
        showOnlyIfNotUsingGoogleSignIn()


        return binding.root
    }

    /**
     * Show only if not using google sign in
     * Shows specific options to users which are not signed in with google account. Shows possibility to change password and change email.
     */
    private fun showOnlyIfNotUsingGoogleSignIn() {
        authByPasswordAndEmail = auth.currentUser!!.providerData[auth.currentUser!!.providerData.size - 1].providerId == "password"
        if (authByPasswordAndEmail) {
            binding.btnChangePasswordSettingsFragment.visibility = View.VISIBLE
            binding.btnChangeEmailSettingsFragment.visibility = View.VISIBLE
        }
    }

    /**
     * Navigates to change profile photo
     */
    fun navigateToChangeProfilePhoto(){
        val action = SettingsFragmentDirections.actionSettingsFragmentToChangePhotoFragment()
        findNavController().navigate(action)
    }

    /**
     * Navigates to change name
     */
    fun navigateToChangeName(){
        val action = SettingsFragmentDirections.actionSettingsFragmentToChangeNameFragment()
        findNavController().navigate(action)

    }

    /**
     * Navigates to change email
     */
    fun navigateToChangeEmail(){
        val action = SettingsFragmentDirections.actionSettingsFragmentToChangeEmailFragment()
        findNavController().navigate(action)

    }

    /**
     * Navigates to change password
     */
    fun navigateToChangePassword(){
        val action = SettingsFragmentDirections.actionSettingsFragmentToChangePasswordFragment()
        findNavController().navigate(action)
    }

}