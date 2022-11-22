import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

suspend fun main(args: Array<String>) {
       var presenterClass : Presenter = Presenter()
    presenterClass.presenter()
    }

fun socket() {
    val client = HttpClient(CIO) {
        install(Logging) {
            LogLevel.ALL
        }

        install(WebSockets) {
            maxFrameSize = Long.MAX_VALUE
            pingInterval = 60000

            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 60000
            socketTimeoutMillis = 60000
        }
        install(HttpRequestRetry) {
            maxRetries = 10
            retryOnExceptionIf { _, cause ->
                println("Retrying due to $cause")
                cause is Exception
            }
            delayMillis { retry -> retry * 1000L } // will retry in 3, 6, 9, etc. seconds
        }
    }
    runBlocking {
        try{
            println("onConnecting")
            //client.ws("wss://demo.piesocket.com/v3/channel_123?api_key=VCXCEuvhGcBDP7XhiJJUDvR1e1D3eiVjgZ9VRiaV&notify_self") {
            client.ws("wss://wts7zfnin2.execute-api.ap-south-1.amazonaws.com/development") {
                println("onConnected")
                /*val messageOutputRoutine = launch { outputMessages() }
               val userInputRoutine = launch { inputMessages() }

                  userInputRoutine.join() // Wait for completion; either "exit" or error
                   if (userInputRoutine.isCompleted) {
                       messageOutputRoutine.join()
                   }*/

                sendSerialized(Customer("1", "Divya", "Chaubey"))
                try {
                    val customer = receiveDeserialized<Customer>()
                    println("A customer with id ${customer.message} is received by the client.")
                } catch (cause: WebsocketDeserializeException) {
                    println(cause.frame)
                }
            }
            client.close()
            println("Connection closed. Goodbye!")
        } catch(e: Exception) {
            println(e.localizedMessage)
            println("onFailure")
            client.close()
            println("Connection closed. Goodbye!")
        }
    }
}

suspend fun DefaultClientWebSocketSession.outputMessages() {
    try {
        incoming.consumeEach { frame ->
            when (frame) {
                is Frame.Text -> {
                    //println("onReceived")
                    println("Received :${frame.readText()}")
                }
                is Frame.Close -> {
                    val reason = closeReason.await()!!.message
                    println("Closing: $reason")
                    println("onDisconnected")
                    println("In Close:${frame.frameType}")
                }
                else -> {
                    println("In Else:${closeReason.await()!!.message}")
                }
            }
        }
    } catch (e: Exception) {
        println("Error while receiving: " + e.localizedMessage)
    }
}

suspend fun DefaultClientWebSocketSession.inputMessages() {
    var i = 10
     while (i>0) {
    try {
        delay(1000)
        send("Frames $i")
        i--
    } catch (e: Exception) {
        println("Error while sending: " + e.localizedMessage)
        return
    }
     }
    return
}
