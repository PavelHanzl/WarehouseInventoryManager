package cz.pavelhanzl.warehouseinventorymanager.invitations

import android.os.Bundle
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

/**
 * Invitations fragment
 *
 * @constructor Create empty Invitations fragment
 */
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

        fragmentMode = args.fragmentMode

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //binds and assigns a viewmodel
        binding =
            FragmentInvitationsBinding.inflate(inflater, container, false)
        viewModel =
            ViewModelProvider(this).get(InvitationsFragmentViewModel::class.java)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        //sets fragment to corresponding mode
        when (fragmentMode) {
            Constants.RECIEVED_STRING -> runFragmentReceivedMode()
            Constants.SEND_STRING -> runFragmentSendMode()
        }

        setUpRecycleView()
        // Inflate the layout for this fragment
        return binding.root
    }

    /**
     * Sets up recycle view
     *
     * Sets up recycleview for this fragment correspondingly to chosen
     * frament mode.
     */
    private fun setUpRecycleView() {

        var query: Query

        //sets the recycleview query for the recycle view
        // depending on the fragment mode

        //fragment is in received invitations mode
        if (fragmentMode == Constants.RECIEVED_STRING) {
            //query all received invitations for current user
            query = db.collection(Constants.INVITATIONS_STRING)
                .whereEqualTo("to", auth.currentUser!!.uid)
                .orderBy("date", Query.Direction.DESCENDING)
        }
        //fragment is in send invitations mode
        else {
            //query all send invitations by current user
            query = db.collection(Constants.INVITATIONS_STRING)
                .whereEqualTo("from", auth.currentUser!!.uid)
                .orderBy("date", Query.Direction.DESCENDING)
        }

        //sets up options for Firestore Recycler View Adapter,
        //takes previously made query as a parameter
        val options = FirestoreRecyclerOptions.Builder<Invitation>()
            .setQuery(query, Invitation::class.java).setLifecycleOwner(this)
            .build()

        //makes instances of recycleview adapters
        // with options from previous step
        val invitationsRecievedAdapter = InvitationsRecievedAdapter(options)
        val invitationsSendAdapter = InvitationsSendAdapter(options)

        //Display empty animation if there is no data in query
        showEmptyAnimationIfQueryIsEmpty(query)

        //Finally set up recycle view
        binding.rvInvitationsListInvitationsFragment.apply {
            //set up linear layout for chosen recycle view
            layoutManager = LinearLayoutManager(activity)

            //assigns different recycle view adapter for received mode
            //and different for send mode to chosen recycle view
            adapter = if (fragmentMode == Constants.RECIEVED_STRING) {
                invitationsRecievedAdapter
            } else {
                invitationsSendAdapter
            }
        }
    }

    /**
     * Shows empty animation, if there is no invitation in the current list
     */
    private fun showEmptyAnimationIfQueryIsEmpty(query: Query) {
        //Listen for real time changes in query, so empty animation can be
        //shown dynamically
        queryListener = query.addSnapshotListener { snapshot, e ->
            if (e != null) { // if error occurred, stops to listen
                return@addSnapshotListener
            }

            //if query is empty, show empty animation
            if (snapshot!!.documents.isEmpty()) {
                binding.noinvitationsAnim.visibility = View.VISIBLE
            } else { // hide empty animation
                binding.noinvitationsAnim.visibility = View.GONE
            }
        }
    }

    /**
     * Runs fragment in received mode
     */
    private fun runFragmentReceivedMode() {
        //set the corresponding name in the "Invitations received" actionbar
        (activity as MainActivity).supportActionBar!!.title =
            getString(R.string.drawerMenu_warehouseInvitationsRecieved)

        //set the active icon in the drawer menu to "Invitations received"
        navigationView.menu.getItem(3).isChecked = true

    }

    /**
     * Runs fragment in send mode
     */
    private fun runFragmentSendMode() {
        //sets the corresponding title in the "Sent invitations" actionbar
        (activity as MainActivity).supportActionBar!!.title =
            getString(R.string.drawerMenu_warehouseInvitationsSend)

        // correspondingly changes the text below the animation
        // in the case of an empty recycleview
        binding.emptyRecycleAnimText.text =
            getString(R.string.youHaveNotInvitedAnyoneInWh)

        //sets the active icon in the drawer menu to "Sent invitations"
        navigationView.menu.getItem(4).isChecked = true
    }

    override fun onDestroy() {
        queryListener.remove()
        hideKeyboard(requireActivity())
        super.onDestroy()
    }
}