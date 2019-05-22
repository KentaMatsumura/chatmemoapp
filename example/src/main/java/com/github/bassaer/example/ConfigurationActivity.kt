package com.github.bassaer.example

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.Menu
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_configuration.*


@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ConfigurationActivity : AppCompatActivity() {

    companion object {
        const val TITLE = "title"
        const val CONTENT = "content"
        const val DATE = "date"
        const val TIME = "time"
        const val ID = "id"
    }

    private val CHANNEL_EDIT = 1

    var mMessageDB: MessageDataBase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)

        // アクションバーに前画面に戻る機能をつける
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        /*  Receive intent data
            From MessengerActivity
         */
        val intent = this.intent
        val configTitle = intent.extras.getString(TITLE)
        val configContent = intent.extras.getString(CONTENT)
        val configDate = intent.extras.getString(DATE)
        val configTime = intent.extras.getString(TIME)
        val configId = intent.extras.getInt(ID)

        // Realm set up
        val realm: Realm = Realm.getDefaultInstance()

        val realmResults = realm.where(MessageDataBase::class.java).findAll()
        realmResults.forEach {
            if (it.id == configId) {
                mMessageDB = it
                return@forEach
            }
        }

        if (mMessageDB == null) {
            // 新規作成
            mMessageDB = MessageDataBase()
            mMessageDB!!.id = configId
            mMessageDB!!.title = configTitle
            mMessageDB!!.contents = configContent
            mMessageDB!!.date = configDate
            mMessageDB!!.time = configTime
        } else {
            //TODO 更新の場合
            mMessageDB = realm.where(MessageDataBase::class.java)
                    .equalTo("id", configId)
                    .findAll()[0]
        }

        realm.beginTransaction()
        realm.copyToRealmOrUpdate(mMessageDB)
        realm.commitTransaction()

        realm.close()

        // Set view text
        title_text_view.text = mMessageDB!!.title
        content_text_view.text = mMessageDB!!.contents
        date_view.text = mMessageDB!!.date
        time_view.text = mMessageDB!!.time

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.config_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val configIntent = Intent()
                val configTitle = title_text_view.text.toString()
                val configContent = content_text_view.text.toString()
                val configDate = date_view.text.toString()
                val configTime = time_view.text.toString()
                configIntent.putExtra(TITLE, configTitle)
                configIntent.putExtra(CONTENT, configContent)
                configIntent.putExtra(DATE, configDate)
                configIntent.putExtra(TIME, configTime)
                setResult(Activity.RESULT_OK, configIntent)
                finish()
                return true
            }
            R.id.addButton -> {
                val intent = Intent(this, EditActivity::class.java)
                val title = title_text_view.text.toString()
                val content = content_text_view.text.toString()
                val date = date_view.text.toString()
                val time = time_view.text.toString()
                val id = mMessageDB!!.id
                intent.putExtra(TITLE, title)
                intent.putExtra(CONTENT, content)
                intent.putExtra(DATE, date)
                intent.putExtra(TIME, time)
                intent.putExtra(ID, id)
                startActivityForResult(intent, CHANNEL_EDIT)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK && requestCode == CHANNEL_EDIT && intent != null) {
            val configId = intent.extras.getInt(ID)

            // Realm set up
            val realm: Realm = Realm.getDefaultInstance()

            mMessageDB = realm.where(MessageDataBase::class.java)
                    .equalTo("id", configId)
                    .findAll()[0]

            title_text_view.text = mMessageDB!!.title
            content_text_view.text = mMessageDB!!.contents
            date_view.text = mMessageDB!!.date
            time_view.text = mMessageDB!!.time

        }
    }

}

