package cz.pavelhanzl.warehouseinventorymanager.warehouse.itemDetail

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentItemDetailBinding
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment

class ItemDetailFragment : BaseFragment() {
    private val args: ItemDetailFragmentArgs by navArgs()
    private lateinit var binding: FragmentItemDetailBinding
    lateinit var viewModel: ItemDetailFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        //předá argumenty do viewmodelu
        if (savedInstanceState == null) {
            viewModel = ViewModelProvider(this).get(ItemDetailFragmentViewModel::class.java)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //binduje a přiřazuje viewmodel
        binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        Log.d("editeditemos", "trigthis")
        viewModel.setdata(args.selectedWarehouseItemObject)







        return binding.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        if (args.ownWarehouse) {
            inflater.inflate(R.menu.item_detail_menu_admin, menu)
        } else {
            inflater.inflate(R.menu.item_detail_menu_user, menu)
        }


        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item!!.itemId) {
            R.id.mi_WhItemEdit -> {
               Log.d("editeditemos", viewModel.selectedWarehouseItem.value!!.code)
                var action = ItemDetailFragmentDirections.actionItemDetailFragmentToCreateEditItemFragment(viewModel.selectedWarehouseItem.value!!.warehouseID, viewModel.selectedWarehouseItem.value!!)
                findNavController().navigate(action)
            }

            R.id.mi_WhItemDelete -> deleteWarehouseItem()

            R.id.mi_WhItemLog -> true //leaveSharedWarehouse()

        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteWarehouseItem() {
        //zobrazí dialog s výzvou k potvrzení ke smazání skladu
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Odstranit pložku?")
            .setMessage("Odstraněním položky k ní ztratíte přístup nejen vy, ale i všichni ostatní lidé v tomto skladu. \n \nOpravdu si přejete položku odstranit?")
            .setNegativeButton(R.string.no) { dialog, which ->
                /* zrušení dialogu*/
            }
            .setPositiveButton(R.string.yes) { dialog, which ->
                //volá smazání skladové položky
                viewModel.deleteWarehouseItem()

                //zobrazí snackar s možností  vrácení akce smazání skladové položky
                Snackbar.make(this.requireView(), "Položka byla odstraněna!", Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.restore)) {
                        viewModel.undoChangesOfWarehouseItemDocument()
                    }.show()

                //naviguje na předchozí lokaci
                findNavController().navigateUp()
            }
            .show()
    }

}