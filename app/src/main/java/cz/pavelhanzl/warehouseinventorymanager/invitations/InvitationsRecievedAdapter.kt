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

class InvitationsRecievedAdapter(options: FirestoreRecyclerOptions<Invitation>) : FirestoreRecyclerAdapter<Invitation, InvitationsRecievedAdapter.InvitationViewHolder>(options) {
    val db = Firebase.firestore
    val auth = Firebase.auth


    inner class InvitationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        fun bindUser(fromUser:User) {
            itemView.rv_userName_warehouseInvitationFragment.text = fromUser.name

            Glide.with(itemView.context)
                .load(fromUser.photoURL)
                .placeholder(R.drawable.avatar_profileavatar)
                .error(R.drawable.avatar_profileavatar)
                .into(itemView.userProfileImage_warehouseInvitationFragment)


            itemView.userProfileImage_warehouseInvitationFragment
        }

        fun bindWarehouse(warehouse: Warehouse) {
            itemView.rv_warehouseName_warehouseInvitationFragment.text = warehouse.name
        }

        fun bindDate(date:String){
            itemView.rv_date_warehouseInvitationFragment.text = date
        }

        fun bindBtns(warehouseId:String, invitationId:String ){

            itemView.btn_acceptInvitation_warehouseInvitationFragment.setOnClickListener{

                Log.d("invi", "accept warehouse:" + warehouseId + " ivni:" + invitationId )
                //zapíše současného usera do pole users v daném skladě
                val warehouse = db.collection(Constants.WAREHOUSES_STRING).document(warehouseId)
                warehouse.update("users", FieldValue.arrayUnion(auth.currentUser!!.uid))

                //smaže tuto pozvánku
                db.collection(Constants.INVITATIONS_STRING).document(invitationId).delete()
            }

            itemView.btn_declineInvitation_warehouseInvitationFragment.setOnClickListener{
                Log.d("invi", "decline warehouse:" + warehouseId + " ivni:" + invitationId )

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
                Log.d("Invi", "DocumentSnapshot data: ${document.data}")
                holder.bindUser(document.toObject(User::class.java)!!)

            } else {
                Log.d("Invi", "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d("Invi", "get failed with ", exception)
        }

        //binduje jaký sklad
        warehouseRef.get().addOnSuccessListener { document ->
            if (document != null && document.data != null) {
                Log.d("Invi", "DocumentSnapshot data: ${document.data}")
                holder.bindWarehouse(document.toObject(Warehouse::class.java)!!)
            } else {
                Log.d("Invi", "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d("Invi", "get failed with ", exception)
        }

        //binduje datum
        holder.bindDate(model.date)

        //binduje tlačítka
        holder.bindBtns(model.warehouseId, model.invitationId)


    }

    override fun onViewAttachedToWindow(holder: InvitationViewHolder) {
        super.onViewAttachedToWindow(holder)

        val animation: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scale_up)
        holder.itemView.startAnimation(animation);


    }


}