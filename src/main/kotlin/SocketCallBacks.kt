interface SocketCallBacks {
    fun onConnecting()
    fun onConnected()
    fun onConnectionFailure()
    fun onReceived(data: Any)
    fun onError(message:String)
    fun onReceiveError(message:String)
    fun onSendError(message:String)
    fun onRetry()
    fun onSend()
    fun onClose()
}