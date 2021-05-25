package models

abstract class BaseModel {
    protected val attributes = HashMap<String, Any>()

    fun getAttribute(atrName: String) : Any? = attributes[atrName]

    operator fun get(atrName: String) : Any? = attributes[atrName]
}