package cz.pavelhanzl.warehouseinventorymanager.invitations

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.*
import kotlinx.android.synthetic.main.rv_warehouse_invitation_list_item.view.*

/**
 * Invitations recieved adapter
 *
 * @constructor
 *
 * @param options
 */
class InvitationsRecievedAdapter(options: FirestoreRecyclerOptions<Invitation>) : FirestoreRecyclerAdapter<Invitation, InvitationsRecievedAdapter.InvitationViewHolder>(options) {
    val db = Firebase.firestore
    val auth = Firebase.auth

    /**
     * Invitation view holder
     *
     * @constructor
     *
     * @param itemView
     */
    inner class InvitationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /**
         * Bind user
         * Binds info about user who send invitation.
         *
         * @param fromUser user who send invitation
         */
        fun bindUser(fromUser:User) {
            itemView.rv_userName_warehouseInvitationFragment.text = fromUser.name

            Glide.with(itemView.context)
                .load(fromUser.photoURL)
                .placeholder(R.drawable.avatar_profileavatar)
                .error(R.drawable.avatar_profileavatar)
                .into(itemView.userProfileImage_warehouseInvitationFragment)


            itemView.userProfileImage_warehouseInvitationFragment
        }

        /**
         * Bind warehouse
         * Binds info about warehouse joined with this invitation.
         * @param warehouse warehouse to be joined
         */
        fun bindWarehouse(warehouse: Warehouse) {
            itemView.rv_warehouseName_warehouseInvitationFragment.text = warehouse.name
        }

        /**
         * Bind date
         * Binds date when was invitation send.
         *
         * @param date date when was invitation send
         */
        fun bindDate(date:String){
            itemView.rv_date_warehouseInvitationFragment.text = date
        }

        /**
         * Bind btns
         * Binds actions to buttons of invitation. Actions are join warehouse and decline invitation
         *
         * @param warehouseId warehouse joined with invitation
         * @param invitationId id of this invitation
         */
        fun bindBtns(warehouseId:String, invitationId:String ){

            itemView.btn_acceptInvitation_warehouseInvitationFragment.setOnClickListener{

                //zapíše současného usera do pole users v daném skladě
                val warehouse = db.collection(Constants.WAREHOUSES_STRING).document(warehouseId)
                warehouse.update(Constants.USERS_STRING, FieldValue.arrayUnion(auth.currentUser!!.uid))

                //smaže tuto pozvánku
                db.collection(Constants.INVITATIONS_STRING).document(invitationId).delete()
            }

            itemView.btn_declineInvitation_warehouseInvitationFragment.setOnClickListener{
                //smaže tuto pozvánku
                db.collection(Constants.INVITATIONS_STRING).document(invitationId).delete()
            }


        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.rv_warehouse_invitation_list_item,
            parent,
            false
        )
        return InvitationViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: InvitationViewHolder,
        position: Int,
        model: Invitation
    ) {

        val fromRef = db.collection(Constants.USERS_STRING).document(model.from)
        val warehouseRef = db.collection(Constants.WAREHOUSES_STRING).document(model.warehouseId)
        val invitationRef = db.collection(Constants.INVITATIONS_STRING).document(model.invitationId)

        //binduje od koho přišla pozvánka
        fromRef.get().addOnSuccessListener { document ->
            if (document != null) {
                holder.bindUser(document.toObject(User::class.java)!!)
            } else {
            Log.d("InviRecievedAdapter", "No such document")
        }
        }.addOnFailureListener { exception ->
            Log.d("InviRecievedAdapter", "get failed with ", exception)
        }

        //binduje jaký sklad
        warehouseRef.get().addOnSuccessListener { document ->
            if (document != null && document.data != null) {
                Log.d("InviRecievedAdapter", "DocumentSnapshot data: ${document.data}")
                holder.bindWarehouse(document.toObject(Warehouse::class.java)!!)
            } else {
                Log.d("InviRecievedAdapter", "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d("InviRecievedAdapter", "get failed with ", exception)
        }

        //binduje datum
        holder.bindDate(model.date)

        //binduje tlačítka
        holder.bindBtns(model.warehouseId, model.invitationId)


    }

    override fun onViewAttachedToWindow(holder: InvitationViewHolder) {
        super.onViewAttachedToWindow(holder)

        //přidá animaci na položky
        val animation: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scale_up)
        holder.itemView.startAnimation(animation);

    }


}