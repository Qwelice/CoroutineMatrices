package utils.tasks

import Operation
import com.google.gson.JsonObject
import com.google.gson.JsonParser

abstract class Task(firstMatrix: String, secondMatrix: String, val operation: Operation){
    private val first: JsonObject = JsonParser.parseString(firstMatrix).asJsonObject
    private val second: JsonObject = JsonParser.parseString(secondMatrix).asJsonObject
    private var done: Boolean = false
    val finished: Boolean
        get() = done
    protected abstract val correct: Boolean

    val firstId: String
        get() = first["id"].asString
    val secondId: String
        get() = second["id"].asString
    val firstRowsCount: Int
        get() = first["rows"].asInt
    val firstColumnsCount: Int
        get() = first["columns"].asInt
    val secondRowsCount: Int
        get() = second["rows"].asInt
    val secondColumnsCount: Int
        get() = second["columns"].asInt

    protected fun finish(){
        done = true
    }

    abstract fun updateProgress()
}
