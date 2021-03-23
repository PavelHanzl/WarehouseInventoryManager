package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.ownWarehouseDetail

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.OwnWarehousesAdapter
import cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.OwnWarehousesFragmentDirections
import cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.itemDetail.ItemDetailFragment
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse

import cz.pavelhanzl.warehouseinventorymanager.repository.WarehouseItem
import kotlinx.android.synthetic.main.rv_own_warehouses_list_item.view.*
import kotlinx.android.synthetic.main.rv_own_warehousesdetail_list_item.view.*

class OwnWarehouseDetailAdapter(options: FirestoreRecyclerOptions<WarehouseItem>) :
    FirestoreRecyclerAdapter<WarehouseItem, OwnWarehouseDetailAdapter.WarehouseItemViewHolder>(options) {

    class WarehouseItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindVisible(warehouseItem: WarehouseItem) {
            itemView.rv_ownWarehousesDetailListItemName.text = warehouseItem.name
            itemView.rv_ownWarehousesDetailListItemCount.text = warehouseItem.count
            if (warehouseItem.photoURL.isNotEmpty()) {
                Glide.with(itemView).load(warehouseItem.photoURL)
                    .into(itemView.rv_ownWarehousesDetailListItemProfileImage)
            } else {
                Glide.with(itemView).load(R.drawable.ic_avatar_warehouse_item)
                    .into(itemView.rv_ownWarehousesDetailListItemProfileImage)
            }

        }

        fun  bindID(id: String){
            itemView.setOnClickListener{
                var action = OwnWarehouseDetailFragmentDirections.actionOwnWarehouseDetailFragmentToItemDetailFragment(id)
                itemView.findNavController().navigate(action)
                Log.d("test", id)
            }

        }


    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OwnWarehouseDetailAdapter.WarehouseItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.rv_own_warehousesdetail_list_item,
            parent,
            false
        )
        return WarehouseItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: WarehouseItemViewHolder, position: Int, model: WarehouseItem) {

        holder.bindVisible(model)
        var id = snapshots.getSnapshot(position).id
        holder.bindID(id)
    }


}