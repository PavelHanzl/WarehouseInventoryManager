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
import kotlinx.android.synthetic.main.rv_warehouse_item_detail_list_item.view.*

/**
 * Warehouse detail adapter
 *
 * @property ownWarehouse
 * @constructor
 *
 * @param options
 */
class WarehouseDetailAdapter(options: FirestoreRecyclerOptions<WarehouseItem>, var ownWarehouse: Boolean) :
    FirestoreRecyclerAdapter<WarehouseItem, WarehouseDetailAdapter.WarehouseItemViewHolder>(options) {

    inner class WarehouseItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /**
         * Binds visible information about warehouse item
         * Binds name, count and item profile image
         *
         * @param warehouseItem
         */
        fun bindVisible(warehouseItem: WarehouseItem) {
            itemView.rv_ownWarehousesDetailListItemName.text = warehouseItem.name
            itemView.rv_ownWarehousesDetailListItemCount.text = warehouseItem.count.toString()

            Glide.with(itemView)
                .load(warehouseItem.photoURL)
                .placeholder(R.drawable.avatar_warehouse_item_primary_color)
                .error(R.drawable.avatar_warehouse_item_primary_color)
                .into(itemView.rv_ownWarehousesDetailListItemProfileImage)
        }

        /**
         * Binds id for on click listener to warehouse item recycle item view
         *
         * @param warehouseItem
         */
        fun bindID(warehouseItem: WarehouseItem) {
            itemView.setOnClickListener {
                val action = WarehouseDetailFragmentDirections.actionWarehouseDetailFragmentToItemDetailFragment(warehouseItem, ownWarehouse)
                itemView.findNavController().navigate(action)
            }

        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WarehouseDetailAdapter.WarehouseItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.rv_warehouse_item_detail_list_item,
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

        //sets animation
        val animation: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scale_up)
        holder.itemView.startAnimation(animation);
    }

}