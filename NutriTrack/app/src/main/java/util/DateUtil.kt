package com.example.nutritrack.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtil {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    fun today(): String = LocalDate.now().format(formatter)
}
