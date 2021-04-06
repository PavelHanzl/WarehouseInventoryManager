package cz.pavelhanzl.warehouseinventorymanager.invitations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentInvitationsBinding
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentWarehouseLogBinding
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.Invitation
import cz.pavelhanzl.warehouseinventorymanager.repository.WarehouseLogItem
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseLog.WarehouseLogAdapter
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseLog.WarehouseLogFragmentViewModel

class InvitationsFragment : BaseFragment() {

    private lateinit var binding: FragmentInvitationsBinding
    lateinit var viewModel: InvitationsFragmentViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //binduje a přiřazuje viewmodel
        binding = FragmentInvitationsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(InvitationsFragmentViewModel::class.java)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner






        setUpRecycleView()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun setUpRecycleView() {

        //nastaví recycleview query pro recycle view
        val query = db.collection(Constants.INVITATIONS_STRING).whereEqualTo("to", auth.currentUser!!.uid).orderBy("date", Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions.Builder<Invitation>().setQuery(query, Invitation::class.java).setLifecycleOwner(this).build()
        val invitationsAdapter = InvitationsAdapter(options)


        binding.rvInvitationsListInvitationsFragment.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = invitationsAdapter
        }
    }

   
}