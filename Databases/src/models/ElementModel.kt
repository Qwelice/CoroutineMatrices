package models

class ElementModel(id: String, i: Int, j: Int, elValue: Double) : BaseModel() {
    init {
        attributes["id"] = id
        attributes["row"] = i
        attributes["column"] = j
        attributes["value"] = elValue
    }

    val matrixId: String
        get() = attributes["id"] as String

    val i: Int
        get() = attributes["row"] as Int

    val j: Int
        get() =  attributes["columns"] as Int

    val element: Double
        get() = attributes["value"] as Double
}