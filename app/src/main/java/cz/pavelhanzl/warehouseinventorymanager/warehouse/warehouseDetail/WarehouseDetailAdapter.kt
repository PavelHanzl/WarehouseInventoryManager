package cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseDetail

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import cz.pavelhanzl.warehouseinventorymanager.R

import cz.pavelhanzl.warehouseinventorymanager.repository.WarehouseItem
import kotlinx.android.synthetic.main.rv_own_warehousesdetail_list_item.view.*

class WarehouseDetailAdapter(options: FirestoreRecyclerOptions<WarehouseItem>) :
    FirestoreRecyclerAdapter<WarehouseItem, WarehouseDetailAdapter.WarehouseItemViewHolder>(options) {

    class WarehouseItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindVisible(warehouseItem: WarehouseItem) {
            itemView.rv_ownWarehousesDetailListItemName.text = warehouseItem.name
            itemView.rv_ownWarehousesDetailListItemCount.text = warehouseItem.count.toString()

            Glide.with(itemView)
                .load(warehouseItem.photoURL)
                .placeholder(R.drawable.avatar_warehouse_item_primary_color)
                .error(R.drawable.avatar_warehouse_item_primary_color)
                .into(itemView.rv_ownWarehousesDetailListItemProfileImage)
        }

        fun bindID(warehouseItem: WarehouseItem) {
            itemView.setOnClickListener {
                var action = WarehouseDetailFragmentDirections.actionWarehouseDetailFragmentToItemDetailFragment(warehouseItem)
                itemView.findNavController().navigate(action)
                Log.d("test", warehouseItem.name)
            }

        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WarehouseDetailAdapter.WarehouseItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.rv_own_warehousesdetail_list_item,
            parent,
            false
        )
        return WarehouseItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: WarehouseItemViewHolder, position: Int, model: WarehouseItem) {

        holder.bindVisible(model)
        var warehouseItem = snapshots.getSnapshot(position).toObject(WarehouseItem::class.java)
        holder.bindID(warehouseItem!!)
    }

    override fun onViewAttachedToWindow(holder: WarehouseItemViewHolder) {
        super.onViewAttachedToWindow(holder)

        val animation: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scale_up)
        holder.itemView.startAnimation(animation);
    }

}