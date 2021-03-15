package cz.pavelhanzl.warehouseinventorymanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import kotlinx.android.synthetic.main.fragment_own_warehouses.view.*
import kotlinx.android.synthetic.main.rv_own_warehouses_list_item.view.*

class OwnWarehousesAdapter (var ownWarehousesItems: List<Warehouse>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_own_warehouses_list_item, parent, false)
        return WarehouseViewHolder(view)
    }

    class WarehouseViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun binduj(warehouse: Warehouse) {
            itemView.rv_ownWarehousesListWarehouseName.text = warehouse.name
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as WarehouseViewHolder).binduj(ownWarehousesItems[position])
    }

    override fun getItemCount(): Int {
       return ownWarehousesItems.size
    }

}