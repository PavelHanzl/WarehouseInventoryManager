package cz.pavelhanzl.warehouseinventorymanager.warehouse.peopleInWarehouse

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

import kotlinx.android.synthetic.main.rv_warehouse_people_list_item.view.*

class PeopleInWarehouseAdapter3(options: FirestoreRecyclerOptions<User>, var warehouse: Warehouse) :
    FirestoreRecyclerAdapter<User, PeopleInWarehouseAdapter3.UserViewHolder>(options) {
    val db = Firebase.firestore
    val auth = Firebase.auth

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindVisible(user: User) {
            itemView.rv_userName_peopleInWarehouseFragment.text = user.name

            Glide.with(itemView)
                .load(user.photoURL)
                .placeholder(R.drawable.avatar_profileavatar)
                .error(R.drawable.avatar_profileavatar)
                .into(itemView.rv_userProfilePhoto_peopleInWarehouseFragment)


            if (warehouse.owner == auth.currentUser!!.uid) {
                //itemView.visibility = View.VISIBLE
                itemView.btn_removeUser_peopleInWarehouseFragment.visibility = View.VISIBLE

                itemView.btn_removeUser_peopleInWarehouseFragment.setOnClickListener {
                    //zapíše současného usera do pole users v daném skladě
                    val warehouse = db.collection(Constants.WAREHOUSES_STRING).document(warehouse.warehouseID)
                    warehouse.update("users", FieldValue.arrayRemove(auth.currentUser!!.uid))
                    itemView.visibility = View.GONE

                }

            } else {
                itemView.btn_removeUser_peopleInWarehouseFragment.visibility = View.GONE
                //itemView.visibility = View.VISIBLE
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleInWarehouseAdapter3.UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.rv_warehouse_people_list_item,
            parent,
            false
        )
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {

        holder.bindVisible(model)

    }

    override fun onViewAttachedToWindow(holder: UserViewHolder) {
        super.onViewAttachedToWindow(holder)

        val animation: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scale_up)
        holder.itemView.startAnimation(animation);
    }

}