package com.inter.widget

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.inter.MainActivity

@RequiresApi(Build.VERSION_CODES.N)
class QuickNoteTileService : TileService() {

    override fun onClick() {
        super.onClick()
        
        // Launch main activity with intent triggering immediate new note creation
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("action_trigger_new_note", true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+ (API 34)
            val pendingIntent = PendingIntent.getActivity(
                this,
                99,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            state = android.service.quicksettings.Tile.STATE_ACTIVE
            label = "Quick Note"
            updateTile()
        }
    }
}
