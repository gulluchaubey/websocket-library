import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.kotlinx.serializer.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.plugins.websocket.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json
import java.lang.Exception

class RealTimeSystem {
    var onConnecting: (()->Unit)? = null
    var onConnected: (()->Unit)? = null
    var onFailure: ((message:String)->Unit)? = null
    var onRetrying: ((message:String)->Unit)? = null
    var onDisconnecting: (()->Unit)? = null
    var onDisconnected: ((message:String)->Unit)? = null
    var onReceived: ((message:String)->Unit)? = null

    suspend fun connect() {
        val url = Url("wss://socketsbay.com/wss/v2/2/demo/")
        //val url = Url("wss://demo.piesocket.com/v3/channel_123?api_key=VCXCEuvhGcBDP7XhiJJUDvR1e1D3eiVjgZ9VRiaV&notify_self")
        //val url = Url("wss://demo.piesocket.com/v3/channel_123?api_key=VCXCEuvhGcBDP7XhiJJUDvR1e1D3eiVjgZ9VRiaV&notify_self")
        val ktor = HttpClient(CIO) {
            install(Logging) {
                LogLevel.ALL
            }
            install(WebSockets){
                maxFrameSize = Long.MAX_VALUE
                pingInterval = 20000

                //KotlinxWebsocketSerializationConverter(Json)
                //contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }

           /* install(ContentNegotiation) {
                json(Json{
                    prettyPrint = true
                    isLenient = true
                })
            }*/

            /*defaultRequest {
                url(BASE_URL)
                if (token.isNotEmpty()) {
                    header(AUTHORIZATION, token)
                }
                contentType(ContentType.Application.Json)
            }*/

            install(HttpTimeout) {
                requestTimeoutMillis = 1000
                connectTimeoutMillis = 1000
                socketTimeoutMillis = 1000
            }
            install(HttpRequestRetry) {
                //retryOnServerErrors(maxRetries = 3)
                maxRetries = 3
                retryOnExceptionIf { _, cause ->
                    onRetrying?.invoke(cause.toString())
                    println("Retrying due to $cause")
                    cause is Exception
                }
                delayMillis { retry -> retry * 3000L } // will retry in 3, 6, 9, etc. seconds
            }
        }
        try{
            onConnecting?.invoke()
            ktor.wssRaw(Get, url.host, url.port, url.encodedPath) {
                println("Connected to $url!")
                onConnected?.invoke()
                // Access to a WebSocket session
                // Send a message outbound
                /*val json = JSONObject()
                    .put("action", "sendmessage")
                    .put("data", "Hello from Android!")
                    .toString()*/
                outgoing.send(Frame.Text("json"))

                // Receive an inbound message
                incoming.consumeEach { frame ->
                    when (frame) {
                        is Frame.Text -> {
                            onReceived?.invoke(frame.readText())
                            println("In Text Frame:${frame.readText()}")
                            /*ktor.close()
                            println("Connection closed. Goodbye!")*/
                        }
                        is Frame.Close -> {
                            //val reason = closeReason.await()!!.message
                            //val reason = closeReason.await()!!.message
                            //println("Closing: $reason")
                            //onDisconnected?.invoke(reason)
                            println("In Close:${frame.frameType}")
                        }
                        is Frame.Ping -> {
                            println("Ping Received: ${frame.buffer}")
                            send(Frame.Pong(frame.buffer))
                        }
                        is Frame.Pong -> {
                            println("Pong Received: ${frame.buffer}")
                        }
                        else -> {
                            //println("In Else:${closeReason.await()!!.message}")
                        }
                    }
                }
            }
        }catch (e: Throwable){
            val message = "WebSocket failed: ${e.message}"
            onFailure?.invoke(message)
            println(message)
        }
    }


    /*fun typeConverter(args: Any): Any {
        when (args) {
            is Boolean -> return args
            is Int -> return args
            is Float -> return args
            is Double -> return args
            is String -> return args
            else -> throw Exception("unhandled return type")
        }
    }

    fun String.intOrString(): Any {
        val v = toIntOrNull()
        return when(v) {
            null -> this.booleanString()
            else -> v
        }
    }
     fun String.booleanString(): Any {
         val v = toBooleanStrictOrNull()
         return when(v) {
             null -> this.floatString()
             else -> v
         }
     }
    fun String.floatString(): Any {
        val v = toFloatOrNull()
        return when(v) {
            null -> this.doubleString()
            else -> v
        }
    }
    fun String.doubleString(): Any {
        val v = toDoubleOrNull()
        return when(v) {
            null -> this
            else -> v
        }
    }*/

    /*val stub = Stub(
           jsonObject["id"]!!.jsonPrimitive.int,
           jsonObject["displayName"]!!.jsonPrimitive.content,
           jsonObject["payload"]!!.toString()
       )*/

    //val customer = session?.call?.response?.status?.value
    //val customer = session?.incoming?.receive() as Frame.Ping
    //val customer = session?.incoming
    /*if (customer != null) {
        socketCallbacks.onReceived(customer.toString())
        println("A customer with id $customer is received by the client.")
    }*/

    /* val gson = Gson()
           val json = gson.toJson(args)
           println("$TAG : $json")*/

    /* override fun onReceived(data: Customer) {
       println("$TAG : onReceived")
       println("$TAG : ${data.message}")
       println("$TAG : ${data.connectionId}")
       println("$TAG : ${data.requestId}")
   }*/
    /*var gson = Gson()
       var json = gson.toJson(data)
       println("$TAG : $json")
       val myClass = Gson().fromJson(json, Customer::class.java)
       println(myClass)*/


    /*var gson = Gson()
    var json = gson.toJson(data)
    println("$TAG : ${json}")*/

    //val jsonObject = JsonToken(data).ne

    /*"4".intOrString() // 4
    "x".intOrString() // x*/

    /*WebSocketManager.init(serverUrl,this)
        WebSocketManager.connect()*/

    /*WebSocketManager.apply {
        config.apply {
            pingInterval(60000)
            connectTimeoutMillis(60000)
            socketTimeoutMillis(60000)
            maxRetries(5)
            retryInterval(1000)
            maxFrameSize(Long.MAX_VALUE)
        }
            init(serverUrl,this@Presenter)
        connect()
    }*/


    /*retryIf { request, response ->
                    !response.status.isSuccess()
                }*/
    /*retryOnExceptionIf { _, cause ->
        println("Retrying due to $cause")
        cause is Exception
    }*/

}