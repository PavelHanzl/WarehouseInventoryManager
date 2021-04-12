package cz.pavelhanzl.warehouseinventorymanager.warehouse.addRemoveItem

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
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

/**
 * Add remove item fragment
 *
 * @constructor Create empty Add remove item fragment
 */
class AddRemoveItemFragment : BaseFragment() {
    private lateinit var binding: FragmentAddRemoveItemBinding
    private val sharedViewModel: WarehousesDetailFragmentViewModel by activityViewModels()

    private lateinit var dropDownItemsMenuView: AutoCompleteTextView
    private lateinit var dropDownBarcodesMenuView: AutoCompleteTextView

    private lateinit var createItemBtn: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        //získá list všech aktuálních položek, které se nacházejí na skladě
        sharedViewModel.getListOfActualWarehouseItems()
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
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



        setUpFabCreateBtnAnimation()

        populateDropdowns()

        binding.fabCreateItemAddRemoveFragment.setOnClickListener {
            val action = AddRemoveItemFragmentDirections.actionAddRemoveItemToCreateEditItemFragment(sharedViewModel.warehouseID.value!!)
            findNavController().navigate(action)
        }

        return binding.root
    }

    /**
     * Sets up fab create btn animation
     * gives this button simple animation to draw more attention
     */
    private fun setUpFabCreateBtnAnimation() {
        val viewToAnimate = binding.fabCreateItemAddRemoveFragment

        val scaleDownAnimation: ObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(
            viewToAnimate,
            PropertyValuesHolder.ofFloat("scaleX", 1.1f),
            PropertyValuesHolder.ofFloat("scaleY", 1.1f)
        )
        scaleDownAnimation.duration = 700
        scaleDownAnimation.repeatCount = ObjectAnimator.INFINITE
        scaleDownAnimation.repeatMode = ObjectAnimator.REVERSE

        scaleDownAnimation.start()
    }

    /**
     * Register dropdowns do after text change listeners
     *
     */
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

    /**
     * Shows and hides floating action button to create new item
     *
     */
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

    /**
     * Populates the dropdown menus with current items in warehouse
     */
    private fun populateDropdowns() {
        sharedViewModel.dropdownMenuDataReady.observe(viewLifecycleOwner, Observer { dataReady ->
            if (dataReady) {

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
                            createItemBtn.show()
                        } else {
                            createItemBtn.hide()
                        }

                    }
                    WarehousesDetailFragmentViewModel.Event.PlaySuccessAnimation -> {
                        playSuccessErrorAnimation(true)
                    }
                    WarehousesDetailFragmentViewModel.Event.PlayErrorAnimation -> {
                        playSuccessErrorAnimation(false)
                    }
                }
            }.observeInLifecycle(viewLifecycleOwner)

        registerObserverForResultOfScanner()


    }

    /**
     * Register observer for result of scanner
     * Ensures the passing of the argument from the previous activity (in this case it gets the result if it comes from the scanner)
     * */
    private fun registerObserverForResultOfScanner() {//zajišťuje předání argumentu z předchozí aktivity (v tomto případě získá result pokud přichází ze skenneru)
        val navBackStackEntry = findNavController().getBackStackEntry(R.id.addRemoveItemFragment)

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

    /**
     * Navigates to scanner fragment
     */
    fun navigateToScanner() {
        val action = AddRemoveItemFragmentDirections.actionAddRemoveItemToScannerFragment(Constants.READING_STRING, null)
        Navigation.findNavController(requireView()).navigate(action)
    }

    /**
     * Runs fragment in adding mode
     */
    private fun runFragmentInAddingMode() {
        //nastaví odpovídající title v actionbaru "Ostatní sklady"
        (activity as MainActivity).supportActionBar!!.title = getString(R.string.addItemToWarehouse)

        //nastaví private variable o módu tohoto fragmentu
        sharedViewModel.addingMode = true

    }

    /**
     * Runs fragment in removing mode
     */
    private fun runFragmentInRemovingMode() {

        //nastaví odpovídající title v actionbaru "Ostatní sklady"
        (activity as MainActivity).supportActionBar!!.title = getString(R.string.removeItemFromWarehouse)

        //přepíše hint u počtu
        binding.tfItemCountAddRemoveFragment.hint = getString(R.string.enterNumberOfRemovedItems)

        //nastaví private variable o módu tohoto fragmentu
        sharedViewModel.addingMode = false

    }

    /**
     * Plays success or error animation
     *
     * @param success true for success, false for error
     */
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