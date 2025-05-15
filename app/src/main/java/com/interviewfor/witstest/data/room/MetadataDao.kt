package com.interviewfor.witstest.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: MetadataEntity)

    @Query("SELECT value FROM metadata WHERE 'key' = :key")
    suspend fun getValue(key: String): Long?

    @Query("DELETE FROM metadata")
    suspend fun clearAll()
}