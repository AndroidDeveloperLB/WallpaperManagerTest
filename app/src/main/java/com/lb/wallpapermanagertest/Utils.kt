package com.lb.wallpapermanagertest

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import androidx.annotation.RequiresApi

object Utils {
    @RequiresApi(Build.VERSION_CODES.R)
    fun getManageStoragePermissionRequestIntent(context: Context): Intent =
        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).setData(Uri.parse("package:${context.packageName}"))

    @RequiresApi(Build.VERSION_CODES.R)
    fun getManageStoragePermissionSettingsIntent(): Intent =
        Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)

    fun isManageStoragePermissionGranted() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager() else false

    fun canRequestManageStoragePermission(context: Context) =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && context.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.R
                && context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS).requestedPermissions.contains(Manifest.permission.MANAGE_EXTERNAL_STORAGE)

    fun getAvailableStoragePermissionsToRequest(context: Context): Set<String> {
        val storagePermissionsToCheck =
            hashSetOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
        val packageInfo =
            context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        val declaredPermissions =
            packageInfo.requestedPermissions.toHashSet()
        if (packageInfo.applicationInfo.targetSdkVersion >= 33)
        //READ_EXTERNAL_STORAGE can't be requested when targeting API 33
            declaredPermissions.remove(Manifest.permission.READ_EXTERNAL_STORAGE)
        val result = storagePermissionsToCheck.intersect(declaredPermissions)
        return result
    }
}
