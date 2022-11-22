package dataUtils

data class Config(var maxFrameSize: Long = Long.MAX_VALUE,
                  var pingInterval: Long = 60000,
                  var connectTimeoutMillis: Long = 60000,
                  var socketTimeoutMillis: Long = 60000,
                  var maxRetries: Int = 3,
                  var retryInterval: Long = 1000)



