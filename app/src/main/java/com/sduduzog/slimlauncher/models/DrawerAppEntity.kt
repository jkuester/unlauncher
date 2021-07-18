package com.sduduzog.slimlauncher.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.sduduzog.slimlauncher.data.model.App

@Entity(tableName = "drawer_apps", primaryKeys = ["package_name", "user_serial"])
data class DrawerApp(
    @field:ColumnInfo(name = "activity_name")
    var activityName: String,

    @field:ColumnInfo(name = "app_name")
    var appName: String,

    @field:ColumnInfo(name = "package_name")
    var packageName: String,

    @field:ColumnInfo(name = "user_serial")
    val userSerial: Long
) {
    companion object {
        fun from(app: App): DrawerApp {
            return DrawerApp(
                appName = app.appName,
                activityName = app.activityName,
                packageName = app.packageName,
                userSerial = app.userSerial
            )
        }
    }
}