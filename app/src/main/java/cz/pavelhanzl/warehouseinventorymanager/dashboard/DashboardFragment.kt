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
import cz.pavelhanzl.warehouseinventorymanager.repository.hideKeyboard
import kotlinx.android.synthetic.main.fragment_dashboard.*

class DashboardFragment : Fragment() {
    private lateinit var binding: FragmentDashboardBinding
    lateinit var viewModel: DashboardFragmentViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        hideKeyboard(requireActivity())

        //binduje a přiřazuje viewmodel
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

    fun navigateToOwnWarehouses(){
        val action = DashboardFragmentDirections.actionDashboardFragmentToListOfWarehousesFragment(true)
        findNavController().navigate(action)
    }

    fun navigateToSharedWarehouses(){
        val action = DashboardFragmentDirections.actionDashboardFragmentToListOfWarehousesFragment(false)
        findNavController().navigate(action)
    }

    fun navigateToRecievedInvitations(){
        val action = DashboardFragmentDirections.actionGlobalInvitationsFragmentRecieved()
        findNavController().navigate(action)
    }

    fun navigateToSendInvitations(){
        val action = DashboardFragmentDirections.actionGlobalInvitationsFragmentSend()
        findNavController().navigate(action)
    }

    fun navigateToSettings(){
        val action = DashboardFragmentDirections.navigateDashboardToSettings(5)
        findNavController().navigate(action)
    }

    fun navigateToAbout(){

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.seznam.cz/"))
        startActivity(intent)

    }







}