import kotlinx.coroutines.runBlocking

fun main(){
    runBlocking {
        val cl = Client()
        cl.connect()
        cl.startClient()
    }
}