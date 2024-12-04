package com.example.pum

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_appointments")
data class HistoryAppointment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val originalId: Long,
    val title: String,
    val date: String,
    val time: String,
    val addedAt: Long
)