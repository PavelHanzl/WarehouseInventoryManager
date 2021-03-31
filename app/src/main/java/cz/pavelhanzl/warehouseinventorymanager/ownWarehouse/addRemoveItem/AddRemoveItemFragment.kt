package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.addRemoveItem

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentAddRemoveItemBinding
import cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.ownWarehouseDetail.OwnWarehousesDetailFragmentViewModel
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.hideKeyboard
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import cz.pavelhanzl.warehouseinventorymanager.service.observeInLifecycle
import kotlinx.android.synthetic.main.fragment_add_remove_item.*
import kotlinx.coroutines.flow.onEach

class AddRemoveItemFragment : BaseFragment() {
    private lateinit var binding: FragmentAddRemoveItemBinding
    private val sharedViewModel: OwnWarehousesDetailFragmentViewModel by activityViewModels()

    private lateinit var dropDownItemsMenu: AutoCompleteTextView
    private lateinit var dropDownBarcodesMenu: AutoCompleteTextView
    private lateinit var createItemBtn: FloatingActionButton

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

        dropDownItemsMenu = binding.dropdownItemSelectorContentAddRemoveFragment
        dropDownBarcodesMenu = binding.dropdownBarcodeSelectorContentAddRemoveFragment
        createItemBtn = binding.fabCreateItemAddRemoveFragment

        //pokud nastaví fragment pode toho jestli přidáváme nebo odebíráme
        when (sharedViewModel.addRemoveFragmentMode) {
            Constants.ADDING_STRING -> runFragmentInAddingMode()
            Constants.REMOVING_STRING -> runFragmentInRemovingMode()
        }

        //získá list všech aktuálních položek, které se nacházejí na skladě
        sharedViewModel.getListOfActualWarehouseItems()

        //pokud zadaný čárový kód odpovídá nějakému názvu, tak předvyplň název
        dropDownBarcodesMenu.doAfterTextChanged {
            var matchFound = false
            if(dropDownBarcodesMenu.text.toString() !="") {
                itemBarcodeCheckLoop@ for (item in sharedViewModel.localListOfAllItems) {
                    //pro každou položku se ptá jestli se zadaný text nerovná nějaké položce na listu všech položek
                    if (dropDownBarcodesMenu.text.toString() == item.code) {
                        createItemBtn.hide()//položka existuje skryje možnost vytvoření nové položky
                        Log.d("Hajdin", "Skrývám na barcodu")
                        if (dropDownItemsMenu.text.toString() != item.name) {//ochrana proti zacyklení
                            dropDownItemsMenu.setText(item.name)
                            Glide.with(requireContext())
                                .load(item.photoURL)
                                .placeholder(R.drawable.avatar_warehouse_item_primary_color)
                                .error(R.drawable.avatar_warehouse_item_primary_color)
                                .into(binding.ciItemProfileImageAddRemoveFragment)
                        }
                        matchFound = true
                        break@itemBarcodeCheckLoop//při první shodě ukončí forloop
                    } else {
                        createItemBtn.show()//položka neexistuje zobrazí možnost vytvoření nové položky
                        Log.d("Hajdin", "Odkrývám na barcodu")
                    }
                }

            if(!matchFound && dropDownItemsMenu.text.toString()!=""){
                dropDownItemsMenu.setText("")//pokud nedojde ke shodě barcodů, tak smaž obsah pole s názvem položky
            }
            }
        }

        //pokud zadaný název odpovídá nějakému čárovému kódu, tak předvyplň čárový kód
        dropDownItemsMenu.doAfterTextChanged {
            var matchFound = false
            if(dropDownItemsMenu.text.toString() !="") {
                itemNameCheckLoop@ for (item in sharedViewModel.localListOfAllItems) {
                    if (dropDownItemsMenu.text.toString() == item.name) {
                        createItemBtn.hide()//položka existuje skryje možnost vytvoření nové položky
                        Log.d("Hajdin", "Skrývám na dropdownu")
                        if (dropDownBarcodesMenu.text.toString() != item.code) {//ochrana proti zacyklení
                            dropDownBarcodesMenu.setText(item.code)
                            Glide.with(requireContext())
                                .load(item.photoURL)
                                .placeholder(R.drawable.avatar_warehouse_item_primary_color)
                                .error(R.drawable.avatar_warehouse_item_primary_color)
                                .into(binding.ciItemProfileImageAddRemoveFragment)
                        }
                        matchFound = true
                        break@itemNameCheckLoop //při první shodě ukončí forloop
                    } else {
                        createItemBtn.show()//položka neexistuje zobrazí možnost vytvoření nové položky
                        Log.d("Hajdin", "Odkrývám na dropdownu")
                    }
                }


            if(!matchFound && dropDownBarcodesMenu.text.toString()!=""){
                dropDownBarcodesMenu.setText("")//pokud nedojde ke shodě názvů, tak smaž obsah pole s barcodem
            }
            }

        }

        showHideFabCreateBtn()

        populateDropdowns()




        return binding.root
    }

    fun evaluateDropdownitem(){}



    private fun showHideFabCreateBtn() {
        dropDownItemsMenu.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && createItemBtn.visibility != View.VISIBLE && dropDownItemsMenu.text.toString() == "") {
                createItemBtn.show()

            } else if (!hasFocus && dropDownItemsMenu.text.toString() == "") {
                createItemBtn.hide()
            }

        }
    }

    //naplní dropdown menu aktualními položkami na skladě
    private fun populateDropdowns() {
        sharedViewModel.dropdownMenuDataReady.observe(viewLifecycleOwner, Observer {
            //názvy položek
            (binding.dropdownItemSelectorAddRemoveFragment.editText as? AutoCompleteTextView)?.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.dropdown_item_layout,
                    sharedViewModel.localListOfAllItemNames
                )
            )
            //čárové kódy
            (binding.dropdownBarcodeSelectorAddRemoveFragment.editText as? AutoCompleteTextView)?.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.dropdown_item_layout,
                    sharedViewModel.localListOfAllItemCodes
                )
            )
        })
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
        val action = AddRemoveItemFragmentDirections.actionAddRemoveItemToScannerFragment(Constants.READING_STRING)
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun runFragmentInAddingMode() {

        //nastaví odpovídající title v actionbaru "Ostatní sklady"
        (activity as MainActivity).supportActionBar!!.title = "Přidat položku na sklad"


    }

    private fun runFragmentInRemovingMode() {

        //nastaví odpovídající title v actionbaru "Ostatní sklady"
        (activity as MainActivity).supportActionBar!!.title = "Odebrat položku ze skladu"

        //přepíše hint u počtu
        binding.tfItemCountAddRemoveFragment.hint = "Zadejte počet odebíraných kusů"

    }


}