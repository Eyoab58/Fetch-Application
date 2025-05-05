package com.example.fetchapplication

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : ComponentActivity() {
    private lateinit var tv : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv = findViewById(R.id.tv)
        fetchData()
    }


    private fun fetchData(){
        GlobalScope.launch(Dispatchers.IO){
            try{
                val url = URL("https://hiring.fetch.com/hiring.json")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(response)
                val filtered = filterJsonArray(jsonArray)

                withContext(Dispatchers.Main){
                   val list = StringBuilder()

                    for(i in 0 until filtered.length()){
                        val obj = filtered.getJSONObject(i)
                        val id = obj.getInt("id")
                        val listId = obj.getInt("listId")
                        val name = obj.getString("name")
                        list.append("listId: $listId, id: $id, name: $name\n")
                    }
                    tv.text = list.toString()
                }
            } catch(e : Exception){
                Log.e("FetchApp", "Error: ${e.message}")
            }
        }
    }

    private fun filterJsonArray(jsonArray: JSONArray): JSONArray{
        val filteredArray = mutableListOf<JSONObject>()

        for(i in 0 until jsonArray.length()){
            val elem = jsonArray.getJSONObject(i)

            if(!elem.isNull("name") && elem.getString("name").isNotBlank()) {
                filteredArray.add(elem)
            }
        }

        val grouped = filteredArray.groupBy { it.getInt("listId") }
        val sortedList = grouped.toSortedMap().flatMap { (_, group) ->
            group.sortedBy { it.getString("name").lowercase() }
        }
        val result = JSONArray()
        sortedList.forEach { result.put(it) }
        return result
    }
}

