package cz.pavelhanzl.warehouseinventorymanager.warehouse.listOfWarehouses

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
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import kotlinx.android.synthetic.main.rv_list_of_warehouses_list_item.view.*

/**
 * List of warehouses adapter
 *
 * @property ownWarehouse
 * @constructor
 *
 * @param options
 */
class ListOfWarehousesAdapter(options: FirestoreRecyclerOptions<Warehouse>, var ownWarehouse: Boolean) : FirestoreRecyclerAdapter<Warehouse, ListOfWarehousesAdapter.WarehouseViewHolder>(options) {

    inner class WarehouseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /**
         * Binds visible info about warehouse
         * name and photo
         *
         * @param warehouse warehouse object to show in recycle item view
         */
        fun bindVisible(warehouse: Warehouse) {
            itemView.rv_ownWarehousesListWarehouseName.text = warehouse.name

            Glide.with(itemView)
                .load(warehouse.photoURL)
                .placeholder(R.drawable.avatar_ownwarehouseavatar_primary_color)
                .error(R.drawable.avatar_ownwarehouseavatar_primary_color)
                .into(itemView.rv_ownWarehousesListWarehouseProfileImage)

        }

        /**
         * Binds on click action
         *
         * @param warehouseID id of displayed warehouse
         */
        fun  bindID(warehouseID: String){
            itemView.setOnClickListener{
                val action = ListOfWarehousesFragmentDirections.actionListOfWarehousesFragmentToWarehouseDetailFragment(warehouseID, ownWarehouse)
                itemView.findNavController().navigate(action)
            }

        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WarehouseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.rv_list_of_warehouses_list_item,
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

    }

    override fun onViewAttachedToWindow(holder: WarehouseViewHolder) {
        super.onViewAttachedToWindow(holder)

        //nastavuje animaci
        val animation: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scale_up)
        holder.itemView.startAnimation(animation);
    }


}