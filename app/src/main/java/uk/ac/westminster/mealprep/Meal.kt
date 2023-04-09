package uk.ac.westminster.mealprep

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "my_table")
data class Meal (
    @PrimaryKey val name: String,

    val drinkAlternate: String?,
    val category: String?,
    val area: String?,
    val instructions: String?,
    val thumb: String?,
    val tags: String?, // unsure if this will be list or string
    val ingredients: String?,
    val measure: String?,
    val youtube: String?


    )

