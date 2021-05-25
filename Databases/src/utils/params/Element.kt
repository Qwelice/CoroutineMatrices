package utils.params

class Element(id: String, i: Int, j: Int) : HyperParameter() {
    init {
        params["id"] = id
        params["row"] = i
        params["column"] = j
    }

    val id: String
        get() = params["id"] as String
    val row: Int
        get() = params["row"] as Int
    val column: Int
        get() = params["column"] as Int
}