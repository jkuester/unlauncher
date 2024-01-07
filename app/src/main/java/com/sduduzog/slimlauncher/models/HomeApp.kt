package com.sduduzog.slimlauncher.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.sduduzog.slimlauncher.data.model.App

@Entity(tableName = "home_apps", primaryKeys = ["package_name", "user_serial"])
data class HomeApp(
    @field:ColumnInfo(name = "app_name")
    var appName: String,

    @field:ColumnInfo(name = "package_name")
    var packageName: String,

    @field:ColumnInfo(name = "activity_name")
    var activityName: String,

    @field:ColumnInfo(name = "sorting_index")
    var sortingIndex: Int,

    @field:ColumnInfo(name = "app_nickname")
    var appNickname: String? = null,

    @field:ColumnInfo(name = "user_serial")
    val userSerial: Long
) {
    companion object {
        fun from(app: App, sortingIndex: Int = 0): HomeApp {
            return HomeApp(
                appName = app.appName,
                activityName = app.activityName,
                packageName = app.packageName,
                userSerial = app.userSerial,
                sortingIndex = sortingIndex
            )
        }
    }
}
