package uk.ac.westminster.mealprep

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

// search ingredient grabs meals from the api and adds them to DB
class SearchIngredient: AppCompatActivity() {
    lateinit var mealList: List<Meal>
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_ingredient)

        val input = findViewById<EditText>(R.id.ingredientInput)
        val retrieve = findViewById<Button>(R.id.retrieveBtn)
        val save = findViewById<Button>(R.id.saveBtn)
        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        val linearlayout = findViewById<LinearLayout>(R.id.listMealsLL)

        val db = AppDatabase.getDatabase(this)
        var my_dao = db.mealDao()

        retrieve.setOnClickListener {
            var userInput = input.text
            if (userInput != null){
                println("retrieving " + userInput + "...")
                val mealStr = callAPI(userInput.toString())
                mealList = strToMeals(mealStr)
                println(mealList)

                linearlayout.removeAllViews()
                // we want to display this mealList
                // and potentially add these meals to the db
                for (meal in mealList){
                    val textView = TextView(this)
                    textView.text = mealToString(meal)
                    linearlayout.addView(textView)
                }
            }
        }
        save.setOnClickListener {
            println("saving meals to Database...")
            // if there is meals in meal list, add meals to db
            runBlocking {
                launch {
                    for (meal in mealList){
                        // add to db
                        my_dao.insertMeal(meal)
                    }
                }
            }

        }
    }
    /*
    searched by ingredients given an input. Will return a String that is full of JSON objects
    with a summary of each meal.
     */
    private fun callAPI(input:String) : String{
        var urlString = "https://www.themealdb.com/api/json/v1/1/filter.php?i=" + input.replace(" ","_")
        var stb = StringBuilder()
        val url = URL(urlString)
        val con: HttpURLConnection = url.openConnection() as HttpURLConnection// need to open in new thread (in co routine)
        runBlocking {
            launch {
                withContext(Dispatchers.IO){
                    var bf = BufferedReader(InputStreamReader(con.inputStream))
                    var line: String? = bf.readLine()
                    while (line != null){
                        stb.append(line + "\n")
                        line = bf.readLine()
                    }
                }
            }
        }
        return stb.toString()
    }
    /*
    same as the method from our main activity. Takes a JSON obj and turns it into a Meal
     */
    private fun JSONtoMeal(JSON_obj:JSONObject) : Meal{
        // add all info we need for meal obj
        val name = JSON_obj["Meal"].toString()    // meal name
        val drinkAlternate = JSON_obj["DrinkAlternate"].toString()   // drinkAlternate
        val category = JSON_obj["Category"].toString()     // category
        val area = JSON_obj["Area"].toString()     // area
        val instructions = JSON_obj["Instructions"].toString()     // instructions
        val thumb = JSON_obj["MealThumb"].toString()     // thumb
        val tags = JSON_obj["Tags"].toString() // tags
        val youtube = JSON_obj["Youtube"].toString()
        // ingredients
        val ingredients_stb = StringBuilder()
        var count = 1
        val measures_stb = StringBuilder()
        while (true) {
            try{
                ingredients_stb.append(JSON_obj["Ingredient" + count].toString() + "/")
                measures_stb.append(JSON_obj["Measure" + count].toString() + "/")
            }catch (e: Exception){
                break
            }
            // increment
            count++
        }
        return Meal(name, drinkAlternate, category, area, instructions, thumb, tags, ingredients_stb.toString(), measures_stb.toString(), youtube)
    }
    /*
    this method looks up by id from the webservice returns a Meal object for this id
     */
    private fun searchID(id:String): Meal{
        var urlString = "https://www.themealdb.com/api/json/v1/1/lookup.php?i=" + id
        var stb = StringBuilder()
        val url = URL(urlString)
        val con: HttpURLConnection = url.openConnection() as HttpURLConnection// need to open in new thread (in co routine)
        runBlocking {
            launch {
                withContext(Dispatchers.IO){
                    var bf = BufferedReader(InputStreamReader(con.inputStream))
                    var line: String? = bf.readLine()
                    while (line != null){
                        stb.append(line + "\n")
                        line = bf.readLine()
                    }
                }
            }
        }
        // turn stb into JSON, turn JSON into meal
        val json = JSONObject(stb.toString())
        val mealArray = json.getJSONArray("meals")
        val mealObj = mealArray.getJSONObject(0)
        val newMealObj = JSONObject()
        val keys = mealObj.keys()
        while (keys.hasNext()){
            val key = keys.next()
            if (key.startsWith("str")){
                val newKey = key.substring(3)
                newMealObj.put(newKey,mealObj.get(key))
            }else{
                // add to newMealObj
                newMealObj.put(key,mealObj.get(key))
            }
        }
        return JSONtoMeal(newMealObj)
    }
    /*
    this method takes our string of meal outputs from the ingredient search
    and will return a list of meal objects that we can display to the screen and add to the database
     */
    fun strToMeals(strMeals : String) : List<Meal>{
        val json = JSONObject(strMeals)
        val meals = json.getJSONArray("meals")
        var myList = mutableListOf<Meal>()

        for (i in 0 until meals.length()){
            val meal = meals.getJSONObject(i)
            val id = meal.getString("idMeal")
            // use id to get full meal details and make into meal obj to add to list
            val mealToAdd = searchID(id)
            myList.add(mealToAdd)
        }
        return myList
    }
    /*
    this method takes a meal object and returns a string representation of it that we want to
    display to the user
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

}