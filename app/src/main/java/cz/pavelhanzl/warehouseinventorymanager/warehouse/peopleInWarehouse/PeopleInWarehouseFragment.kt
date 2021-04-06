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
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentPeopleInWarehouseBinding
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentWarehouseLogBinding
import cz.pavelhanzl.warehouseinventorymanager.repository.hideKeyboard
import cz.pavelhanzl.warehouseinventorymanager.repository.vibratePhoneError
import cz.pavelhanzl.warehouseinventorymanager.repository.vibratePhoneSuccess
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import cz.pavelhanzl.warehouseinventorymanager.service.observeInLifecycle
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseDetail.WarehouseDetailFragmentDirections
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseDetail.WarehousesDetailFragmentViewModel
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseLog.WarehouseLogFragmentArgs
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseLog.WarehouseLogFragmentViewModel
import kotlinx.coroutines.flow.onEach

class PeopleInWarehouseFragment : BaseFragment() {

    private val args: PeopleInWarehouseFragmentArgs by navArgs()
    private lateinit var binding: FragmentPeopleInWarehouseBinding
    private val viewModel: PeopleInWarehouseFragmentViewModel by activityViewModels()


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
            val action =  PeopleInWarehouseFragmentDirections.actionPeopleInWarehouseFragmentToInviteUserFragment()
            findNavController().navigate(action)
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewmodel!!.eventsFlow
            .onEach {
                when (it) {
                    PeopleInWarehouseFragmentViewModel.Event.NavigateBack -> {
                        findNavController().navigateUp()
                        hideKeyboard(activity as MainActivity)
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


}