package com.kyleyarwood.goalbingo.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromGoalType(value: GoalType?): String? = value?.name

    @TypeConverter
    fun toGoalType(value: String?): GoalType? = value?.let { GoalType.valueOf(it) }
}
