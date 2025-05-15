package com.interviewfor.witstest.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metadata")
data class MetadataEntity(
    @PrimaryKey val key: String,
    val value: Long
)
