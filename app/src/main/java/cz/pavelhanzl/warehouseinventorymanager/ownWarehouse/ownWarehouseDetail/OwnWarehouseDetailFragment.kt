package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.ownWarehouseDetail

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentOwnWarehouseDetailBinding
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import cz.pavelhanzl.warehouseinventorymanager.repository.WarehouseItem
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import kotlinx.android.synthetic.main.fragment_own_warehouse_detail.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.tasks.await

class OwnWarehouseDetailFragment : BaseFragment() {
    private val args: OwnWarehouseDetailFragmentArgs by navArgs()
    private lateinit var binding: FragmentOwnWarehouseDetailBinding
    lateinit var viewModel: OwnWarehousesDetailFragmentViewModel

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
            viewModel =
                ViewModelProvider(this).get(OwnWarehousesDetailFragmentViewModel::class.java)
            viewModel.setData(args.warehouseID)
        }

        Toast.makeText(requireContext(), "Tvůj sklad: " + args.ownWarehouse, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //binduje a přiřazuje viewmodel
        binding = FragmentOwnWarehouseDetailBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        //observer na warehouse object, který když se změní, tak se upraví title pro tento fragment
        viewModel.warehouseObject.observe(viewLifecycleOwner, Observer { profilePhoto ->
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

    private fun setOnClickListenersOnAddRemoveButtons() {
        binding.fabOwnWhDetailAddItem.setOnClickListener {
            onAddOrRemoveItemButtonClicked(addItemClicked, "add")
        }

        binding.fabOwnWhDetailAddItemByHand.setOnClickListener {
            Toast.makeText(requireContext(), "add by hand", Toast.LENGTH_SHORT).show()
        }

        binding.fabOwnWhDetailAddItemByScan.setOnClickListener {
            Toast.makeText(requireContext(), "add by scan", Toast.LENGTH_SHORT).show()
        }

        binding.fabOwnWhDetailRemoveItem.setOnClickListener {
            onAddOrRemoveItemButtonClicked(removeItemClicked, "remove")
        }

        binding.fabOwnWhDetailRemoveItemByHand.setOnClickListener {
            Toast.makeText(requireContext(), "remove by hand", Toast.LENGTH_SHORT).show()
        }

        binding.fabOwnWhDetailRemoveItemByScan.setOnClickListener {
            Toast.makeText(requireContext(), "remove by scan", Toast.LENGTH_SHORT).show()
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
                    binding.fabOwnWhDetailAddItemByScan.visibility = VISIBLE
                    binding.fabOwnWhDetailAddItemByHand.visibility = VISIBLE
                } else {
                    Log.d("Animace", "Add visi - clicked")
                    binding.fabOwnWhDetailAddItemByScan.visibility = INVISIBLE
                    binding.fabOwnWhDetailAddItemByHand.visibility = INVISIBLE
                }
            }
            "remove" -> {
                if (!clicked) {
                    Log.d("Animace", "Remove visi - not clicked")
                    binding.fabOwnWhDetailRemoveItemByScan.visibility = VISIBLE
                    binding.fabOwnWhDetailRemoveItemByHand.visibility = VISIBLE
                } else {
                    Log.d("Animace", "Remove visi - clicked")
                    binding.fabOwnWhDetailRemoveItemByScan.visibility = INVISIBLE
                    binding.fabOwnWhDetailRemoveItemByScan.visibility = INVISIBLE
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {


        // pokud je přihlášený user zároveň majitelem skladu, tak inflatuje menu s rozšířenými možnostmi, pokud není, tak s omezenými možnostmi
        GlobalScope.launch(Dispatchers.IO) {

            //porovnává přihlášeného usera s ownerem
            val whOwner= viewModel.getWarehouseOwner(warehouseId = args.warehouseID).await().toObject(Warehouse::class.java)!!.owner
            if(whOwner  == auth.currentUser!!.uid){
                withContext(Main){
                inflater.inflate(R.menu.own_warehouse_detail_menu_admin, menu)}
            } else{
                withContext(Main){
                inflater.inflate(R.menu.own_warehouse_detail_menu_user, menu)}
            }
        }



        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item!!.itemId) {
            R.id.miOwnWarehouseEdit -> {
                Toast.makeText(context, "Edit", Toast.LENGTH_SHORT).show()
            }

            R.id.miOwnWarehouseDelete -> {
                viewModel.deleteWarehouse()

                Snackbar.make(this.requireView(), getString(R.string.warehouse_was_deleted), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.restore)) {
                        viewModel.deleteWarehouseUndo()
                    }.show()
                findNavController().navigateUp()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpRecycleView() {
        //nastaví recycleview
        val query = db.collection("warehouses").document(args.warehouseID).collection("items")
        val options = FirestoreRecyclerOptions.Builder<WarehouseItem>().setQuery(query, WarehouseItem::class.java).setLifecycleOwner(this).build()
        val ownWarehouseDetailAdapter = OwnWarehouseDetailAdapter(options)

        rv_ownWarehouseDetailList.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = ownWarehouseDetailAdapter
        }
    }

}