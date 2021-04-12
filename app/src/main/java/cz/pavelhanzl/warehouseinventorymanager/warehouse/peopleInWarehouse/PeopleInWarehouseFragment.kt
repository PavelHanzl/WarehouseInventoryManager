package cz.pavelhanzl.warehouseinventorymanager.warehouse.peopleInWarehouse

import android.animation.Animator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentPeopleInWarehouseBinding
import cz.pavelhanzl.warehouseinventorymanager.repository.*
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import cz.pavelhanzl.warehouseinventorymanager.service.observeInLifecycle
import kotlinx.coroutines.flow.onEach

/**
 * People in warehouse fragment
 *
 * @constructor Create empty People in warehouse fragment
 */
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

    /**
     * Play success or error animation
     *
     * @param success true for succes, false for error
     */
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

    /**
     * Sets up recycle view based on snapshot listener attached to current warehouse
     */
    private fun setUpRecycleView() {

        usersInWarehouse = db.collection(Constants.WAREHOUSES_STRING).document(args.warehouseObject.warehouseID).addSnapshotListener { snapshot, e ->

            if (e != null) {
                Log.w("Adapter", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d("Adapter", "Current data: ${snapshot.data}")

                //Nastaví live data na zvoleném skladě
                viewModel.warehouse = snapshot.toObject(Warehouse::class.java)!!

                if (viewModel.warehouse.users.isEmpty()) {
                    //skryje lottie s informací že zde není jiný user než owner
                    binding.nousersAnim.visibility = View.VISIBLE
                } else {
                    //zobrazí lottie s informací že zde není jiný user než owner
                    binding.nousersAnim.visibility = View.GONE
                }


                //vytvoí adaptér na základě aktuálních dat
                val popleInWhAdapter = PeopleInWarehouseAdapter(viewModel.warehouse.users, viewModel.warehouse)

                //seststaví recycleview
                binding.rvPeopleListPeopleInWarehouseFragment.apply {
                    layoutManager = LinearLayoutManager(activity)
                    adapter = popleInWhAdapter
                }

            } else {
                //zobrazí lottie s informací že zde není jiný user než owner
                binding.nousersAnim.visibility = View.VISIBLE
                Log.d("Adapter", "Current data: null")
            }
        }
    }

    override fun onDestroy() {
        //removes listener
        usersInWarehouse.remove()
        hideKeyboard(requireActivity())
        super.onDestroy()
    }

}