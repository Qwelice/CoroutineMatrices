package utils.params

class ElementsByColumn(id: String, column: Int) : HyperParameter() {
    init {
        params["id"] = id
        params["column"] = column
    }

    val id: String
        get() = params["id"] as String
    val column: Int
        get() = params["column"] as Int
}