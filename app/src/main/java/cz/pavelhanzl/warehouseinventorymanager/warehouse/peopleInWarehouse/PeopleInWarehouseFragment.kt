package cz.pavelhanzl.warehouseinventorymanager.warehouse.peopleInWarehouse

import android.animation.Animator
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentPeopleInWarehouseBinding
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentWarehouseLogBinding
import cz.pavelhanzl.warehouseinventorymanager.invitations.InvitationsAdapter
import cz.pavelhanzl.warehouseinventorymanager.repository.*
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import cz.pavelhanzl.warehouseinventorymanager.service.observeInLifecycle
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseDetail.WarehouseDetailFragmentDirections
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseDetail.WarehousesDetailFragmentViewModel
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseLog.WarehouseLogFragmentArgs
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseLog.WarehouseLogFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PeopleInWarehouseFragment : BaseFragment() {

    private val args: PeopleInWarehouseFragmentArgs by navArgs()
    private lateinit var binding: FragmentPeopleInWarehouseBinding
    private val viewModel: PeopleInWarehouseFragmentViewModel by activityViewModels()
    private lateinit var usersInWarehouse: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //předá argumenty do viewmodelu
        if (savedInstanceState == null) {
            viewModel.setData(args.warehouseObject)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //binduje a přiřazuje viewmodel
        binding = FragmentPeopleInWarehouseBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        binding.fabAddUserToThisWhPeopleInWarehouseFragment.setOnClickListener {
            val action = PeopleInWarehouseFragmentDirections.actionPeopleInWarehouseFragmentToInviteUserFragment()
            findNavController().navigate(action)
        }


        setUpRecycleView()
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewmodel!!.eventsFlow
            .onEach {
                when (it) {
                    PeopleInWarehouseFragmentViewModel.Event.NavigateBack -> {
                        hideKeyboard(activity as MainActivity)
                        findNavController().navigateUp()
                    }
                    PeopleInWarehouseFragmentViewModel.Event.PlaySuccessAnimation -> {
                        playSuccessErrorAnimation(true)
                    }

                }
            }.observeInLifecycle(viewLifecycleOwner)
    }

    private fun playSuccessErrorAnimation(success: Boolean) {

        //nastaví odpovídající animaci
        if (success) {
            binding.lottieSucessErrorAnimPeopleInWarehouseFragment.setAnimation("success.json")

            //zavibruje
            vibratePhoneSuccess(requireContext())
        } else {
            binding.lottieSucessErrorAnimPeopleInWarehouseFragment.setAnimation("error.json")
            //zavibruje
            vibratePhoneError(requireContext())
        }

        //zobrazí a přehraje animaci
        binding.lottieSucessErrorAnimPeopleInWarehouseFragment.visibility = View.VISIBLE
        binding.lottieSucessErrorAnimPeopleInWarehouseFragment.playAnimation()

        //listenery
        binding.lottieSucessErrorAnimPeopleInWarehouseFragment.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
                //Log.d("Animation:", "start")

            }

            override fun onAnimationEnd(animation: Animator?) {
                //Log.d("Animation:", "end")
                //skryje animaci po dokončení
                try {
                    binding.lottieSucessErrorAnimPeopleInWarehouseFragment.visibility = View.GONE
                } catch (ex: Exception) {
                    ex.toString()
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
                //Log.e("Animation:", "cancel")
            }

            override fun onAnimationRepeat(animation: Animator?) {
                //Log.e("Animation:", "repeat")
            }
        })

    }

    private fun setUpRecycleView() {

        usersInWarehouse = db.collection(Constants.WAREHOUSES_STRING).document(args.warehouseObject.warehouseID).addSnapshotListener { snapshot, e ->

            if (e != null) {
                Log.w("Data pro adapteros", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d("Data pro adapteros", "Current data: ${snapshot.data}")

                //Nastaví live data na zvoleném skladě
                viewModel.warehouse=snapshot.toObject(Warehouse::class.java)!!

                //vytvoí adaptér na základě aktuálních dat
                val popleInWhAdapter = PeopleInWarehouseAdapter(viewModel.warehouse.users, viewModel.warehouse)

                //seststaví recycleview
                binding.rvPeopleListPeopleInWarehouseFragment.apply {
                    layoutManager = LinearLayoutManager(activity)
                    adapter = popleInWhAdapter
                }

            } else {
                Log.d("Data pro adapteros", "Current data: null")
            }

        }


    }

    override fun onDestroy() {
        Log.d("Data pro adapteros", "Odpojuji listener")
        usersInWarehouse.remove()
        hideKeyboard(requireActivity())
        super.onDestroy()
    }

}