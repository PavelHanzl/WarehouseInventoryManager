package cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseDetail

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ListenerRegistration
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentWarehouseDetailBinding
import cz.pavelhanzl.warehouseinventorymanager.generated.callback.OnClickListener
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.WarehouseItem
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import kotlinx.android.synthetic.main.fragment_warehouse_detail.*

/**
 * Warehouse detail fragment
 *
 * @constructor Create empty Warehouse detail fragment
 */
class WarehouseDetailFragment : BaseFragment() {

    private val args: WarehouseDetailFragmentArgs by navArgs()
    private lateinit var binding: FragmentWarehouseDetailBinding
    private val viewModel: WarehousesDetailFragmentViewModel by activityViewModels()

    private val fabAddItemAnimFromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.fab_from_bottom
        )
    }
    private val fabAddItemAnimToBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.fab_to_bottom
        )
    }

    private val fabRemoveItemAnimFromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.fab_from_bottom
        )
    }
    private val fabRemoveItemAnimToBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.fab_to_bottom
        )
    }

    private var addItemClicked = false
    private var removeItemClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        //předá argumenty do viewmodelu
        if (savedInstanceState == null) {
            viewModel.setData(args.warehouseID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //definuje počáteční stav fab tlačítek pro přidání/odebírání
        addItemClicked = false
        removeItemClicked = false

        //binduje a přiřazuje viewmodel
        binding = FragmentWarehouseDetailBinding.inflate(
            inflater,
            container,
            false
        )
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        //observer na warehouse object, který když se změní, tak se upraví title pro tento fragment
        viewModel.warehouseObject.observe(
            viewLifecycleOwner,
            Observer { profilePhoto ->
                (activity as MainActivity).supportActionBar!!.title =
                    viewModel.warehouseObject.value!!.name
            })



        setOnClickListenersOnAddRemoveButtons()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecycleView()
    }

    /**
     * Set on click listeners on add remove buttons
     *
     */
    private fun setOnClickListenersOnAddRemoveButtons() {
        binding.fabOwnWhDetailAddItem.setOnClickListener {
            onAddOrRemoveItemButtonClicked(
                addItemClicked,
                Constants.ADD_STRING
            )
        }

        binding.fabOwnWhDetailAddItemByHand.setOnClickListener {
            viewModel.addRemoveFragmentMode = Constants.ADDING_STRING
            val action =
                WarehouseDetailFragmentDirections.actionWarehouseDetailFragmentToAddRemoveItem()
            findNavController().navigate(action)
        }

        binding.fabOwnWhDetailAddItemByScan.setOnClickListener {
            viewModel.addRemoveFragmentMode = Constants.ADDING_STRING
            val action =
                WarehouseDetailFragmentDirections.actionWarehouseDetailFragmentToScannerFragment(
                    Constants.ADDING_STRING,
                    viewModel.warehouseObject.value
                )
            findNavController().navigate(action)
        }

        binding.fabOwnWhDetailRemoveItem.setOnClickListener {

            onAddOrRemoveItemButtonClicked(
                removeItemClicked,
                Constants.REMOVE_STRING
            )
        }

        binding.fabOwnWhDetailRemoveItemByHand.setOnClickListener {
            viewModel.addRemoveFragmentMode = Constants.REMOVING_STRING
            val action =
                WarehouseDetailFragmentDirections.actionWarehouseDetailFragmentToAddRemoveItem()
            findNavController().navigate(action)
        }

        binding.fabOwnWhDetailRemoveItemByScan.setOnClickListener {
            viewModel.addRemoveFragmentMode = Constants.REMOVING_STRING
            val action =
                WarehouseDetailFragmentDirections.actionWarehouseDetailFragmentToScannerFragment(
                    Constants.REMOVING_STRING,
                    viewModel.warehouseObject.value
                )
            findNavController().navigate(action)
        }
    }

    /**
     * Handles on add or remove item button clicked
     *
     * @param clicked
     * @param operation
     */
    private fun onAddOrRemoveItemButtonClicked(
        clicked: Boolean,
        operation: String
    ) {

        when (operation) {
            Constants.ADD_STRING -> addItemClicked = !addItemClicked
            Constants.REMOVE_STRING -> removeItemClicked = !removeItemClicked
        }

        setVisibility(clicked, operation)
        setAnimation(clicked, operation)
    }

    /**
     * Sets animation for add or remove buttons
     *
     * @param clicked
     * @param operation
     */
    private fun setAnimation(clicked: Boolean, operation: String) {
        when (operation) {
            Constants.ADD_STRING -> {
                if (!clicked) {
                    binding.fabOwnWhDetailAddItemByScan.startAnimation(
                        fabAddItemAnimFromBottom
                    )
                    binding.fabOwnWhDetailAddItemByHand.startAnimation(
                        fabAddItemAnimFromBottom
                    )
                } else {
                    binding.fabOwnWhDetailAddItemByScan.startAnimation(
                        fabAddItemAnimToBottom
                    )
                    binding.fabOwnWhDetailAddItemByHand.startAnimation(
                        fabAddItemAnimToBottom
                    )
                }
            }
            Constants.REMOVE_STRING -> {
                if (!clicked) {
                    binding.fabOwnWhDetailRemoveItemByScan.startAnimation(
                        fabRemoveItemAnimFromBottom
                    )
                    binding.fabOwnWhDetailRemoveItemByHand.startAnimation(
                        fabRemoveItemAnimFromBottom
                    )
                } else {
                    binding.fabOwnWhDetailRemoveItemByScan.startAnimation(
                        fabRemoveItemAnimToBottom
                    )
                    binding.fabOwnWhDetailRemoveItemByHand.startAnimation(
                        fabRemoveItemAnimToBottom
                    )
                }
            }
        }

    }

    /**
     * Set visibility for add or remove buttons
     *
     * @param clicked
     * @param operation
     */
    private fun setVisibility(clicked: Boolean, operation: String) {
        when (operation) {
            Constants.ADD_STRING -> {
                if (!clicked) {
                    binding.fabOwnWhDetailAddItemByScan.show()
                    binding.fabOwnWhDetailAddItemByHand.show()
                } else {
                    binding.fabOwnWhDetailAddItemByScan.hide()
                    binding.fabOwnWhDetailAddItemByHand.hide()
                }
            }
            Constants.REMOVE_STRING -> {
                if (!clicked) {
                    binding.fabOwnWhDetailRemoveItemByScan.show()
                    binding.fabOwnWhDetailRemoveItemByHand.show()
                } else {
                    binding.fabOwnWhDetailRemoveItemByScan.hide()
                    binding.fabOwnWhDetailRemoveItemByScan.hide()
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        //infaltes different menus for admin and for common user
        if (args.ownWarehouse) {
            inflater.inflate(R.menu.warehouse_detail_menu_admin, menu)
        } else {
            inflater.inflate(R.menu.warehouse_detail_menu_user, menu)
        }


        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item!!.itemId) {
            R.id.miOwnWarehouseEdit -> {
                val action =
                    WarehouseDetailFragmentDirections.actionWarehouseDetailFragmentToCreateWarehouseFragment(
                        viewModel.warehouseObject.value
                    )
                findNavController().navigate(action)
            }

            R.id.miOwnWarehouseDelete -> deleteOfOwnWarehouse()

            R.id.miOwnWarehouseLeave -> leaveSharedWarehouse()

            R.id.miOwnWarehouseLog -> {
                val action =
                    WarehouseDetailFragmentDirections.actionWarehouseDetailFragmentToWarehouseLogFragment(
                        viewModel.warehouseObject.value!!
                    )
                findNavController().navigate(action)

            }

            R.id.miOwnWarehousePeople -> {
                val action =
                    WarehouseDetailFragmentDirections.actionWarehouseDetailFragmentToPeopleInWarehouseFragment(
                        viewModel.warehouseObject.value!!,
                        args.ownWarehouse
                    )
                findNavController().navigate(action)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Shows dialog to leave shared warehouse
     */
    private fun leaveSharedWarehouse() {
        //zobrazí dialog s výzvou k opuštění cizího skladu
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.leave_warehouse_title))
            .setMessage(R.string.leave_warehouse_description)
            .setNegativeButton(R.string.no) { dialog, which ->
                /* zrušení dialogu*/
            }
            .setPositiveButton(R.string.yes) { dialog, which ->
                //volá opuštění skladu pro aktuálně pihlášeného usera
                viewModel.leaveWarehouse()

                //zobrazí snackar s možností  vrácení akce opuštění skladu
                Snackbar.make(
                    this.requireView(),
                    getString(R.string.you_left_the_warehouse),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.back) {
                        viewModel.undoChangesOfWarehouseDocument()
                    }.show()

                //naviguje na předchozí lokaci
                findNavController().navigateUp()
            }
            .show()
    }

    /**
     * Shows dialog with option to delete own warehouse
     */
    private fun deleteOfOwnWarehouse() {
        //Shows dialog with option to delete of own warehouse
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_warehouse_title)
            .setMessage(R.string.delete_warehouse_description)
            .setNegativeButton(R.string.no) { dialog, which ->
                /* cancel dialog*/
            }
            .setPositiveButton(R.string.yes) { dialog, which ->
                //calls deletion of warehouse in viewmodel class
                viewModel.deleteWarehouse()

                //shows snackbar with option to restore warehouse
                Snackbar.make(
                    this.requireView(),
                    getString(R.string.warehouse_was_deleted),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(getString(R.string.restore)) {
                        //calls restoration of warehouse in viewmodel class
                        viewModel.undoChangesOfWarehouseDocument()
                    }.show()

                //navigates to previous location
                findNavController().navigateUp()
            }
            .show()
    }

    /**
     * Sets up recycle view
     */
    private fun setUpRecycleView() {
        //nastaví recycleview
        val query = db.collection(Constants.WAREHOUSES_STRING)
            .document(args.warehouseID).collection(Constants.ITEMS_STRING)
            .orderBy("name_lowercase")
        val options = FirestoreRecyclerOptions.Builder<WarehouseItem>()
            .setQuery(query, WarehouseItem::class.java)
            .setLifecycleOwner(this).build()

        query.get().addOnCompleteListener {
            if (it.result!!.documents.isEmpty()) {
                binding.noitemAnim.visibility = View.VISIBLE
            } else {
                binding.noitemAnim.visibility = View.GONE
            }
        }

        val ownWarehouseDetailAdapter =
            WarehouseDetailAdapter(options, args.ownWarehouse)

        rv_ownWarehouseDetailList.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = ownWarehouseDetailAdapter
        }
    }


}