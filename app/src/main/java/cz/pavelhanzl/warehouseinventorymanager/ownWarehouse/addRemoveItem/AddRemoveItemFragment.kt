package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.addRemoveItem

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.Query
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentAddRemoveItemBinding
import cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.ownWarehouseDetail.OwnWarehousesDetailFragmentViewModel
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.WarehouseItem
import cz.pavelhanzl.warehouseinventorymanager.repository.hideKeyboard
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import cz.pavelhanzl.warehouseinventorymanager.service.observeInLifecycle
import kotlinx.android.synthetic.main.fragment_add_remove_item.*
import kotlinx.coroutines.flow.onEach

class AddRemoveItemFragment : BaseFragment() {
    private lateinit var binding: FragmentAddRemoveItemBinding
    private val sharedViewModel: OwnWarehousesDetailFragmentViewModel by activityViewModels()

    private lateinit var dropDownMenu: AutoCompleteTextView
    private lateinit var barcodeEditText: TextInputEditText

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

        dropDownMenu = binding.dropdownItemSelectorContentAddRemoveFragment
        barcodeEditText = binding.tfBarcodeValueContentAddRemoveFragment

        //pokud nastaví fragment pode toho jestli přidáváme nebo odebíráme
        when (sharedViewModel.addRemoveFragmentMode) {
            Constants.ADDING_STRING -> runFragmentInAddingMode()
            Constants.REMOVING_STRING -> runFragmentInRemovingMode()
        }

        //získá list všech aktuálních položek, které se nacházejí na skladě
        sharedViewModel.getListOfActualWarehouseItems()

        //pokud zadaný čárový kód odpovídá nějakému názvu, tak předvyplň název
        barcodeEditText.doAfterTextChanged {
            for (item in sharedViewModel.localListOfAllItems) {
                //pro každou položku se ptá jestli se zadaný text nerovná nějaké položce na listu všech položek
                if (barcodeEditText.text.toString() == item.code) {
                    if (dropDownMenu.text.toString() != item.name) {//ochrana proti zacyklení
                        dropDownMenu.setText(item.name)
                    }
                }

            }
        }

        //pokud zadaný název odpovídá nějakému čárovému kódu, tak předvyplň čárový kód
        dropDownMenu.doAfterTextChanged {
            for (item in sharedViewModel.localListOfAllItems) {
                if (dropDownMenu.text.toString() == item.name) {
                    if (barcodeEditText.text.toString() != item.code) {//ochrana proti zacyklení
                        barcodeEditText.setText(item.code)
                    }
                }
            }
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item_layout, sharedViewModel.localListOfAllItemNames)
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

    fun navigateToScanner() {
        val action = AddRemoveItemFragmentDirections.actionAddRemoveItemToScannerFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun runFragmentInAddingMode() {

        //nastaví odpovídající title v actionbaru "Ostatní sklady"
        (activity as MainActivity).supportActionBar!!.title = "Přidat položku na sklad"


    }

    private fun runFragmentInRemovingMode() {

        //nastaví odpovídající title v actionbaru "Ostatní sklady"
        (activity as MainActivity).supportActionBar!!.title = "Odebrat položku ze skladu"

    }


}