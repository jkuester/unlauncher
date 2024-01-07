package com.sduduzog.slimlauncher.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.sduduzog.slimlauncher.models.HomeApp

@Dao
interface BaseDao {

    @get:Query("SELECT * FROM home_apps ORDER BY sorting_index ASC")
    val apps: LiveData<List<HomeApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(app: HomeApp)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg apps: HomeApp)

    @Transaction
    fun remove(app: HomeApp) {
        removeFromTable(app)
        updateIndices(app.sortingIndex)
    }

    @Delete
    fun removeFromTable(app: HomeApp)

    @Query(
        "UPDATE home_apps SET sorting_index = sorting_index - 1 WHERE sorting_index > :sortingIndex"
    )
    fun updateIndices(sortingIndex: Int)

    @Query("DELETE FROM home_apps")
    fun clearTable()
}
