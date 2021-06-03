import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread
import kotlin.random.Random

fun main(){
    val c = Client()
    thread{
        c.connect()
    }
    val str = Scanner(System.`in`).nextLine()
    c.appendMatrices(str)
    /*try{
        val bw = BufferedWriter(FileWriter("client\\src\\testing.csv"))
        val r = Random(1488)
        val lst = ArrayList<Int>()
        for(i in 0 until 3){
            for(j in 0 until 3){
                lst.add(r.nextInt(1, 15))
            }
            bw.write(lst.joinToString(";") + '\n')
            lst.clear()
        }
        bw.close()
    }catch (ex: IOException){

    }*/
}