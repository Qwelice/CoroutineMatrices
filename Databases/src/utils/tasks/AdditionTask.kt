package utils.tasks

class AdditionTask(first: String, second: String) : Task(first, second, Operation.ADDITION) {
    override val correct: Boolean
        get() = firstRowsCount == secondRowsCount && firstColumnsCount == secondColumnsCount
    private var i = 0
    val currRow: Int
        get() = i
    init {
        if(!correct){
            finish()
        }
    }

    override fun updateProgress(){
        if(i + 1 < firstRowsCount){
            i++
        }else{
            i++
            finish()
        }
    }
}