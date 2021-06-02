package signals

import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

/**
 * Транспортная единица данных
 * */
abstract class Signal  : Serializable {
    companion object{
        fun fromBytesStream(stream: ObjectInputStream) : Signal {
            try{
                val obj = stream.readObject()
                if(obj is Signal){
                    return obj
                }
                throw IOException("Received object is not a Signal!")
            }catch (ex: IOException){
                println(ex.message)
                throw IOException(ex.message)
            }
        }
    }

    fun toBytesStream(stream: ObjectOutputStream){
        try{
            stream.writeObject(this)
        }catch (ex: IOException){
            throw IOException("Failed on sending signal")
        }
    }

    fun getType() : String{
        if(this.javaClass.simpleName.startsWith("Signal")){
            return this.javaClass.simpleName.substringAfter("Signal")
        }
        return this.javaClass.simpleName
    }
}