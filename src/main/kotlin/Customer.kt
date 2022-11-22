import kotlinx.serialization.Serializable

@Serializable
//data class Customer(val id:Int, val firstName:String, val lastName:String)
data class Customer(val message:String, val connectionId:String, val requestId:String)


