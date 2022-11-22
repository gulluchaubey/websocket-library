import java.text.ParseException
import java.util.Date
import java.util.UUID


fun typeConverter(args: String): Any {
    return (if(args.toIntOrNull() is Int) {
        args.toIntOrNull()
    } else if(args.toFloatOrNull() is Float) {
        args.toFloatOrNull()
    } else if(args.toDoubleOrNull() is Double) {
        args.toDoubleOrNull()
    } else if(args.toBooleanStrictOrNull() is Boolean) {
        args.toBooleanStrictOrNull()
    } else {
        args
    })!!
}

fun uuidGenerator(): String {
    return UUID.randomUUID().toString()
}
fun getCurrentDateAndTime(): Date {
    //val df: DateFormat = SimpleDateFormat("EEE, d MMM yyyy, HH:mm")
    //return df.format(Calendar.getInstance().time)
    //val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    return Date()

   /* val toyBornTime = "2014-06-18 12:56:50"
    val dateFormat = SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss"
    )*/
}

fun getTimeDifference(oldDate: Date): Boolean {
    try {
        //System.out.println(oldDate)
        val currentDate = Date()
        val diff: Long = currentDate.time - oldDate.time
        /*val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24*/
        return if (oldDate.before(currentDate)) {
            true
        } else if(diff >= 60000) {
            return true
        } else {
            false
        }
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return false
}

