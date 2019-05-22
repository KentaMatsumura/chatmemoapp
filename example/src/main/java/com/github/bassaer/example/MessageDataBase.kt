package com.github.bassaer.example

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.sql.Time
import java.util.*

open class MessageDataBase : RealmObject() {
    @Index
    var title: String? = null
    var contents: String? = null
    var date: String? = null
    var time: String? = null
    var alarmFlag:Boolean = false

    @PrimaryKey
    var id: Int = 0

}
