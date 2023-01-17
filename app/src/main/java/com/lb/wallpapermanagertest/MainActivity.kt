package com.lb.wallpapermanagertest

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.*
import androidx.core.app.ActivityCompat
import androidx.core.content.*
import java.io.FileInputStream
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val requestVariousStoragePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val areAllStoragePermissionsGranted =
                Utils.getAvailableStoragePermissionsToRequest(this).lastOrNull {
                    ContextCompat.checkSelfPermission(this, it) != PermissionChecker.PERMISSION_GRANTED
                } == null
            if (areAllStoragePermissionsGranted) {
                Log.d("AppLog", "all storage permissions are granted, so checking ")
                testWallpaperFunctions()
            } else {
                showMessage("missing storage permissions...")
            }
        }
    private val manageStoragePermissionRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Utils.isManageStoragePermissionGranted()) {
                testWallpaperFunctions()
            } else {
                showMessage("MANAGE_EXTERNAL_STORAGE permission not granted")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val wallpaperManager = WallpaperManager.getInstance(this)
        findViewById<View>(R.id.useManageExternalStorage).setOnClickListener {
            when {
                Utils.isManageStoragePermissionGranted() -> {
                    testWallpaperFunctions()
                }

                Utils.canRequestManageStoragePermission(this) -> {
                    manageStoragePermissionRequest.launch(Utils.getManageStoragePermissionRequestIntent(this))
                }

                else -> {
                    showMessage("cannot request MANAGE_EXTERNAL_STORAGE permission")
                }
            }
        }
        findViewById<View>(R.id.useMediaPermissions).setOnClickListener {
            val storagePermissionsToRequest = Utils.getAvailableStoragePermissionsToRequest(this)
            storagePermissionsToRequest.forEach {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, it)) {
                    showMessage("need to grant permissions manually (was requested and denied before)")
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
                    return@setOnClickListener
                }
            }
            Log.d("AppLog", "requesting these storage permissions:${storagePermissionsToRequest.joinToString()}")
            requestVariousStoragePermissions.launch(storagePermissionsToRequest.toTypedArray())
        }
    }

    private fun showMessage(msg: String) {
        Log.d("AppLog", msg)
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    @SuppressLint("MissingPermission")
    private fun testWallpaperFunctions() {
        val wallpaperManager = WallpaperManager.getInstance(this)
        thread {
            try {
                val systemWallpaperFile: ParcelFileDescriptor? =
                    wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)
                if (systemWallpaperFile != null) {
                    val bitmap =
                        BitmapFactory.decodeFileDescriptor(systemWallpaperFile.fileDescriptor)
                    Log.d("AppLog", "got systemWallpaperFile bitmap?${bitmap != null}")
                }
                Log.d("AppLog", "got systemWallpaperFile?${systemWallpaperFile != null}")
                systemWallpaperFile?.close()
                val lockWallpaperFile =
                    wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK)
                if(lockWallpaperFile!=null){
                    val bitmap =
                        BitmapFactory.decodeFileDescriptor(lockWallpaperFile.fileDescriptor)
                    Log.d("AppLog", "got lockWallpaperFile bitmap?${bitmap != null}")
                }
                Log.d("AppLog", "got lockWallpaperFile?${lockWallpaperFile != null}")
                lockWallpaperFile?.close()
                val drawable = wallpaperManager.drawable
                Log.d("AppLog", "got wallpaper using drawable?${drawable != null}")
                val fastDrawable = wallpaperManager.fastDrawable
                Log.d("AppLog", "got wallpaper using fastDrawable?${fastDrawable != null}")
                val peekFastDrawable = wallpaperManager.peekFastDrawable()
                Log.d("AppLog", "got wallpaper using peekFastDrawable?${peekFastDrawable != null}")
                val peekDrawable = wallpaperManager.peekDrawable()
                Log.d("AppLog", "got wallpaper using peekDrawable?${peekDrawable != null}")
                runOnUiThread {
                    showMessage("all wallpaper functions worked fine without any exception")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showMessage("failed to get current wallpaper. Exception:$e")
                }
            }
        }

    }
}
