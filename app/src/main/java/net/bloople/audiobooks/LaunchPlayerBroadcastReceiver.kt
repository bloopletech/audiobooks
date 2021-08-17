package net.bloople.audiobooks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class LaunchPlayerBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notifyIntent = Intent(context, PlayAudiobookActivity::class.java)
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notifyIntent.putExtra("_id", intent.getLongExtra("_id", -1))
        context.startActivity(notifyIntent)
    }
}