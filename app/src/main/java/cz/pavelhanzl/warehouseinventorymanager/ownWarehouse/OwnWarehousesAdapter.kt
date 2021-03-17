package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestoreException
import com.squareup.picasso.Picasso
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import kotlinx.android.synthetic.main.rv_own_warehouses_list_item.view.*

class OwnWarehousesAdapter(var ownWarehousesItems: List<Warehouse>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.rv_own_warehouses_list_item,
            parent,
            false
        )
        return WarehouseViewHolder(view)
    }

    class WarehouseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(warehouse: Warehouse) {
            itemView.rv_ownWarehousesListWarehouseName.text = warehouse.name


            if (warehouse.photoURL.isNotEmpty() && warehouse.photoURL != "null") {
                Log.d("tisk", warehouse.photoURL)
                try {
                    Picasso.get().load(warehouse.photoURL)
                        .placeholder(R.drawable.avatar_ownwarehouseavatar)// Place holder image from drawable folder
                        .error(R.drawable.avatar_profileavatar).resize(110, 110).centerCrop()
                        .into(itemView.rv_ownWarehousesListWarehouseProfileImage)
                } catch (e: Exception) {
                    Log.d("tagik", e.message.toString())
                }
            } else {
                Log.d("tagi", "else" )

                Picasso.get().load(R.drawable.avatar_ownwarehouseavatar)
                    .error(R.drawable.avatar_ownwarehouseavatar)
                    .into(itemView.rv_ownWarehousesListWarehouseProfileImage)
            }


        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as WarehouseViewHolder).bind(ownWarehousesItems[position])
    }

    override fun getItemCount(): Int {
        return ownWarehousesItems.size
    }


}