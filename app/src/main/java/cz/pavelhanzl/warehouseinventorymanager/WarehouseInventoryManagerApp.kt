package cz.pavelhanzl.warehouseinventorymanager

import android.app.Application
import android.content.Context

/**
 * Warehouse inventory manager app
 *
 * @constructor Create empty Warehouse inventory manager app
 */
class WarehouseInventoryManagerApp: Application() {
    init {
        instance = this
    }

    companion object {
        private var instance: WarehouseInventoryManagerApp? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }
}

/**
 * String resource
 * Returns string value of given id resource
 *
 * @param resource R.id of string
 * @return returns string value of given resource
 */
fun stringResource(resource: Int): String {
    return WarehouseInventoryManagerApp.applicationContext().getString(resource)
}