package app.dfeverx.ninaiva.utils

fun <T> List<T>.filterNotIn(collection: List<T>): List<T> {
    val set = collection.toSet()
    return filterNot { set.contains(it) }.toList()
}