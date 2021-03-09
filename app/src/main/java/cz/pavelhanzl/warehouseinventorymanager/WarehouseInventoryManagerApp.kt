package cz.pavelhanzl.warehouseinventorymanager

import android.app.Application
import android.content.Context

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

fun stringResource(resource: Int): String {
    return WarehouseInventoryManagerApp.applicationContext().getString(resource)
}