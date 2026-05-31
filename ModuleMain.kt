package com.dynamicisland.app

import android.content.Context
import android.content.Intent
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ModuleMain : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam?.packageName == "android" && lpparam.processName == "android") {
            // Sistem sunucusunda servisi başlat
            val context = AndroidAppHelper.currentApplication() as Context
            val intent = Intent(context, MediaListenerService::class.java)
            context.startForegroundService(intent)
        }
    }
}