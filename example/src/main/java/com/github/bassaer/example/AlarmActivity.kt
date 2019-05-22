package com.github.bassaer.example

import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager.LayoutParams.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_alarm.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AlarmActivity : AppCompatActivity()
        , SimpleAlertDialog.OnClickListener
        , DatePickerFragment.OnDateSelectedListener
        , TimePickerFragment.OnTimeSelectedListener {

    override fun onSelected(year: Int, month: Int, date: Int) {
        val c = Calendar.getInstance()
        c.set(year, month, date)
        date_text.text = android.text.format.DateFormat.format("yyyy/MM/dd", c)
    }

    override fun onSelected(hourOfDay: Int, minute: Int) {
        time_text.text = "%1$02d:%2$02d".format(hourOfDay, minute)
    }

    override fun onPositiveClick() {
        finish()
    }

    override fun onNegativeClick() {
        val calender = Calendar.getInstance()
        calender.timeInMillis = System.currentTimeMillis()
        calender.add(Calendar.MINUTE, 5)
        setAlarmManager(calender)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.getBooleanExtra("onReceive", false) == true) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                    window.addFlags(FLAG_TURN_SCREEN_ON or
                            FLAG_SHOW_WHEN_LOCKED)
                else ->
                    window.addFlags(FLAG_TURN_SCREEN_ON or
                            FLAG_SHOW_WHEN_LOCKED or
                            FLAG_DISMISS_KEYGUARD)
            }
        }

        setContentView(R.layout.activity_alarm)

        set_alarm.setOnClickListener {
            val date = "${date_text.text} ${time_text.text}".toDate()
            when {
                date != null -> {
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    setAlarmManager(calendar)
                    Toast.makeText(
                            this,
                            "Set alarm",
                            Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(
                            this,
                            "Date format is incorrect",
                            Toast.LENGTH_SHORT).show()
                }
            }
        }

        cancel_alarm.setOnClickListener {
            cancelAlarmManager()
        }

        date_text.setOnClickListener {
            val dialog = DatePickerFragment()
            dialog.show(supportFragmentManager, "date_dialog")
        }

        time_text.setOnClickListener {
            val dialog = TimePickerFragment()
            dialog.show(supportFragmentManager, "time_dialog")
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setAlarmManager(calendar: Calendar) {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        val pending = PendingIntent.getBroadcast(this, 0, intent, 0)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                val info = AlarmManager.AlarmClockInfo(
                        calendar.timeInMillis, null
                )
                am.setAlarmClock(info, pending)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                am.setExact(AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis, pending)
            }
            else -> {
                am.set(AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis, pending)
            }
        }
    }

    private fun cancelAlarmManager() {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        val pending = PendingIntent.getBroadcast(this, 0, intent, 0)
        am.cancel(pending)
    }

    fun String.toDate(pattern: String = "yyyy/MM/dd HH:mm"): Date? {
        val sdFormat = try {
            SimpleDateFormat(pattern)
        } catch (e: IllegalAccessException) {
            null
        }
        val date = sdFormat?.let {
            try {
                it.parse(this)
            } catch (e: ParseException) {
                null
            }
        }
        return date
    }
}
