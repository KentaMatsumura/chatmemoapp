package com.github.bassaer.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val intents = Intent(context, EditActivity::class.java)
                .putExtra("onReceive", true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(intents)
    }
}
