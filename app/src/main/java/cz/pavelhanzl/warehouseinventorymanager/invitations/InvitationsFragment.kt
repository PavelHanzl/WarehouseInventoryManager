package cz.pavelhanzl.warehouseinventorymanager.invitations

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentInvitationsBinding
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.Invitation
import cz.pavelhanzl.warehouseinventorymanager.repository.hideKeyboard
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import kotlinx.android.synthetic.main.activity_main.*

class InvitationsFragment : BaseFragment() {

    private lateinit var binding: FragmentInvitationsBinding
    lateinit var viewModel: InvitationsFragmentViewModel
    lateinit var queryListener: ListenerRegistration
    lateinit var fragmentMode: String
    private val args: InvitationsFragmentArgs by navArgs()
    lateinit var navigationView: NavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //získá odkaz na drawer navigation view, abychom pomocí něho mohli později checknout aktivní položku v menu, podle toho jestli se nacházíme v "Obdržené pozvánky" nebo "Odeslané pozvánky"
        navigationView = requireActivity().drawerNavigationView

        fragmentMode= args.fragmentMode

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //binduje a přiřazuje viewmodel
        binding = FragmentInvitationsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(InvitationsFragmentViewModel::class.java)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        when(fragmentMode){
            Constants.RECIEVED_STRING -> runFragmentRecievedMode()
            Constants.SEND_STRING -> runFragmentSendMode()
        }





        setUpRecycleView()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun setUpRecycleView() {

        var query:Query


        //nastaví recycleview query pro recycle view v závislosti na módu fragmentu
        if(fragmentMode == Constants.RECIEVED_STRING) { //fragment je v modu recieved
            query = db.collection(Constants.INVITATIONS_STRING).whereEqualTo("to", auth.currentUser!!.uid).orderBy("date", Query.Direction.DESCENDING)
        }else{ //fragment je v módu send
            query = db.collection(Constants.INVITATIONS_STRING).whereEqualTo("from", auth.currentUser!!.uid).orderBy("date", Query.Direction.DESCENDING)
        }



        val options = FirestoreRecyclerOptions.Builder<Invitation>().setQuery(query, Invitation::class.java).setLifecycleOwner(this).build()


        val invitationsRecievedAdapter = InvitationsRecievedAdapter(options)
        val invitationsSendAdapter = InvitationsSendAdapter(options)


        queryListener = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Invitation Listener", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot!!.documents.isEmpty()) {
                binding.noinvitationsAnim.visibility = View.VISIBLE
            } else {
                binding.noinvitationsAnim.visibility = View.GONE
            }



        }

        binding.rvInvitationsListInvitationsFragment.apply {
            layoutManager = LinearLayoutManager(activity)
            if(fragmentMode == Constants.RECIEVED_STRING){
                adapter = invitationsRecievedAdapter
            }else{
                adapter = invitationsSendAdapter
            }

        }
    }

    private fun runFragmentRecievedMode() {
        //nastaví odpovídající title v actionbaru "Obdržené pozvánky"
        (activity as MainActivity).supportActionBar!!.title = getString(R.string.drawerMenu_warehouseInvitationsRecieved)

        //nastaví aktivní ikonu v drawer menu na "Obdržené pozvánky"
        navigationView.menu.getItem(3).isChecked = true

    }

    private fun runFragmentSendMode() {
        //nastaví odpovídající title v actionbaru "Odeslané pozvánky"
        (activity as MainActivity).supportActionBar!!.title = getString(R.string.drawerMenu_warehouseInvitationsSend)

        //změní text pod animací v případě prázdného recycleview
        binding.emptyRecycleAnimText.text = getString(R.string.youHaveNotInvitedAnyoneInWh)

        //nastaví aktivní ikonu v drawer menu na "Odeslané pozvánky"
        navigationView.menu.getItem(4).isChecked = true
    }



    override fun onDestroy() {
        queryListener.remove()
        hideKeyboard(requireActivity())
        super.onDestroy()
    }
   
}