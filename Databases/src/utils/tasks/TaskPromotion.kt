package utils.tasks

import com.google.gson.JsonArray
import kotlin.math.abs
import java.util.*

class TaskPromotion(val task: Task) {
    private val answers = Stack<JsonArray>()
    private var queries: Int = 0
    val queriesCount: Int
        get() = queries
    private val isFull: Boolean
        get() = answers.size == queries
    fun addQuery(){
        queries++
    }
    fun updateProgress(){
        task.updateProgress()
    }

    fun appendAnswer(answer: JsonArray){
        answers.add(answer)
    }

    fun summarizeTask(): JsonArray {
        val eps = 0.001
        if(isFull){
            if(queries == 1){
                val result = answers.pop()
                answers.clear()
                queries = 0
                return result
            }else if(queries > 1){
                val f = answers.pop()
                val s = answers.pop()
                answers.clear()
                queries = 0
                for(i in 0 until f.size()){
                    if(abs(f[i].asDouble - s[i].asDouble) > eps){
                        throw Exception()
                    }
                }
                return f
            }
        }
        throw Exception()
    }
}