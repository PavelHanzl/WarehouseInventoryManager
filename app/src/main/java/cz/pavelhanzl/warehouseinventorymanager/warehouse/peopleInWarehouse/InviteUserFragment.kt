package cz.pavelhanzl.warehouseinventorymanager.warehouse.peopleInWarehouse

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentInviteUserBinding
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentWarehouseLogBinding
import cz.pavelhanzl.warehouseinventorymanager.repository.RepoComunicationLayer
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseDetail.WarehousesDetailFragmentViewModel
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseLog.WarehouseLogFragmentArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class InviteUserFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentInviteUserBinding

    val db = Firebase.firestore
    val auth = Firebase.auth
    val storage = Firebase.storage.reference
    val repoComunicationLayer = RepoComunicationLayer()

    private val viewModel: PeopleInWarehouseFragmentViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//binduje a přiřazuje viewmodel
        binding = FragmentInviteUserBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        binding.fabSendInvitationFragmentInviteUser.setOnClickListener {
            Log.d("btn pressed","fab btn pressed")
            GlobalScope.launch(Dispatchers.IO) {
                viewModel.inviteUser()
            }
        }

        // Inflate the layout for this fragment
        return binding.root
    }


}