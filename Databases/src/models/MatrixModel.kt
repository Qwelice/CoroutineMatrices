package models

class MatrixModel(id: String, rows: Int, columns: Int) : BaseModel() {
    init {
        attributes["id"] = id
        attributes["rows"] = rows
        attributes["columns"] = columns
    }

    val id: String
        get() = attributes["id"] as String

    val rows: Int
        get() = attributes["rows"] as Int

    val columns: Int
        get() = attributes["columns"] as Int
}