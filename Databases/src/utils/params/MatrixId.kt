package utils.params

class MatrixId(id: String) : HyperParameter() {
    init {
        params["id"] = id
    }

    val id: String
        get() = params["id"] as String
}