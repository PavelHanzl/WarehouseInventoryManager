package cz.pavelhanzl.warehouseinventorymanager.warehouse.listOfWarehouses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.navigation.NavigationView
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_list_of_warehouses.*

class ListOfWarehousesFragment : BaseFragment() {
    val args: ListOfWarehousesFragmentArgs by navArgs()
    lateinit var navigationView: NavigationView
    var ownWarehouse: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        //získá odkaz na drawer navigation view, abychom pomocí něho mohli později checknout aktivní položku v menu, podle toho jestli se nacházíme v "Mé sklady" nebo "Ostatní sklady"
        navigationView = requireActivity().drawerNavigationView
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_list_of_warehouses, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //tlačítko na vytvoření nového skladu
        fab_ownWarehouses_addNewOwnWarehouse.setOnClickListener {
            val action =
                ListOfWarehousesFragmentDirections.actionListOfWarehousesFragmentToCreateEditWarehouseFragment()
            Navigation.findNavController(view).navigate(action)
        }

        if (!args.ownWarehouse) {
            fab_ownWarehouses_addNewOwnWarehouse.visibility = INVISIBLE
        }


        checkWarehouseOwnership()


    }

    private fun checkWarehouseOwnership() {
        if (args.ownWarehouse) {//kliknuto na mé sklady
            //nastaví member variable vlastnictví skladu
            ownWarehouse = true

            //nastaví aktivní ikonu v drawer menu na "mé sklady"
            navigationView.menu.getItem(1).isChecked = true

            //nastaví odpovídající title v actionbaru "Ostatní sklady"
            (activity as MainActivity).supportActionBar!!.title = getString(R.string.drawerMenu_ownWarehouses)

        } else {//kliknuto na ostatní sklady
            //nastaví member variable vlastnictví skladu
            ownWarehouse = false

            //nastaví aktivní ikonu v drawer menu na "ostatní sklady"
            navigationView.menu.getItem(2).isChecked = true

            //skryje možnost přidání skladu
            fab_ownWarehouses_addNewOwnWarehouse.visibility = INVISIBLE

            //nastaví odpovídající title v actionbaru "Ostatní sklady"
            (activity as MainActivity).supportActionBar!!.title = getString(R.string.drawerMenu_sharedWarehouses)
        }

        setUpRecycleView()
    }

    private fun setUpRecycleView() {
        val query = //nastaví recycleview query pro recycle view
            if (ownWarehouse) {// vlastní sklady
                db.collection("warehouses").whereEqualTo("owner", auth.currentUser!!.uid).orderBy("name")
            } else {// ostatní sklady
                //TODO: Domyslet načítání skladů cizích
                db.collection("warehouses").whereArrayContains("users",auth.currentUser!!.uid)

                    /* db.collection("warehouses").whereIn(FieldPath.documentId(), listOf(
                    "C8Py9nDTVudEpfU19zCg",
                    "F7eDAwyPZNC4MYvxe2fO",
                    "AkQ6Bhygfbeb8JesnIIJ",
                    "GXo3yE1voxPdvBl2Zemn",
                    "JrEC3hx2BsaF5aecBiVZ",
                    "Mc8aUoi9w0qEnU4bhC4k",
                    "NbeMJV8U5hG1UFMg3nxD",
                    "OCJsVeOPwpa6cEoz710x",
                    "OM2KSPISM5lYHT2ob6DU"))*/

            }

        val options = FirestoreRecyclerOptions.Builder<Warehouse>().setQuery(query, Warehouse::class.java).setLifecycleOwner(this).build()
        val ownWarehousesAdapter = ListOfWarehousesAdapter(options, ownWarehouse)


        rv_ownWarehousesList.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = ownWarehousesAdapter
        }


    }


}