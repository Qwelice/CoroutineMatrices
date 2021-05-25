package utils.params

class ElementsByRow(id: String, row: Int) : HyperParameter() {
    init {
        params["id"] = id
        params["row"] = row
    }

    val id: String
        get() = params["id"] as String
    val row: Int
        get() = params["row"] as Int
}