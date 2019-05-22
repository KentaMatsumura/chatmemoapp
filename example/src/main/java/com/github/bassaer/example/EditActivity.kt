package com.github.bassaer.example

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_edit.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast


@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class EditActivity : AppCompatActivity() {

    lateinit var title: String
    lateinit var content: String

    var mMessageDB: MessageDataBase? = null
    var mRealm: Realm = Realm.getDefaultInstance()

    lateinit var am: AlarmManager
    lateinit var pending: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        // アクションバーに前画面に戻る機能をつける
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        // Receive intent data
        val intent = this.intent
        val editDate = intent.extras.getString(ConfigurationActivity.DATE) ?: "Null"
        val editTime = intent.extras.getString(ConfigurationActivity.TIME) ?: "Null"
        val editId = intent.extras.getInt(ConfigurationActivity.ID)
        //TODO 上の受け取りをrealm上で行う

        mMessageDB = this.mRealm.where(MessageDataBase::class.java)
                .equalTo("id", editId)
                .findAll()[0]

        alarm_switch.isChecked = mMessageDB!!.alarmFlag

        Log.d("debug2", mMessageDB!!.id.toString() + alarm_switch.isChecked)

        // Set text
        title_edit_view.setText(mMessageDB!!.title, TextView.BufferType.NORMAL)
        content_edit_view.setText(mMessageDB!!.contents)
        date_button.text = editDate
        time_button.text = editTime

        this.showDatePickerDialog()
        this.showTimePickerDialog()

        alarm_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            // Realm set up
            val realm: Realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            mMessageDB!!.alarmFlag = isChecked

            realm.copyToRealmOrUpdate(mMessageDB)
            realm.commitTransaction()
            realm.close()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            // Save edit data
            R.id.editButton -> {

                putIntentExtra()

                when (mMessageDB!!.alarmFlag) {
                    true -> {
                        Log.d("debug2", "true")
                        setAlarm()
                    }
                    false -> {

                        Log.d("debug2", "false")
                        cancelAlarm()
                    }
                }

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Select date
    private fun showDatePickerDialog() {
        // Get open DatePickerDialog button.
        date_button.setOnClickListener {
            // Create a new OnDateSetListener instance. This listener will be invoked when user click ok button in DatePickerDialog.
            val onDateSetListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, dayOfMonth ->

                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth)

                date_button.text = android.text.format.DateFormat.format("yyyy/MM/dd", calendar)
            }
            // Set alarm date
            val year = date_button.text.split("/")[0].toInt()
            val month = date_button.text.split("/")[1].toInt() - 1
            val day = date_button.text.split("/")[2].toInt()

            // Create the new DatePickerDialog instance.
            val datePickerDialog = DatePickerDialog(
                    this,
                    android.R.style.Theme_Holo_Light_Dialog,
                    onDateSetListener,
                    year,
                    month,
                    day)

            // Set dialog title.
            datePickerDialog.setTitle("Please select date.")

            // Popup the dialog.
            datePickerDialog.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_menu, menu)
        return true
    }

    // Select time
    @SuppressLint("SetTextI18n")
    private fun showTimePickerDialog() {
        time_button.setOnClickListener {
            val onTimeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->

                time_button.text = "%1$02d:%2$02d".format(hour, minute)
            }

            // Set alarm time
            val hour = time_button.text.split(":")[0].toInt()
            val minute = time_button.text.split(":")[1].toInt()

            val is24Hour = true

            val timePickerDialog = TimePickerDialog(
                    this,
                    android.R.style.Theme_Holo_Light_Dialog,
                    onTimeSetListener,
                    hour,
                    minute,
                    is24Hour)

            // Set dialog title
            timePickerDialog.setTitle(R.string.edit_time_title)

            // Popup the dialog
            timePickerDialog.show()
        }
    }

    private fun putIntentExtra() {
        val editIntent = Intent()
        val title = title_edit_view.text.toString()
        val content = content_edit_view.text.toString()
        val date = date_button.text.toString()
        val time = time_button.text.toString()

        // Realm set up
        val realm: Realm = Realm.getDefaultInstance()

        realm.beginTransaction()

        mMessageDB!!.title = title
        mMessageDB!!.contents = content
        mMessageDB!!.date = date
        mMessageDB!!.time = time

        realm.copyToRealmOrUpdate(mMessageDB)
        realm.commitTransaction()
        realm.close()

        editIntent.putExtra(ConfigurationActivity.ID, mMessageDB!!.id)

        setResult(Activity.RESULT_OK, editIntent)
        finish()
    }

    private fun setAlarm() {
        val calenderAlarm = Calendar.getInstance()
        val date = "${date_button.text} ${time_button.text}".toDate()
        val calendarNow = Calendar.getInstance()
        calendarNow.timeInMillis = System.currentTimeMillis()
        calenderAlarm.time = date
        val diffTime: Long = calenderAlarm.timeInMillis - calendarNow.timeInMillis

        if (diffTime < 0) {
            Toast.makeText(application,
                    R.string.edit_incorrect_values,
                    Toast.LENGTH_SHORT).show()
            changeFlag()
            return
        }

        calendarNow.add(Calendar.MILLISECOND, diffTime.toInt())

        val alarmIntent = Intent(applicationContext, AlarmNotification::class.java)
        alarmIntent.putExtra("AlarmCode", 1)
        alarmIntent.putExtra("MessageID", mMessageDB!!.id)
        alarmIntent.putExtra("MessageTitle", mMessageDB!!.title.toString())
        alarmIntent.putExtra("MessageContent", mMessageDB!!.contents.toString())

        pending = PendingIntent.getBroadcast(applicationContext, mMessageDB!!.id, alarmIntent, 0)

        am = getSystemService(ALARM_SERVICE) as AlarmManager

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                val info = AlarmManager.AlarmClockInfo(
                        calendarNow.timeInMillis, null
                )
                am.setExact(AlarmManager.RTC_WAKEUP,
                        calendarNow.timeInMillis, pending)
                am.setAlarmClock(info, pending)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                am.setExact(AlarmManager.RTC_WAKEUP,
                        calendarNow.timeInMillis, pending)
            }
            else -> {
                am.set(AlarmManager.RTC_WAKEUP,
                        calendarNow.timeInMillis, pending)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
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

    private fun cancelAlarm() {
        val cancelIntent = Intent(applicationContext, AlarmNotification::class.java)
        val pending = PendingIntent.getBroadcast(
                applicationContext, mMessageDB!!.id, cancelIntent, 0
        )

        changeFlag()

        val am: AlarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (am != null) {
            am.cancel(pending)
            Toast.makeText(applicationContext,
                    R.string.edit_cancel_alarm,
                    Toast.LENGTH_SHORT).show()
            Log.d("debug22", "cancel")
        } else {
            Log.d("debug22", "null")
        }
    }

    private fun changeFlag() {
        val realm: Realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        when (mMessageDB!!.alarmFlag) {
            true -> mMessageDB!!.alarmFlag = false
            false -> mMessageDB!!.alarmFlag = true
        }
        realm.copyToRealmOrUpdate(mMessageDB)
        realm.commitTransaction()
        realm.close()
    }
}
