package services

import com.google.gson.JsonArray
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import utils.tasks.Task
import utils.tasks.TaskAdapter
import utils.tasks.TaskPromotion


class TaskService(private val adapter: TaskAdapter){
    private val tasks = HashMap<Int, TaskPromotion>()

    suspend fun appendTask(taskId: Int, task: Task) = GlobalScope.launch{
        val mutex = Mutex()
        mutex.withLock {
            tasks[taskId] = TaskPromotion(task).apply {
                adapter.inputPromotion(this, taskId)
            }
        }
    }

    suspend fun appendAnswer(taskId: Int, answer: JsonArray) = GlobalScope.launch {
        val mutex = Mutex()
        mutex.withLock {
            val tp = tasks[taskId]
            if(tp != null){
                tp.appendAnswer(answer)
                try{
                    val result = tp.summarizeTask()
                    adapter.inputAnswer(tp, result)
                    tp.updateProgress()
                }catch (ignore: Exception){}
                if(!tp.task.finished){
                    adapter.inputPromotion(tp, taskId)
                }else{
                    adapter.inputReady(taskId)
                    tasks.remove(taskId)
                }
            }
        }
    }

    fun newClient() = GlobalScope.launch {
        val mutex = Mutex()
        mutex.withLock {
            for(k in tasks.keys){
                if(!tasks[k]!!.task.finished){
                    adapter.inputPromotion(tasks[k]!!, k)
                    break
                }
            }
        }
    }
}