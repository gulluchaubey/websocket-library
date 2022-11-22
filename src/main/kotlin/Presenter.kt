import kotlinx.coroutines.runBlocking

class Presenter : SocketCallBacks {

    private val TAG = Presenter::class.java.simpleName

    suspend fun presenter() {
        val serverUrl = "wss://wts7zfnin2.execute-api.ap-south-1.amazonaws.com/development"
        //val serverUrl = "wss://demo.piesocket.com/v3/channel_123?api_key=VCXCEuvhGcBDP7XhiJJUDvR1e1D3eiVjgZ9VRiaV&notify_self"

        WebSocketManager.apply {
            config.apply {
                pingInterval = -60000
                connectTimeoutMillis = 60000
                socketTimeoutMillis = 60000
                maxRetries = 5
                retryInterval = 1000
                maxFrameSize = Long.MAX_VALUE
            }
            init(serverUrl,this@Presenter)
            connect()
        }
    }

    override fun onConnecting() {
        println("$TAG : onConnecting")
    }

    override fun onConnected() {
        println("$TAG : onConnected")
        runBlocking {
            //WebSocketManager.send(Customer("one","two","three"))
            //WebSocketManager.send("Customer")
            if(WebSocketManager.isConnect())
            WebSocketManager.send(123455678)
            else
                println("$TAG : Server is not Connected!")
        }
    }

    override fun onConnectionFailure() {
        println("$TAG : onConnectionFailure")
    }

    override fun onReceived(data: Any) {
        println("$TAG : onReceived")
        println("$TAG : $data")

        //val jsonObject = Json.parseToJsonElement("{\"message\":\"one\",\"connectionId\":\"two\",\"requestId\":\"three\"}").jsonObject
        //val jsonObject = Json.parseToJsonElement(data.toString()).jsonObject
        //val ff = jsonObject["message"]?.jsonPrimitive?.content
        //println("sghgc $ff")
    }

    override fun onError(message: String) {
        println("$TAG : onError")
    }

    override fun onReceiveError(message: String) {
        println("$TAG : onReceiveError")
    }

    override fun onSendError(message: String) {
        println("$TAG : onSendError")
    }

    override fun onRetry() {
        println("$TAG : onRetry")
    }

    override fun onSend() {
        println("$TAG : onSend")
    }

    override fun onClose() {
        println("$TAG : onClose")
    }

}