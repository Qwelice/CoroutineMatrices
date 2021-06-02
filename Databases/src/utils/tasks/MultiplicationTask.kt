package utils.tasks

class MultiplicationTask(first: String, second: String) : Task(first, second, Operation.MULTIPLICATION) {
    private var i: Int = 0
    private var j: Int = 0
    val currRow: Int
        get() = i
    val currColumn: Int
        get() = j

    override val correct: Boolean
        get() = firstColumnsCount == secondRowsCount

    init {
        if(!correct){
            finish()
        }
    }

    override fun updateProgress() {
        if(j + 1 < secondColumnsCount){
            j++
        }else{
            if(i + 1 < firstRowsCount){
                j = 0
                i++
            }else{
                i++
                j++
                finish()
            }
        }
    }
}