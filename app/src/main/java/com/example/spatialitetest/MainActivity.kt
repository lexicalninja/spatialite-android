package com.example.spatialitetest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import jsqlite.Database
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    lateinit var db: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        copyDbFromAssets()
        val dbFile = File(filesDir.absolutePath + "/countries.sqlite")
        try {
            db = Database()
            db.open(dbFile.absolutePath, jsqlite.Constants.SQLITE_OPEN_READONLY)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        getUsaTest()
        getInUsaTest()
    }

    private fun copyDbFromAssets() {
        val am = assets
        val stream = am.open("database/countries.sqlite")
        FileOutputStream(filesDir.absolutePath + "/countries.sqlite").use {
            stream.copyTo(it)
            it.flush()
        }
    }

    private fun getUsaTest(){
        val query = "SELECT NAME_LONG from geometry_lookup WHERE ID=7"
        executeQuery(query)
    }

    private fun getInUsaTest() {
        val query = "SELECT NAME_LONG from geometry_lookup WHERE Within(MakePoint(-10354375.020855809, 5629995.233439691), geometry_lookup.GEOMETRY)=1"
        executeQuery(query)
    }

    private fun executeQuery(query: String) {
        try {
            val stmt = db.prepare(query)
            while (stmt.step()){
                Log.d("TEST", stmt.column_string(0))
            }
            stmt.close()
        } catch (e: java.lang.Exception){
            e.printStackTrace()
        }
    }


}

