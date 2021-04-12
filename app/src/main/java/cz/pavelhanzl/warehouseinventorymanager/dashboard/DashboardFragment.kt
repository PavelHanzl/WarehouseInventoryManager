package cz.pavelhanzl.warehouseinventorymanager.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentDashboardBinding
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.hideKeyboard
import kotlinx.android.synthetic.main.fragment_dashboard.*

/**
 * Dashboard fragment
 *
 * @constructor Create empty Dashboard fragment
 */
class DashboardFragment : Fragment() {
    private lateinit var binding: FragmentDashboardBinding
    lateinit var viewModel: DashboardFragmentViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        hideKeyboard(requireActivity())

        //binds and assigns a viewmodel
        viewModel = ViewModelProvider(this).get(DashboardFragmentViewModel::class.java)
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.fragmentClass = this
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.registrateBadgeListeners()
        // Inflate the layout for this fragment
        return binding.root


    }

    override fun onDestroyView() {
        viewModel.unregistrateBadgeListeners()
        super.onDestroyView()
    }

    /**
     * Navigate to own warehouses
     */
    fun navigateToOwnWarehouses(){
        val action = DashboardFragmentDirections.actionDashboardFragmentToListOfWarehousesFragment(true)
        findNavController().navigate(action)
    }

    /**
     * Navigate to shared warehouses
     */
    fun navigateToSharedWarehouses(){
        val action = DashboardFragmentDirections.actionDashboardFragmentToListOfWarehousesFragment(false)
        findNavController().navigate(action)
    }

    /**
     * Navigate to recieved invitations
     */
    fun navigateToRecievedInvitations(){
        val action = DashboardFragmentDirections.actionGlobalInvitationsFragmentRecieved()
        findNavController().navigate(action)
    }

    /**
     * Navigate to send invitations
     */
    fun navigateToSendInvitations(){
        val action = DashboardFragmentDirections.actionGlobalInvitationsFragmentSend()
        findNavController().navigate(action)
    }

    /**
     * Navigate to settings
     */
    fun navigateToSettings(){
        val action = DashboardFragmentDirections.navigateDashboardToSettings(5)
        findNavController().navigate(action)
    }

    /**
     * Navigate to about
     * Starts new intent, opening web browser with website of project.
     */
    fun navigateToAbout(){
        //val action = DashboardFragmentDirections.actionDashboardFragmentToAboutFragment()
        //findNavController().navigate(action)

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.PROJECTS_WEBSITE_URL))
        startActivity(intent)
    }







}