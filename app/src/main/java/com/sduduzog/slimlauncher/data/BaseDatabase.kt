package com.sduduzog.slimlauncher.data

import android.os.Process
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sduduzog.slimlauncher.models.HomeApp


@Database(entities = [HomeApp::class], version = 9, exportSchema = false)
abstract class BaseDatabase : RoomDatabase() {

    abstract fun baseDao(): BaseDao

    companion object {

         val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `home_apps` ADD COLUMN `sorting_index` INTEGER NOT NULL DEFAULT 0")
                val cursor = database.query("SELECT package_name FROM home_apps")
                cursor.moveToFirst()
                var index = 0
                while (!cursor.isAfterLast) {
                    val column = cursor.getString(cursor.getColumnIndexOrThrow("package_name"))
                    database.execSQL("UPDATE `home_apps` SET `sorting_index`=$index WHERE `package_name`='$column'")
                    cursor.moveToNext()
                    index++
                }
            }
        }

         val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `notes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `title` TEXT, `body` TEXT NOT NULL, `edited` INTEGER NOT NULL)")
            }
        }

         val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS `apps`")
                database.execSQL("CREATE TABLE IF NOT EXISTS `tasks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `body` TEXT NOT NULL, `is_complete` INTEGER NOT NULL, `sorting_index` INTEGER NOT NULL)")
            }
        }

         val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `notes` RENAME TO `notes_old`")
                database.execSQL("CREATE TABLE IF NOT EXISTS `notes` (`id` INTEGER PRIMARY KEY NOT NULL, `body` TEXT NOT NULL, `title` TEXT, `edited` INTEGER NOT NULL)")
                database.execSQL("INSERT INTO `notes` (`id`, `body`, `edited`, `title`) SELECT `id`, `body`, `edited`, `title` FROM `notes_old`")
                database.execSQL("ALTER TABLE `notes` ADD COLUMN `type` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `notes` ADD COLUMN `filename` TEXT")
            }
        }
         val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS `notes`")
                database.execSQL("DROP TABLE IF EXISTS `tasks`")
            }
        }
         val MIGRATION_6_7 = object : Migration(6, 7){
            override fun migrate(database: SupportSQLiteDatabase) {
               database.execSQL("ALTER TABLE `home_apps` ADD COLUMN `app_nickname` TEXT")
            }
        }
        val MIGRATION_7_8 = object : Migration(7, 8){
            override fun migrate(database: SupportSQLiteDatabase) {
                val userSerial = Process.myUserHandle().hashCode()
                database.execSQL("ALTER TABLE `home_apps` ADD COLUMN `user_serial` INTEGER NOT NULL DEFAULT $userSerial")

                database.execSQL("CREATE TABLE home_apps_copy(" +
                        "package_name TEXT NOT NULL, " +
                        "user_serial INTEGER NOT NULL, " +
                        "app_name TEXT NOT NULL, " +
                        "app_nickname TEXT, " +
                        "activity_name TEXT NOT NULL, " +
                        "sorting_index INTEGER NOT NULL, " +
                        "PRIMARY KEY(package_name, user_serial))"
                )
                database.execSQL("INSERT INTO home_apps_copy (package_name, user_serial, app_name, app_nickname, activity_name, sorting_index) " +
                        "SELECT package_name, user_serial, app_name, app_nickname, activity_name, sorting_index FROM home_apps")
                database.execSQL("DROP TABLE home_apps")
                database.execSQL("ALTER TABLE home_apps_copy RENAME TO home_apps")
            }
        }
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val userSerial = Process.myUserHandle().hashCode().toLong()
                val statement =
                    database.compileStatement("DELETE FROM home_apps WHERE user_serial = ? AND package_name NOT IN (SELECT package_name FROM home_apps WHERE user_serial = ? ORDER BY sorting_index ASC limit 6)")
                statement.bindLong(1, userSerial)
                statement.bindLong(2, userSerial)
                statement.execute()
            }
        }
    }
}
