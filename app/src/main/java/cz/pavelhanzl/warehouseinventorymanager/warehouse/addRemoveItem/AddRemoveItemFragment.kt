package cz.pavelhanzl.warehouseinventorymanager.warehouse.addRemoveItem

import android.animation.Animator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
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
import cz.pavelhanzl.warehouseinventorymanager.repository.*
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import cz.pavelhanzl.warehouseinventorymanager.service.observeInLifecycle
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseDetail.WarehousesDetailFragmentViewModel
import kotlinx.android.synthetic.main.fragment_add_remove_item.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.onEach

class AddRemoveItemFragment : BaseFragment() {
    private lateinit var binding: FragmentAddRemoveItemBinding
    private val sharedViewModel: WarehousesDetailFragmentViewModel by activityViewModels()

    private lateinit var dropDownItemsMenuView: AutoCompleteTextView
    private lateinit var dropDownBarcodesMenuView: AutoCompleteTextView

    private lateinit var createItemBtn: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("AddRemove", "create" + sharedViewModel.localListOfAllItems.size.toString())
        //získá list všech aktuálních položek, které se nacházejí na skladě
        sharedViewModel.getListOfActualWarehouseItems()
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        Log.d("AddRemove", "destroy" + sharedViewModel.localListOfAllItems.size.toString())
        sharedViewModel.initVariablesForAddRemoveFragment()
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("AddRemove", "createView" + sharedViewModel.localListOfAllItems.size.toString())
        binding = FragmentAddRemoveItemBinding.inflate(inflater, container, false)
        binding.sharedViewmodel = sharedViewModel
        binding.fragmentClass = this
        binding.lifecycleOwner = viewLifecycleOwner

        dropDownItemsMenuView = binding.dropdownItemSelectorContentAddRemoveFragment
        dropDownBarcodesMenuView = binding.dropdownBarcodeSelectorContentAddRemoveFragment
        createItemBtn = binding.fabCreateItemAddRemoveFragment

        //pokud nastaví fragment pode toho jestli přidáváme nebo odebíráme
        when (sharedViewModel.addRemoveFragmentMode) {
            Constants.ADDING_STRING -> runFragmentInAddingMode()
            Constants.REMOVING_STRING -> runFragmentInRemovingMode()
        }

        registerDropdownsDoAfterTextChangeListeners()

        showHideFabCreateBtn()

        populateDropdowns()

        binding.fabCreateItemAddRemoveFragment.setOnClickListener {
            Log.d("create item", "pressed")
            val action = AddRemoveItemFragmentDirections.actionAddRemoveItemToCreateEditItemFragment(sharedViewModel.warehouseID.value!!)
            findNavController().navigate(action)
        }


        return binding.root
    }

    private fun registerDropdownsDoAfterTextChangeListeners() {
        //pokud zadaný čárový kód odpovídá nějakému názvu, tak předvyplň název
        dropDownBarcodesMenuView.doAfterTextChanged {
            if (dropDownBarcodesMenuView.hasFocus()) {//trigne se jen pokud je aktivní view na kterém je nasazen doAfterTextChanged

                val barcodeString = it.toString()
                sharedViewModel.setDropdownsBasedOnBarcode(barcodeString)

            }
        }

        //pokud zadaný název odpovídá nějakému čárovému kódu, tak předvyplň čárový kód
        dropDownItemsMenuView.doAfterTextChanged {
            if (dropDownItemsMenuView.hasFocus()) {//trigne se jen pokud je aktivní view na kterém je nasazen doAfterTextChanged

                val itemNameString = it.toString()
                sharedViewModel.setDropdownsBasedOnName(itemNameString)

            }
        }
    }

    private fun evaluateItems(
        activeDropdown: AutoCompleteTextView,
        relativeDropdown: AutoCompleteTextView,
        localListOfAllItems: MutableList<WarehouseItem>,
        activeParam: String,
        relativeParam: String
    ) {
        //relative barcode
        //active items

        var matchFound = false
        //proveď jen pokud současné pole není prázný string
        if (activeDropdown.text.toString() != "") {
            itemCheckLoop@ for (item in localListOfAllItems) {

                val param1: String? = item.getField<String>(activeParam)
                val param2: String? = item.getField<String>(relativeParam)

                if (activeDropdown.text.toString() == param1) {
                    createItemBtn.hide()//položka existuje skryje možnost vytvoření nové položky
                    Log.d("Hajdisn", "Skrývám na dropdownu")
                    if (relativeDropdown.text.toString() != param2) {//ochrana proti zacyklení
                        relativeDropdown.setText(param2)
                        Glide.with(requireContext())
                            .load(item.photoURL)
                            .placeholder(R.drawable.avatar_warehouse_item_primary_color)
                            .error(R.drawable.avatar_warehouse_item_primary_color)
                            .into(binding.ciItemProfileImageAddRemoveItemFragment)
                    }
                    matchFound = true
                    break@itemCheckLoop //při první shodě ukončí forloop
                } else if (sharedViewModel.addRemoveFragmentMode == Constants.ADDING_STRING) { // zobrazí fab s možností vytvoření  pouze pokud jsme v módu pro přidání položky na sklad
                    createItemBtn.show()//položka neexistuje zobrazí možnost vytvoření nové položky
                    Log.d("Hajdin", "Odkrývám na dropdownu")
                } else if (sharedViewModel.addRemoveFragmentMode == Constants.REMOVING_STRING) {
                    //pokud nedošlo ke shodě, zobraz v avataru defaultní obrázek
                    Glide.with(requireContext())
                        .load(R.drawable.avatar_warehouse_item_primary_color)
                        .into(binding.ciItemProfileImageAddRemoveItemFragment)
                }
            }

            //se nenašla shoda a pokud druhé pole již není prázný string, tak zapiš prázdný string do druhého pole
            if (!matchFound && relativeDropdown.text.toString() != "") {
                Log.d("Hajdin", "dropdownwipe")
                Log.d("Hajdin", "Hodnota:" + sharedViewModel.itemBarcodeContent.value.toString())
                relativeDropdown.setText("")//pokud nedojde ke shodě názvů, tak smaž obsah pole s barcodem
                Log.d("Hajdin", "Hodnota:" + sharedViewModel.itemBarcodeContent.value.toString())
            }
        }

    }

    private fun showHideFabCreateBtn() {
        dropDownItemsMenuView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus //pole pro výběr názvu má focus
                && createItemBtn.visibility != View.VISIBLE //fab pro vytvoření ještě není viditelný
                && dropDownItemsMenuView.text.toString() == "" //pole pro výběr názvu nic neobsahuje
                && sharedViewModel.addRemoveFragmentMode == Constants.ADDING_STRING //fragment je v modu přidávání
            ) {
                createItemBtn.show()
            } else if (!hasFocus //pole pro výběr názvu ztratilo focus
                && dropDownItemsMenuView.text.toString() == "" //pole pro výběr názvu nic neobsahuje
            ) {
                createItemBtn.hide()
            }

        }
    }

    //naplní dropdown menu aktualními položkami na skladě
    private fun populateDropdowns() {
        sharedViewModel.dropdownMenuDataReady.observe(viewLifecycleOwner, Observer { dataReady ->
            if (dataReady) {

                Log.d("Populuju", "populuju ted")
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
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.sharedViewmodel!!.eventsFlow
            .onEach {
                when (it) {
                    WarehousesDetailFragmentViewModel.Event.NavigateBack -> {
                        findNavController().navigateUp()
                        hideKeyboard(activity as MainActivity)
                    }
                    is WarehousesDetailFragmentViewModel.Event.SetVisibilityOfCreateItemBtnt -> {
                        if (it.visibility) {
                            Log.d("create", it.visibility.toString())
                            createItemBtn.show()
                        } else {
                            Log.d("create", it.visibility.toString())
                            createItemBtn.hide()
                        }

                    }
                    WarehousesDetailFragmentViewModel.Event.PlaySuccessAnimation -> {
                        playSuccessErrorAnimation(true)
                    }
                    WarehousesDetailFragmentViewModel.Event.PlayErrorAnimation -> {
                        playSuccessErrorAnimation(false)
                    }
                    /* is WarehousesDetailFragmentViewModel.Event.CreateEditDebt -> {
                        val action = FriendDetailFragmentDirections.actionFriendDetailFragmentToAddEditDebtFragment(it.debtID,
                            viewModel.friendshipData.value!!,
                            viewModel.friendData.value!!.name)
                        Navigation.findNavController(view).navigate(action)
                    }*/
                }
            }.observeInLifecycle(viewLifecycleOwner)

        registerObserverForResultOfScanner()


    }

    override fun onResume() {
        Log.d("AddRemove", "resume" + sharedViewModel.localListOfAllItems.size.toString())
        super.onResume()
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

                // Uloží předaný argument ze skenneru do proměnné barcode v sharedviewmodelu
                sharedViewModel.itemBarcodeContent.postValue(result)
                //nastaví dropdowny v závislosti na získané hodnotě ze skenneru
                sharedViewModel.setDropdownsBasedOnBarcode(result!!)
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

        //nastaví private variable o módu tohoto fragmentu
        sharedViewModel.addingMode = true

    }

    private fun runFragmentInRemovingMode() {

        //nastaví odpovídající title v actionbaru "Ostatní sklady"
        (activity as MainActivity).supportActionBar!!.title = "Odebrat položku ze skladu"

        //přepíše hint u počtu
        binding.tfItemCountAddRemoveFragment.hint = "Zadejte počet odebíraných kusů"

        //nastaví private variable o módu tohoto fragmentu
        sharedViewModel.addingMode = false

    }

    private fun playSuccessErrorAnimation(success: Boolean) {

        //nastaví odpovídající animaci
        if (success) {
            binding.lottieSucessErrorAnimAddRemoveItemFragment.setAnimation("success.json")

            //zavibruje
            vibratePhoneSuccess(requireContext())
        } else {
            binding.lottieSucessErrorAnimAddRemoveItemFragment.setAnimation("error.json")
            //zavibruje
            vibratePhoneError(requireContext())
        }



        binding.lottieSucessErrorAnimAddRemoveItemFragment.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
                //Log.d("Animation:", "start")

            }

            override fun onAnimationEnd(animation: Animator?) {
                //Log.d("Animation:", "end")
                //skryje animaci po dokončení
                try {
                    binding.lottieSucessErrorAnimAddRemoveItemFragment.visibility = GONE
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

        //zobrazí a přehraje animaci
        binding.lottieSucessErrorAnimAddRemoveItemFragment.visibility = VISIBLE
        binding.lottieSucessErrorAnimAddRemoveItemFragment.playAnimation()
    }


}