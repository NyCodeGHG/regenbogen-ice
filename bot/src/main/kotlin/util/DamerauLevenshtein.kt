package dev.nycode.regenbogenice.util

import kotlin.math.min

/**
 * Calculates the string distance between source and target strings using
 * the Damerau-Levenshtein algorithm. The distance is case-sensitive.
 *
 * @param source The source String.
 * @param target The target String.
 * @return The distance between source and target strings.
 */
fun calculateDistance(source: CharSequence, target: CharSequence): Int {
    val sourceLength = source.length
    val targetLength = target.length
    if (sourceLength == 0) return targetLength
    if (targetLength == 0) return sourceLength
    val dist = Array(sourceLength + 1) { IntArray(targetLength + 1) }
    for (i in 0 until sourceLength + 1) {
        dist[i][0] = i
    }
    for (j in 0 until targetLength + 1) {
        dist[0][j] = j
    }
    for (i in 1 until sourceLength + 1) {
        for (j in 1 until targetLength + 1) {
            val cost = if (source[i - 1] == target[j - 1]) 0 else 1
            dist[i][j] = min(
                min(dist[i - 1][j] + 1, dist[i][j - 1] + 1),
                dist[i - 1][j - 1] + cost
            )
            if (i > 1 && j > 1 && source[i - 1] == target[j - 2] && source[i - 2] == target[j - 1]) {
                dist[i][j] = min(dist[i][j], dist[i - 2][j - 2] + cost)
            }
        }
    }
    return dist[sourceLength][targetLength]
}

inline fun <T> Collection<T>.minByDistanceOrNull(
    text: CharSequence,
    toString: (T) -> CharSequence = { it.toString() }
) =
    minByOrNull { calculateDistance(text, toString(it)) }
