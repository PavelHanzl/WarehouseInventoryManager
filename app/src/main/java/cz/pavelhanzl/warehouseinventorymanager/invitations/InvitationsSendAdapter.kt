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
import cz.pavelhanzl.warehouseinventorymanager.stringResource
import kotlinx.android.synthetic.main.rv_warehouse_invitation_list_item.view.*

class InvitationsSendAdapter(options: FirestoreRecyclerOptions<Invitation>) : FirestoreRecyclerAdapter<Invitation, InvitationsSendAdapter.InvitationViewHolder>(options) {
    val db = Firebase.firestore
    val auth = Firebase.auth


    inner class InvitationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /**
         * Bind user
         * Binds info about user to who the invitation was send.
         * @param toUser user who we send invitation to warehouse
         */
        fun bindUser(toUser:User) {
            itemView.rv_userName_warehouseInvitationFragment.text = toUser.name

            Glide.with(itemView.context)
                .load(toUser.photoURL)
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
         * Binds actions to buttons of invitation. Actions is decline invitation. Hides second unnecessary button.
         *
         * @param warehouseId warehouse joined with invitation
         * @param invitationId id of this invitation
         */
        fun bindBtns(warehouseId:String, invitationId:String ){

            //změní text akčního tlačítka
            itemView.btn_acceptInvitation_warehouseInvitationFragment.text = stringResource(R.string.cancleInvitation)

            itemView.btn_acceptInvitation_warehouseInvitationFragment.setOnClickListener{
                //smaže tuto pozvánku
                db.collection(Constants.INVITATIONS_STRING).document(invitationId).delete()
            }

            // skryje nepotřebné tlaččítko v layoutu (používané na v doručených pozvánkách)
            itemView.btn_declineInvitation_warehouseInvitationFragment.visibility = View.GONE

        }

        /**
         * Bind label
         *
         * @param label binds different label for invitation card
         */
        fun bindLabel(label: String){
            itemView.rv_invitationDescription_warehouseInvitationFragment.text = label
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

        val toRef = db.collection(Constants.USERS_STRING).document(model.to)
        val warehouseRef = db.collection(Constants.WAREHOUSES_STRING).document(model.warehouseId)
        val invitationRef = db.collection(Constants.INVITATIONS_STRING).document(model.invitationId)

        //binduje od komu byla odeslána pozvánka
        toRef.get().addOnSuccessListener { document ->
            if (document != null) {
                Log.d("InviSendAdapter", "DocumentSnapshot data: ${document.data}")
                holder.bindUser(document.toObject(User::class.java)!!)

            } else {
                Log.d("InviSendAdapter", "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d("InviSendAdapter", "get failed with ", exception)
        }

        //binduje jaký sklad
        warehouseRef.get().addOnSuccessListener { document ->
            if (document != null && document.data != null) {
                Log.d("InviSendAdapter", "DocumentSnapshot data: ${document.data}")
                holder.bindWarehouse(document.toObject(Warehouse::class.java)!!)
            } else {
                Log.d("InviSendAdapter", "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d("InviSendAdapter", "get failed with ", exception)
        }

        //binduje datum
        holder.bindDate(model.date)

        //binduje tlačítka
        holder.bindBtns(model.warehouseId, model.invitationId)

        //binduje lable
        holder.bindLabel(stringResource(R.string.userWasInvitedToWh))


    }

    override fun onViewAttachedToWindow(holder: InvitationViewHolder) {
        super.onViewAttachedToWindow(holder)
        //přidá animaci na položky
        val animation: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scale_up)
        holder.itemView.startAnimation(animation);


    }


}