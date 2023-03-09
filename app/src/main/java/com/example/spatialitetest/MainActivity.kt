package com.example.spatialitetest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import jsqlite.Database
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    lateinit var db: Database
    private val dbAssetDir = "database"
    private val dbFileName = "/italy.sqlite"
    private val SEP = "********************************************\n"
    private val ERROR = "\tERROR: "
    private val testLong = "7.474281398163744"
    private val testLat = "44.51649198163321"
    private val lookupTable = "Comuni_11"
    private val geometryColumn = "Geometry"
    private val nameColumn = "NOME"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        copyDbFromAssets()
        val dbFile = File(filesDir.absolutePath +dbFileName)
        try {
            db = Database()
            db.open(dbFile.absolutePath, jsqlite.Constants.SQLITE_OPEN_READONLY)
//            var result = queryComuni()
//            Log.d("TEST", result)
//            result = queryComuniWithGeom()
//            Log.d("TEST", result)
//            result = doSimpleTransform()
//            Log.d("TEST", result)
            getInBuscaTest()
            getInRodelloTest()
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    private fun copyDbFromAssets() {
        val am = assets
        val stream = am.open("${dbAssetDir}${dbFileName}")
        FileOutputStream(filesDir.absolutePath + dbFileName).use {
            stream.copyTo(it)
            it.flush()
        }
    }

    private fun getUsaTest(){
        val query = "SELECT NAME_LONG from geometry_lookup WHERE ID=7"
        executeQuery(query)
    }

    private fun getInBuscaTest() {
        val query = "SELECT ${nameColumn} from ${lookupTable} WHERE Within(Transform(MakePoint(${testLong}, ${testLat}, 4326), 32632), ${lookupTable}.${geometryColumn})"
        executeQuery(query)
    }

    private fun getInRodelloTest() {
        val query = "SELECT ${nameColumn} from ${lookupTable} WHERE Within(Transform(MakePoint(8.064450284144561, 44.62897175193941, 4326), 32632), ${lookupTable}.${geometryColumn})"
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

    private fun queryComuni(): String {
        val sb = StringBuilder()
        sb.append("Query Comuni...\n")
        val query = "SELECT NOME" +  //
                " from Comuni_11" +  //
                " order by NOME;"
        sb.append("Execute query: ").append(query).append("\n")
        try {
            val stmt = db.prepare(query)
            var index = 0
            while (stmt.step()) {
                val nomeStr = stmt.column_string(0)
                sb.append("\t").append(nomeStr).append("\n")
                if (index++ > 5) {
                    break
                }
            }
            sb.append("\t...")
            stmt.close()
        } catch (e: java.lang.Exception) {
            error(e)
        }
        sb.append("Done...\n")
        return sb.toString()
    }


    private fun queryComuniWithGeom(): String {
        val sb = StringBuilder()
        sb.append(SEP)
        sb.append("Query Comuni with AsText(Geometry)...\n")
        val query = "SELECT NOME" +  //
                " , AsText(Geometry)" +  //
                " as geom from Comuni_11" +  //
                " where geom not null;"
        sb.append("Execute query: ").append(query).append("\n")
        try {
            val stmt = db.prepare(query)
            while (stmt.step()) {
                val nomeStr: String = stmt.column_string(0)
                val geomStr: String = stmt.column_string(1)
                var substring = geomStr
                if (substring.length > 40) substring = geomStr.substring(0, 40)
                sb.append("\t").append(nomeStr).append(" - ").append(substring).append("...\n")
                break
            }
            stmt.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            sb.append(ERROR).append(e.localizedMessage).append("\n")
        }
        sb.append("Done...\n")
        return sb.toString()
    }

    fun doSimpleTransform(): String {
        val sb = StringBuilder()
        sb.append("Coordinate transformation...\n")
        val query = "SELECT AsText(Transform(MakePoint($testLong, $testLat, 4326), 32632));"
        sb.append("Execute query: ").append(query).append("\n")
        try {
            val stmt = db.prepare(query)
            if (stmt.step()) {
                val pointStr = stmt.column_string(0)
                sb.append("\t").append(("$testLong/$testLat").toString() + "/EPSG:4326").append(" = ") //
                    .append("$pointStr/EPSG:32632").append("...\n")
            }
            stmt.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            sb.append(ERROR).append(e.localizedMessage).append("\n")
        }
        sb.append("Done...\n")
        return sb.toString()
    }
}

