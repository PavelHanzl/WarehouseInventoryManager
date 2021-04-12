package cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseLog

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat.getColor
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.firebase.Timestamp
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import cz.pavelhanzl.warehouseinventorymanager.repository.WarehouseLogItem
import kotlinx.android.synthetic.main.rv_log_warehouse_list_item.view.*

/**
 * Warehouse log adapter
 *
 * @constructor
 *
 * @param options
 */
class WarehouseLogAdapter(options: FirestoreRecyclerOptions<WarehouseLogItem>) : FirestoreRecyclerAdapter<WarehouseLogItem, WarehouseLogAdapter.WarehouseLogItemViewHolder>(options) {

    class WarehouseLogItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /**
         * Binds visible information about warehouse log item
         * Binds name of the item, name of the user who made an action, type of action, count of item, date and time of made operation
         * @param warehouseLogItem
         */
        fun bindVisible(warehouseLogItem: WarehouseLogItem) {
            //název položky
            bindItemName(warehouseLogItem)

            //jméno usera co provedl akci
            bindUser(warehouseLogItem)

            //provedená akce
            bindAction(warehouseLogItem)

            //počet položek
            bindItemCount(warehouseLogItem)

            //datum a čas akce
            bindDateAndTime(warehouseLogItem)

        }

        /**
         * Binds date and time if it is not null or empty
         *
         * @param warehouseLogItem
         */
        private fun bindDateAndTime(warehouseLogItem: WarehouseLogItem) {
            if (warehouseLogItem.date != "") {
                itemView.rv_dateOfAction_warehouseLog.visibility = View.VISIBLE
                itemView.rv_dateOfActionValue_warehouseLog.visibility = View.VISIBLE
                itemView.rv_dateOfActionValue_warehouseLog.text = warehouseLogItem.date
            } else {
                itemView.rv_dateOfAction_warehouseLog.visibility = View.GONE
                itemView.rv_dateOfActionValue_warehouseLog.visibility = View.GONE
            }
        }

        /**
         * Bind item count if it is not null or empty
         *
         * @param warehouseLogItem
         */
        private fun bindItemCount(warehouseLogItem: WarehouseLogItem) {
            if (warehouseLogItem.itemCount != "") {
                itemView.rv_numberOfItems_warehouseLog.visibility = View.VISIBLE
                itemView.rv_numberOfItemsValue_warehouseLog.visibility = View.VISIBLE

                itemView.rv_numberOfItemsValue_warehouseLog.text = warehouseLogItem.itemCount

                //zjistí jestli v poli itemcount je číslo nebo nepřeveditelný string
                val maybeDouble = warehouseLogItem.itemCount.toDoubleOrNull()

                //pokud lze převést na číslo, tak zbraz odpovídající šipečku
                if (maybeDouble != null) {
                    if (warehouseLogItem.itemCount.toDouble() > 0) { //pokud je číslo větší než nula,zobrazí zelenou šipečku
                        itemView.rv_numberOfItemsValue_warehouseLog.setTextColor(Color.parseColor("#42E826"))
                    } else { //pokud je číslo menší než nula,zobrazí červenou šipečku
                        itemView.rv_numberOfItemsValue_warehouseLog.setTextColor(Color.parseColor("#FD3C3C"))
                    }
                } else {
                    itemView.rv_numberOfItemsValue_warehouseLog.setTextColor(Color.parseColor("#141414"))
                }

            } else {
                //skryje popis údaje a hodnotu
                itemView.rv_numberOfItems_warehouseLog.visibility = View.GONE
                itemView.rv_numberOfItemsValue_warehouseLog.visibility = View.GONE
            }
        }

        /**
         * Bind action if it is not null or empty
         *
         * @param warehouseLogItem
         */
        private fun bindAction(warehouseLogItem: WarehouseLogItem) {
            if (warehouseLogItem.logMessage != "") {
                itemView.rv_action_warehouseLog.visibility = View.VISIBLE
                itemView.rv_actionValue_warehouseLog.visibility = View.VISIBLE
                itemView.rv_actionValue_warehouseLog.text = warehouseLogItem.logMessage
            } else {
                itemView.rv_action_warehouseLog.visibility = View.GONE
                itemView.rv_actionValue_warehouseLog.visibility = View.GONE
            }
        }

        /**
         * Bind user if it is not null or empty
         *
         * @param warehouseLogItem
         */
        private fun bindUser(warehouseLogItem: WarehouseLogItem) {
            if (warehouseLogItem.userName != "") {
                itemView.rv_userName_warehouseLog.visibility = View.VISIBLE
                itemView.rv_userNameValue_warehouseLog.visibility = View.VISIBLE
                itemView.rv_userNameValue_warehouseLog.text = warehouseLogItem.userName
            } else {
                itemView.rv_userName_warehouseLog.visibility = View.GONE
                itemView.rv_userNameValue_warehouseLog.visibility = View.GONE
            }
        }

        /**
         * Bind item name if it is not null or empty
         *
         * @param warehouseLogItem
         */
        private fun bindItemName(warehouseLogItem: WarehouseLogItem) {
            if (warehouseLogItem.itemName != "") {
                itemView.rv_WhItemName_warehouseLog.visibility = View.VISIBLE
                itemView.rv_WhItemNameValue_warehouseLog.visibility = View.VISIBLE
                itemView.rv_WhItemNameValue_warehouseLog.text = warehouseLogItem.itemName
            } else {
                itemView.rv_WhItemName_warehouseLog.visibility = View.GONE
                itemView.rv_WhItemNameValue_warehouseLog.visibility = View.GONE
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WarehouseLogItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.rv_log_warehouse_list_item,
            parent,
            false
        )
        return WarehouseLogItemViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: WarehouseLogItemViewHolder,
        position: Int,
        model: WarehouseLogItem
    ) {
        holder.bindVisible(model)

    }

    override fun onViewAttachedToWindow(holder: WarehouseLogItemViewHolder) {
        super.onViewAttachedToWindow(holder)

        //attach animation to recycle view item
        val animation: Animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scale_up)
        holder.itemView.startAnimation(animation);
    }


}