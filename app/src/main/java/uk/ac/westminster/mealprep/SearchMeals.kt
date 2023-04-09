package uk.ac.westminster.mealprep

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// to search for meals in our database
class SearchMeals: AppCompatActivity() {
    lateinit var mealList: List<Meal>

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_meal)

        val db = AppDatabase.getDatabase(this)
        var my_dao = db.mealDao()

        val searchbtn = findViewById<Button>(R.id.searchBtn)
        val input = findViewById<EditText>(R.id.mealInput)
        val linearlayout = findViewById<LinearLayout>(R.id.mealLL)
        searchbtn.setOnClickListener {
            // display
            if (!input.text.isNullOrBlank()) {
                getMeals(input.text.toString(),my_dao)
                for (meal in mealList){
                    val textView = TextView(this)
                    textView.text = mealToString(meal)
                    linearlayout.addView(textView)
                }
            }
        }
    }
    /*
    takes in a meal object and returns it in the string format we would like to display to the user
     */
    private fun mealToString(meal : Meal) : String{
        var stb = StringBuilder()
        // Meal
        stb.append("\"Meal\": \"${meal.name}\",\n")
        // DrinkAlternate
        stb.append("\"DrinkAlternate\": \"${meal.drinkAlternate}\",\n")
        // Category
        stb.append("\"Category\": \"${meal.category}\",\n")
        // Area
        stb.append("\"Area\": \"${meal.area}\",\n")
        // Instructions
        stb.append("\"Instructions\": \"${meal.instructions}\",\n")
        // Tags
        stb.append("\"Tags\": \"${meal.tags}\",\n")
        // Youtube
        stb.append("\"Youtube\": \"${meal.youtube}\",\n")
        // ingredients and Measures where ingredients and measures are a long string
        // seperated by "/". to be displayed "ingredient1, ingredient2, etc
        val ingredientsArray = meal.ingredients?.split("/")?.toTypedArray()
        val measureArray = meal.measure?.split("/")?.toTypedArray()

        if (ingredientsArray != null && measureArray != null) {
            for (i in ingredientsArray.indices) {
                stb.append("\"Ingredient${i + 1}\": \"${ingredientsArray[i]}\",\n")
            }

            for (i in measureArray.indices) {
                stb.append("\"Measure${i + 1}\": \"${measureArray[i]}\",\n")
            }
        }
        // Remove the last comma and newline characters
        if (stb.isNotEmpty() && stb.last() == '\n') {
            stb.deleteCharAt(stb.length - 1)
        }
        if (stb.isNotEmpty() && stb.last() == ',') {
            stb.deleteCharAt(stb.length - 1)
        }
        return stb.toString()
    }
    private fun getMeals(input:String, dao: MealDao) : Unit{
        runBlocking {
            launch {
                mealList = dao.searchMeals(input)
            }
        }
    }
}