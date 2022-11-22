import dataUtils.Config
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.ws
import io.ktor.serialization.WebsocketContentConvertException
import io.ktor.serialization.WebsocketDeserializeException
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.Date
import java.util.Timer
import java.util.TimerTask

object WebSocketManager {
    private val TAG = WebSocketManager::class.java.simpleName
    private const val MAX_RETRY = 5 //maximum  number of reconnection
    private const val MILLIS = 5000
    private lateinit var socketCallbacks: SocketCallBacks
    var config: Config = Config()
    private lateinit var client: HttpClient
    private var session: DefaultClientWebSocketSession? = null
    private var isConnect = false
    private var connectNum = 0
    private var url = ""
    private var routeMap: MutableMap<String,Date> = mutableMapOf<String, Date>()
    private var timer: Timer? = null

    fun init(url:String, socketCallbacks:SocketCallBacks) {

        client = HttpClient(CIO) {

            install(Logging) {
                LogLevel.ALL
            }
            HttpResponseValidator {
                validateResponse {
                    when (it.status.value) {
                        in 100..299 -> {
                            //print(it.content.toString())
                        }
                        in 300..399 -> {
                            //print(it.content.toString())
                        }
                        in 400..499 -> {
                            //print(it.content.toString())
                            //throw ClientRequestException(it)
                        }
                        in 500..599 -> {
                            //print(it.content.toString())
                        }
                    }
                }
            }
            install(WebSockets) {
                maxFrameSize = config.maxFrameSize
                pingInterval = config.pingInterval
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
            install(HttpTimeout) {
                connectTimeoutMillis = config.connectTimeoutMillis
                socketTimeoutMillis = config.socketTimeoutMillis
            }
            install(HttpRequestRetry) {
                maxRetries = config.maxRetries
                retryOnExceptionIf { request, cause ->
                    cause is Error
                }
                delayMillis { retry ->
                    retry * config.retryInterval
                } // will retry in 3, 6, 9, etc. seconds
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
        this.socketCallbacks = socketCallbacks
        this.url = url
    }

    fun isConnect(): Boolean {
        return isConnect
    }

    suspend fun connect() {
        if(isConnect()) {
            println("$TAG : web socket connected")
            return
        }
        socketCallbacks.onConnecting()
        runBlocking {
            try{
                client.ws(url) {
                    session = this
                    isConnect = true
                    if(!session!!.isActive)
                        reconnect()
                    socketCallbacks.onConnected()
                }
            } catch (e: ClosedReceiveChannelException){
                socketCallbacks.onConnectionFailure()
                println("$TAG: ${e.cause}")
            } finally {
                session = null
            }
        }
    }

    suspend fun send(data: Any) {
        if(isConnect()){
            try {
                withContext(Dispatchers.IO){
                    session?.sendSerialized(data)
                    socketCallbacks.onSend()
                    receive()
                }
            } catch (cause: WebsocketContentConvertException) {
                socketCallbacks.onSendError(cause.cause.toString())
                println("$TAG : ${cause.cause}")
            } finally {
                session = null
            }
        }
    }

   suspend fun receive() {
        try {
            session?.incoming?.consumeEach { frame ->
                when (frame) {
                    is Frame.Text -> {
                        val response = frame.readText()
                        println("$TAG : Received :${frame.readText()}")

                        val type = typeConverter(response)
                            socketCallbacks.onReceived(type)
                    }
                    is Frame.Close -> {
                        val reason = session?.closeReason?.await()!!.message
                        println("Closing: $reason")
                    }
                    else -> {
                        println("In Else:${session?.closeReason?.await()!!.message}")
                    }
                }
            }
        } catch (cause: WebsocketDeserializeException) {
            socketCallbacks.onReceiveError(cause.frame.toString())
            println("$TAG : ${cause.frame}")
        } finally {
            session = null
        }
    }

    suspend fun close() {
        if(isConnect()) {
            socketCallbacks.onClose()
            client.close()
            println("Connection closed. Goodbye!")
        }
    }

    suspend fun reconnect() {
        if(connectNum <= MAX_RETRY) {
            try {
                socketCallbacks.onRetry()
                withContext(Dispatchers.IO) {
                    Thread.sleep(MILLIS.toLong())
                }
                connect()
                connectNum++
            } catch (e: InterruptedException) {
                println(e.localizedMessage)
            } finally {
                session = null
            }
        } else {
            println("$TAG : reconnect over $MAX_RETRY, please check url or network")
        }
    }

    fun addRequestIdToRouteTable() {
        routeMap[uuidGenerator()] = getCurrentDateAndTime()
    }

    fun removeRequestIdFromRouteTable(requestId: String) {
        if(routeMap.containsKey(requestId)) {
            routeMap.remove(requestId)
        }
    }

    fun traverseRoute() {
        cancelTimer()
        timer = Timer()
        timer?.scheduleAtFixedRate(object: TimerTask() {
            override fun run() {
                if(routeMap.isNotEmpty()) {
                    routeMap.forEach { entry ->
                        if(getTimeDifference(entry.value)) {
                            routeMap.remove(entry.key)
                        }
                    }
                } else {
                    cancelTimer()
                }
            }

        },0,30000)
    }

    private fun cancelTimer() {
        if(timer!=null) {
            timer!!.cancel()
            timer =  null
        }
    }

}