package cz.pavelhanzl.warehouseinventorymanager.warehouse.peopleInWarehouse

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.User
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import kotlinx.android.synthetic.main.rv_warehouse_people_list_item.view.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PeopleInWarehouseAdapter (var users: List<String>, var warehouse: Warehouse): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val db = Firebase.firestore
        val auth = Firebase.auth

        inner class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

            fun bindVisible(user: User) {
                itemView.rv_userName_peopleInWarehouseFragment.text = user.name

                Glide.with(itemView)
                    .load(user.photoURL)
                    .placeholder(R.drawable.avatar_profileavatar)
                    .error(R.drawable.avatar_profileavatar)
                    .into(itemView.rv_userProfilePhoto_peopleInWarehouseFragment)


                if (warehouse.owner == auth.currentUser!!.uid) {
                    itemView.visibility = View.VISIBLE
                    itemView.btn_removeUser_peopleInWarehouseFragment.visibility = View.VISIBLE

                    itemView.btn_removeUser_peopleInWarehouseFragment.setOnClickListener {
                        //zapíše současného usera do pole users v daném skladě
                        val warehouse = db.collection(Constants.WAREHOUSES_STRING).document(warehouse.warehouseID)
                        warehouse.update("users", FieldValue.arrayRemove(user.userID))
                        itemView.visibility = View.GONE

                    }

                } else {
                    itemView.btn_removeUser_peopleInWarehouseFragment.visibility = View.GONE
                    itemView.visibility = View.VISIBLE
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_warehouse_people_list_item, parent, false)
            return UserViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder,
            position: Int) {

            val idOfUser = users[position]

            GlobalScope.launch(IO) {
                db.collection(Constants.USERS_STRING).document(idOfUser).addSnapshotListener { snapshot, error ->
                    if(error != null) {
                        Log.w("Users", error.message.toString())
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val user = snapshot.toObject(User::class.java)

                        (holder as UserViewHolder).bindVisible(user!!)
                    } else {
                        Log.w("Users", "Current data null")
                    }
                }
            }

        }

        override fun getItemCount(): Int {
            return users.size
        }
    }

