package uk.ac.westminster.mealprep

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MealDao {
    @Query("Delete from my_table")
    suspend fun clearTable()
    @Query("Select * from my_table")
    suspend fun getAll(): List<Meal>

    // https://www.sqlitetutorial.net/sqlite-like/
    @Query("Select * from my_table Where LOWER(name) Like '%' || LOWER(:name) || '%'")
    suspend fun getMealsByName(name:String): List<Meal>

    @Query("Select * from my_table where LOWER(ingredients) Like '%' || LOWER(:ingredient) || '%'")
    suspend fun searchMealsByIngredient(ingredient: String): List<Meal>

    @Query("Select * from my_table where LOWER(name) like '%' || LOWER(:input) || '%' OR LOWER(ingredients) like '%' || LOWER(:input) || '%'")
    suspend fun searchMeals(input:String): List<Meal>
    // insert meals
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(vararg meal: Meal)
    // get meals (name || ingredients)


}