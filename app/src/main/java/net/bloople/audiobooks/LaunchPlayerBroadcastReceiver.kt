package net.bloople.audiobooks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class LaunchPlayerBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notifyIntent = Book.idTo(Intent(context, PlayAudiobookActivity::class.java), Book.idFrom(intent))
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(notifyIntent)
    }
}