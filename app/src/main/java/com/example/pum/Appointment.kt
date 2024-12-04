package com.example.pum

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: String, // format: "YYYY-MM-DD"
    val time: String, // format: "HH:mm"
    val contact: String? = null
)
