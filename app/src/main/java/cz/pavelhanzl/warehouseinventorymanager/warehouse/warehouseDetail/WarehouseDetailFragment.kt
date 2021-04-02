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
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentWarehouseDetailBinding
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.WarehouseItem
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import kotlinx.android.synthetic.main.fragment_warehouse_detail.*

class WarehouseDetailFragment : BaseFragment() {


    private val args: WarehouseDetailFragmentArgs by navArgs()
    private lateinit var binding: FragmentWarehouseDetailBinding
    private val viewModel: WarehousesDetailFragmentViewModel by activityViewModels()

    private val fabAddItemAnimFromBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.fab_from_bottom) }
    private val fabAddItemAnimToBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.fab_to_bottom) }

    private val fabRemoveItemAnimFromBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.fab_from_bottom) }
    private val fabRemoveItemAnimToBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.fab_to_bottom) }

    private var addItemClicked = false
    private var removeItemClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        //předá argumenty do viewmodelu
        if (savedInstanceState == null) {
            viewModel.setData(args.warehouseID)
        }

        Toast.makeText(requireContext(), "Tvůj sklad: " + args.ownWarehouse, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //definuje počáteční stav fab tlačítek pro přidání/odebírání
        addItemClicked = false
        removeItemClicked = false


        //binduje a přiřazuje viewmodel
        binding = FragmentWarehouseDetailBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        //observer na warehouse object, který když se změní, tak se upraví title pro tento fragment
        viewModel.warehouseObject.observe(viewLifecycleOwner, Observer { profilePhoto ->
            (activity as MainActivity).supportActionBar!!.title = viewModel.warehouseObject.value!!.name
        })



        setOnClickListenersOnAddRemoveButtons()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecycleView()
    }

    private fun setOnClickListenersOnAddRemoveButtons() {
        binding.fabOwnWhDetailAddItem.setOnClickListener {
            onAddOrRemoveItemButtonClicked(addItemClicked, "add")
        }

        binding.fabOwnWhDetailAddItemByHand.setOnClickListener {
            viewModel.addRemoveFragmentMode = Constants.ADDING_STRING
            Toast.makeText(requireContext(), "add by hand", Toast.LENGTH_SHORT).show()
            val action =  WarehouseDetailFragmentDirections.actionWarehouseDetailFragmentToAddRemoveItem()
            findNavController().navigate(action)
        }

        binding.fabOwnWhDetailAddItemByScan.setOnClickListener {
            viewModel.addRemoveFragmentMode = Constants.ADDING_STRING
            Toast.makeText(requireContext(), "add by scan", Toast.LENGTH_SHORT).show()
            val action =  WarehouseDetailFragmentDirections.actionWarehouseDetailFragmentToScannerFragment("adding")
            findNavController().navigate(action)
        }

        binding.fabOwnWhDetailRemoveItem.setOnClickListener {

            onAddOrRemoveItemButtonClicked(removeItemClicked, "remove")
        }

        binding.fabOwnWhDetailRemoveItemByHand.setOnClickListener {
            viewModel.addRemoveFragmentMode = Constants.REMOVING_STRING
            Toast.makeText(requireContext(), "remove by hand", Toast.LENGTH_SHORT).show()
            val action =  WarehouseDetailFragmentDirections.actionWarehouseDetailFragmentToAddRemoveItem()
            findNavController().navigate(action)
        }

        binding.fabOwnWhDetailRemoveItemByScan.setOnClickListener {
            viewModel.addRemoveFragmentMode = Constants.REMOVING_STRING
            Toast.makeText(requireContext(), "remove by scan", Toast.LENGTH_SHORT).show()
            val action =  WarehouseDetailFragmentDirections.actionWarehouseDetailFragmentToScannerFragment("removing")
            findNavController().navigate(action)
        }
    }

    private fun onAddOrRemoveItemButtonClicked(clicked: Boolean, operation: String) {

        when (operation) {
            "add" -> addItemClicked = !addItemClicked
            "remove" -> removeItemClicked = !removeItemClicked
        }

        setVisibility(clicked, operation)
        setAnimation(clicked, operation)
    }

    private fun setAnimation(clicked: Boolean, operation: String) {
        when (operation) {
            "add" -> {
                if (!clicked) {
                    Log.d("Animace", "Add anim - not clicked")
                    binding.fabOwnWhDetailAddItemByScan.startAnimation(fabAddItemAnimFromBottom)
                    binding.fabOwnWhDetailAddItemByHand.startAnimation(fabAddItemAnimFromBottom)
                } else {
                    Log.d("Animace", "Add anim - clicked")
                    binding.fabOwnWhDetailAddItemByScan.startAnimation(fabAddItemAnimToBottom)
                    binding.fabOwnWhDetailAddItemByHand.startAnimation(fabAddItemAnimToBottom)
                }
            }
            "remove" -> {
                if (!clicked) {
                    Log.d("Animace", "Remove anim - not clicked")
                    binding.fabOwnWhDetailRemoveItemByScan.startAnimation(fabRemoveItemAnimFromBottom)
                    binding.fabOwnWhDetailRemoveItemByHand.startAnimation(fabRemoveItemAnimFromBottom)
                } else {
                    Log.d("Animace", "Remove anim - clicked")
                    binding.fabOwnWhDetailRemoveItemByScan.startAnimation(fabRemoveItemAnimToBottom)
                    binding.fabOwnWhDetailRemoveItemByHand.startAnimation(fabRemoveItemAnimToBottom)
                }
            }
        }

    }

    private fun setVisibility(clicked: Boolean, operation: String) {
        when (operation) {
            "add" -> {
                if (!clicked) {
                    Log.d("Animace", "Add visi - not clicked")
                    binding.fabOwnWhDetailAddItemByScan.show()
                    binding.fabOwnWhDetailAddItemByHand.show()
                } else {
                    Log.d("Animace", "Add visi - clicked")
                    binding.fabOwnWhDetailAddItemByScan.hide()
                    binding.fabOwnWhDetailAddItemByHand.hide()
                }
            }
            "remove" -> {
                if (!clicked) {
                    Log.d("Animace", "Remove visi - not clicked")
                    binding.fabOwnWhDetailRemoveItemByScan.show()
                    binding.fabOwnWhDetailRemoveItemByHand.show()
                } else {
                    Log.d("Animace", "Remove visi - clicked")
                    binding.fabOwnWhDetailRemoveItemByScan.hide()
                    binding.fabOwnWhDetailRemoveItemByScan.hide()
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        if (args.ownWarehouse) {
            inflater.inflate(R.menu.own_warehouse_detail_menu_admin, menu)
        } else {
            inflater.inflate(R.menu.own_warehouse_detail_menu_user, menu)
        }


        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item!!.itemId) {
            R.id.miOwnWarehouseEdit -> {
                Toast.makeText(context, "Edit", Toast.LENGTH_SHORT).show()
                var action =  WarehouseDetailFragmentDirections.actionWarehouseDetailFragmentToCreateWarehouseFragment(viewModel.warehouseObject.value)
                findNavController().navigate(action)
            }

            R.id.miOwnWarehouseDelete -> deleteOfOwnWarehouse()

            R.id.miOwnWarehouseLeave -> leaveSharedWarehouse()

        }
        return super.onOptionsItemSelected(item)
    }

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
                Snackbar.make(this.requireView(), getString(R.string.you_left_the_warehouse), Snackbar.LENGTH_LONG)
                    .setAction(R.string.back) {
                        viewModel.undoChangesOfWarehouseDocument()
                    }.show()

                //naviguje na předchozí lokaci
                findNavController().navigateUp()
            }
            .show()
    }

    private fun deleteOfOwnWarehouse() {
        //zobrazí dialog s výzvou k potvrzení ke smazání skladu
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_warehouse_title)
            .setMessage(R.string.delete_warehouse_description)
            .setNegativeButton(R.string.no) { dialog, which ->
                /* zrušení dialogu*/
            }
            .setPositiveButton(R.string.yes) { dialog, which ->
                //volá smazání skladu
                viewModel.deleteWarehouse()

                //zobrazí snackar s možností  vrácení akce smazání skladu
                Snackbar.make(this.requireView(), getString(R.string.warehouse_was_deleted), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.restore)) {
                        viewModel.undoChangesOfWarehouseDocument()
                    }.show()

                //naviguje na předchozí lokaci
                findNavController().navigateUp()
            }
            .show()
    }

    private fun setUpRecycleView() {
        //nastaví recycleview
        val query = db.collection("warehouses").document(args.warehouseID).collection("items")
        val options = FirestoreRecyclerOptions.Builder<WarehouseItem>().setQuery(query, WarehouseItem::class.java).setLifecycleOwner(this).build()
        val ownWarehouseDetailAdapter = WarehouseDetailAdapter(options)

        rv_ownWarehouseDetailList.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = ownWarehouseDetailAdapter
        }
    }

}