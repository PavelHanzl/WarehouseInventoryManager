package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.addRemoveItem


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.Query
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentAddRemoveItemBinding
import cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.ownWarehouseDetail.OwnWarehousesDetailFragmentViewModel
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.hideKeyboard
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import cz.pavelhanzl.warehouseinventorymanager.service.observeInLifecycle
import kotlinx.coroutines.flow.onEach

class AddRemoveItemFragment : BaseFragment() {
    private lateinit var binding: FragmentAddRemoveItemBinding
    private val sharedViewModel: OwnWarehousesDetailFragmentViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        Log.d("destroj", "ted")
        sharedViewModel.initVariablesForAddRemoveFragment()
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAddRemoveItemBinding.inflate(inflater, container, false)
        binding.sharedViewmodel = sharedViewModel
        binding.fragmentClass = this
        binding.lifecycleOwner = viewLifecycleOwner


        //pokud nastaví fragment pode toho jestli přidáváme nebo odebíráme
        when(sharedViewModel.addRemoveFragmentMode){
            Constants.ADDING_STRING -> runFragmentInAddingMode()
            Constants.REMOVING_STRING -> runFragmentInRemovingMode()
        }



        val listOfAllItemNames: MutableList<String> = mutableListOf()


        val allItems =  db.collection("warehouses").document(sharedViewModel.warehouseID.value!!).collection("items").orderBy("name", Query.Direction.ASCENDING).get()


        allItems.addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("položky", "${document.id} => ${document.data}")

                    listOfAllItemNames.add(document.data.getValue("name").toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.d("položky", "Error getting documents: ", exception)
            }




        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item_layout, listOfAllItemNames)
        (binding.dropdownItemSelectorAddRemoveFragment.editText as? AutoCompleteTextView)?.setAdapter(adapter)



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.sharedViewmodel!!.eventsFlow
            .onEach {
                when (it) {
                    OwnWarehousesDetailFragmentViewModel.Event.NavigateBack -> {
                        findNavController().navigateUp()
                        hideKeyboard(activity as MainActivity)
                    }
                    /* is OwnWarehousesDetailFragmentViewModel.Event.CreateEditDebt -> {
                        val action = FriendDetailFragmentDirections.actionFriendDetailFragmentToAddEditDebtFragment(it.debtID,
                            viewModel.friendshipData.value!!,
                            viewModel.friendData.value!!.name)
                        Navigation.findNavController(view).navigate(action)
                    }*/
                }
            }.observeInLifecycle(viewLifecycleOwner)

        registerObserverForResultOfScanner()

    }

    //zajišťuje předání argumentu z předchozí aktivity (v tomto případě získá result pokud přichází ze skenneru)
    private fun registerObserverForResultOfScanner() {
        val navBackStackEntry = findNavController().getBackStackEntry(R.id.addRemoveItem)

        // Create observer and add it to the NavBackStackEntry's lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME
                && navBackStackEntry.savedStateHandle.contains("scannedBarcode")
            ) {
                val result = navBackStackEntry.savedStateHandle.get<String>("scannedBarcode")
                // Uloží předaný argument ze skenneru do proměnné sharedviewmodelu
                sharedViewModel.itemBarcodeContent.postValue(result)

            }
        }
        navBackStackEntry.lifecycle.addObserver(observer)

        // As addObserver() does not automatically remove the observer, we
        // call removeObserver() manually when the view lifecycle is destroyed
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                navBackStackEntry.lifecycle.removeObserver(observer)
            }
        })
    }

    fun navigateToScanner(){
        val action = AddRemoveItemFragmentDirections.actionAddRemoveItemToScannerFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }


    private fun runFragmentInAddingMode(){

        //nastaví odpovídající title v actionbaru "Ostatní sklady"
        (activity as MainActivity).supportActionBar!!.title = "Přidat položku na sklad"



    }
    private fun runFragmentInRemovingMode(){

        //nastaví odpovídající title v actionbaru "Ostatní sklady"
        (activity as MainActivity).supportActionBar!!.title = "Odebrat položku ze skladu"

    }






}