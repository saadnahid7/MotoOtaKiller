package com.droidrooter.otakiller

import android.os.Process
import android.util.Log
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

/**
 * com.motorola.ccc.ota's notification channels are fixedImportance at the framework level
 * (confirmed via `dumpsys notification`), so pm-disabling the app or demoting its channel from
 * Settings/another Xposed module does nothing — some caller still reaches
 * NotificationManagerService in system_server. This hooks that one chokepoint directly instead of
 * the app, so it doesn't matter who's calling it or whether the app process is even alive.
 * Belt-and-suspenders: if the package ever does get a process started, kill it on sight too.
 */
class HookEntry : XposedModule() {

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        log(Log.INFO, TAG, "module_loaded process=${param.processName}")
    }

    override fun onPackageReady(param: PackageReadyParam) {
        when (param.packageName) {
            "android" -> hookNotificationManager(param.classLoader)
            TARGET_PKG -> {
                log(Log.WARN, TAG, "killing $TARGET_PKG pid=${Process.myPid()} on sight")
                Process.killProcess(Process.myPid())
            }
        }
    }

    // AOSP has renamed/reshaped the enqueue entry point across API levels; match by name on the
    // live system_server class instead of assuming one signature, and skip cleanly if a candidate
    // isn't uniquely resolvable rather than guessing (lsposed-development: fail closed).
    private fun hookNotificationManager(loader: ClassLoader) {
        val cls = try {
            loader.loadClass("com.android.server.notification.NotificationManagerService")
        } catch (e: ClassNotFoundException) {
            log(Log.ERROR, TAG, "NotificationManagerService not found", e)
            return
        }

        var hooked = 0
        for (name in CANDIDATE_METHODS) {
            val matches = cls.declaredMethods.filter { it.name == name }
            if (matches.size != 1) {
                log(Log.WARN, TAG, "skip $name: ${matches.size} matching overload(s), need exactly 1")
                continue
            }
            val method = matches[0]
            if (method.parameterTypes.getOrNull(0) != String::class.java) {
                log(Log.WARN, TAG, "skip $name: arg0 is not the package-name String, signature changed")
                continue
            }
            runCatching {
                hook(method).setId("ota-killer-$name").intercept { chain ->
                    val pkg = chain.getArg(0) as? String
                    if (pkg == TARGET_PKG) {
                        log(Log.INFO, TAG, "blocked notification enqueue from $pkg via $name")
                        Unit
                    } else {
                        chain.proceed()
                    }
                }
            }.onSuccess {
                hooked++
                log(Log.INFO, TAG, "hooked $name on NotificationManagerService")
            }.onFailure {
                log(Log.ERROR, TAG, "hook $name failed", it)
            }
        }
        if (hooked == 0) {
            log(Log.ERROR, TAG, "no enqueue hook installed — check NotificationManagerService signature for this API level")
        }
    }

    companion object {
        const val TAG = "OtaKiller"
        const val TARGET_PKG = "com.motorola.ccc.ota"
        val CANDIDATE_METHODS = listOf("enqueueNotificationWithTag", "enqueueNotificationInternal")
    }
}
