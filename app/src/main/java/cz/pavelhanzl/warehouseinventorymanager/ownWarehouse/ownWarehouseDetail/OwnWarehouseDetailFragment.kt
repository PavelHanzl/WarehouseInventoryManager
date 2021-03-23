package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.ownWarehouseDetail

import android.os.Bundle
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentOwnWarehouseDetailBinding
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import kotlinx.android.synthetic.main.menu_header.*

class OwnWarehouseDetailFragment : BaseFragment() {
    private lateinit var binding: FragmentOwnWarehouseDetailBinding
    lateinit var viewModel: OwnWarehousesDetailFragmentViewModel
    private val fabAnimFromBottom: Animation by lazy{AnimationUtils.loadAnimation(requireContext(), R.anim.fab_from_bottom)}
    private val fabAnimToBottom: Animation by lazy{AnimationUtils.loadAnimation(requireContext(), R.anim.fab_to_bottom)}

    private var addItemClicked = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)


        if (savedInstanceState == null) {
            viewModel =
                ViewModelProvider(this).get(OwnWarehousesDetailFragmentViewModel::class.java)
            val args: OwnWarehouseDetailFragmentArgs by navArgs()
            viewModel.setData(args.warehouseID)
        }

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

        binding.fabOwnWhDetailAddItem.setOnClickListener {
            onAddItemButtonClicked()
        }

        binding.fabOwnWhDetailAddItemByHand.setOnClickListener {
            Toast.makeText(requireContext(),"add by hand",Toast.LENGTH_SHORT).show()
        }

        binding.fabOwnWhDetailAddItemByScan.setOnClickListener {
            Toast.makeText(requireContext(),"add by scan",Toast.LENGTH_SHORT).show()
        }


        return binding.root
    }

    private fun onAddItemButtonClicked() {
        setVisibility(addItemClicked)
        setAnimation(addItemClicked)
        addItemClicked = !addItemClicked
    }

    private fun setAnimation(clicked: Boolean) {
        if(!clicked){
            binding.fabOwnWhDetailAddItemByScan.startAnimation(fabAnimFromBottom)
            binding.fabOwnWhDetailAddItemByHand.startAnimation(fabAnimFromBottom)
        } else{
            binding.fabOwnWhDetailAddItemByScan.startAnimation(fabAnimToBottom)
            binding.fabOwnWhDetailAddItemByHand.startAnimation(fabAnimToBottom)
        }
    }

    private fun setVisibility(clicked: Boolean) {
        if(!clicked){
            binding.fabOwnWhDetailAddItemByScan.visibility = VISIBLE
            binding.fabOwnWhDetailAddItemByHand.visibility = VISIBLE
        } else{
            binding.fabOwnWhDetailAddItemByScan.visibility = INVISIBLE
            binding.fabOwnWhDetailAddItemByHand.visibility = INVISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.own_warehouse_detail_menu, menu);

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item!!.itemId){
            R.id.miOwnWarehouseEdit -> {
                Toast.makeText(context,"Edit",Toast.LENGTH_SHORT).show()}

            R.id.miOwnWarehouseDelete -> {Toast.makeText(context,"Delete",Toast.LENGTH_SHORT).show()}
        }
        return super.onOptionsItemSelected(item)
    }


}