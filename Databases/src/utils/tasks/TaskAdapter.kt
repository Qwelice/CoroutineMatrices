package utils.tasks

import com.google.gson.JsonArray

interface TaskAdapter {
    suspend fun inputPromotion(tp: TaskPromotion, taskId: Int)
    suspend fun inputAnswer(tp: TaskPromotion, answer: JsonArray)
    suspend fun inputReady(taskId: Int)
}