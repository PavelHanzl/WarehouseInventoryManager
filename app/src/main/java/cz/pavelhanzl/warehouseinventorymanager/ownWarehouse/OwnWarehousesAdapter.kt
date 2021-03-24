package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import kotlinx.android.synthetic.main.rv_own_warehouses_list_item.view.*

class OwnWarehousesAdapter(options: FirestoreRecyclerOptions<Warehouse>, var ownWarehouse: Boolean) : FirestoreRecyclerAdapter<Warehouse, OwnWarehousesAdapter.WarehouseViewHolder>(options) {

    inner class WarehouseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val holders = itemView

        fun bindVisible(warehouse: Warehouse) {
            itemView.rv_ownWarehousesListWarehouseName.text = warehouse.name
            if (warehouse.photoURL.isNotEmpty()) {
                Glide.with(itemView).load(warehouse.photoURL)
                    .into(itemView.rv_ownWarehousesListWarehouseProfileImage)
            } else {
                Glide.with(itemView).load(R.drawable.avatar_ownwarehouseavatar)
                    .into(itemView.rv_ownWarehousesListWarehouseProfileImage)
            }

        }

        fun  bindID(warehouseID: String){
            itemView.setOnClickListener{
                var action = OwnWarehousesFragmentDirections.actionOwnWarehouseFragmentToOwnWarehouseDetailFragment(warehouseID, ownWarehouse)
                itemView.findNavController().navigate(action)
                Log.d("test",warehouseID)
            }

        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WarehouseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.rv_own_warehouses_list_item,
            parent,
            false
        )
        return WarehouseViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: WarehouseViewHolder,
        position: Int,
        model: Warehouse
    ) {
        holder.bindVisible(model)
        holder.bindID(snapshots.getSnapshot(position).id)
        holder.holders.animate()

    }

    override fun onViewAttachedToWindow(holder: WarehouseViewHolder) {
        super.onViewAttachedToWindow(holder)

        val animation: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scale_up)
        holder.itemView.startAnimation(animation);
    }


}