package uk.ac.westminster.mealprep

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = AppDatabase.getDatabase(this)
        var my_dao = db.mealDao()

        // https://www.themealdb.com/api.php/

        val addBtn = findViewById<Button>(R.id.addBtn)
        val searchMealsBtn = findViewById<Button>(R.id.mealsBtn)
        val searchIngredientsBtn = findViewById<Button>(R.id.ingredientBtn)
        runBlocking {
            launch {
                my_dao.clearTable()
            }
        }
        addBtn.setOnClickListener {
            println("add")
            // should populate our database
            popDataBase(my_dao)

        }
        searchMealsBtn.setOnClickListener {
            println("search for meals")
            val intent = Intent(this, SearchMeals::class.java)
            startActivity(intent)
        }
        searchIngredientsBtn.setOnClickListener {
            println("search by ingredient")
            val intent = Intent(this,SearchIngredient::class.java)
            startActivity(intent)
        }
    }
    /*
    helper function that converts our strings to JSON objects
    */
    private fun strToJSON(meal:String) : JSONObject?{
        if (meal.length === 0){
            return null
        }
        var substr:String = ""
        if (meal[meal.length-1] === ',') {
            substr = meal.substring(0,meal.length-1) // take out ending comma
        }
        var jsonString = if (substr !== "") {
            ("{\n" + substr + "\n}").trimIndent()
        } else{
            ("{\n" + meal + "\n}").trimIndent()
        }
        return JSONObject(jsonString.replace("Instuctions", "Instructions")) // to fix typo in given txt
    }
    /*
    populates our database with the Meals from the given txt file
     */
    private fun popDataBase(dao:MealDao): Unit{
        // https://dracopd.users.ecs.westminster.ac.uk/DOCUM/courses/5cosc023w/meals.txt
        var stb = StringBuilder()
        var url_string = "https://dracopd.users.ecs.westminster.ac.uk/DOCUM/courses/5cosc023w/meals.txt"
        val url = URL(url_string)
        val con: HttpURLConnection = url.openConnection() as HttpURLConnection
        runBlocking {
            launch {
                withContext(Dispatchers.IO){
                    var bf = BufferedReader(InputStreamReader(con.inputStream))
                    var line: String? = bf.readLine()
                    while (line != null){
                        stb.append(line + "\n")
                        line = bf.readLine()
                    }
                    val list = stb.toString().split("\n\n")
                    val array = list.toTypedArray()
                    for (idx in 0..array.size - 1){
                        // convert str to JSON then JSON to meal and add to DB
                        var obj = (strToJSON(array[idx]))
                        if (obj !== null){
                            // add obj to db
                            var mealObj = JSONtoMeal(obj)
                            dao.insertMeal(mealObj)
                        }
                    } // adds Meals to our DB
                }
            }
        } // runBlocking end
    }
    /*
    takes in a JSON object as input and returns a Meal object
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
    private fun makeCall(): Unit{
        // this class will make a API call and display an object to the screen
        // create URI
        var stb = StringBuilder()
        var url_String = ""
        val url = URL(url_String)
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
                    //parseJSON(stb)
                }
            }
        }
    }

}