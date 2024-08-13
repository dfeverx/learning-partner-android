package app.dfeverx.ninaiva.utils

import java.util.Calendar
import kotlin.math.abs


fun Long.relativeTime(): String {
    if (this.toInt() ==0){
        return ""
    }
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this

    // Extract date-only components
    val timestampYear = calendar[Calendar.YEAR]
    val timestampMonth = calendar[Calendar.MONTH]
    val timestampDay = calendar[Calendar.DAY_OF_MONTH]

    val nowCalendar = Calendar.getInstance()
    val nowYear = nowCalendar[Calendar.YEAR]
    val nowMonth = nowCalendar[Calendar.MONTH]
    val nowDay = nowCalendar[Calendar.DAY_OF_MONTH]

    // Calculate differences
    val yearDiff = nowYear - timestampYear
    val monthDiff = nowMonth - timestampMonth
    val dayDiff = nowDay - timestampDay


    return when {
        abs(yearDiff) > 0 -> "${abs(yearDiff)} ${if (abs(yearDiff) > 1) "years" else "year"} ${if (yearDiff > 0) "ago" else "from now"}"
        abs(monthDiff) > 0 -> "${abs(monthDiff)} ${if (abs(monthDiff) > 1) "months" else "month"} ${if (monthDiff > 0) "ago" else "from now"}"
        abs(dayDiff) > 0 -> "${abs(dayDiff)} ${if (abs(dayDiff) > 1) "days" else "day"} ${if (dayDiff > 0) "ago" else "from now"}"
        else -> "Today"
    }
}


fun getTimePeriod(timestampInMillis: Long): TimePeriod {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestampInMillis

    // Extract date-only components
    val timestampYear = calendar[Calendar.YEAR]
    val timestampMonth = calendar[Calendar.MONTH]
    val timestampDay = calendar[Calendar.DAY_OF_MONTH]

    val nowCalendar = Calendar.getInstance()
    val nowYear = nowCalendar[Calendar.YEAR]
    val nowMonth = nowCalendar[Calendar.MONTH]
    val nowDay = nowCalendar[Calendar.DAY_OF_MONTH]

    // Calculate differences
    val yearDiff = nowYear - timestampYear
    val monthDiff = nowMonth - timestampMonth
    val dayDiff = nowDay - timestampDay


    return when {
        yearDiff != 0 || monthDiff != 0 || dayDiff != 0 -> if (timestampInMillis < System.currentTimeMillis()) TimePeriod.PAST else TimePeriod.FUTURE
        else -> TimePeriod.TODAY
    }


}

enum class TimePeriod {
    PAST, FUTURE, TODAY
}

