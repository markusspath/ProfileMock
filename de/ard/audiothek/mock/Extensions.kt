package de.ard.audiothek.mock

fun List<Double>.getNextFirst(): Double {
    if (this.isEmpty()) return 0.0
    return this.min() - 1.0
}

fun List<Double>.getNextLast(): Double {
    if (this.isEmpty()) return 0.0
    return this.max() + 1.0
}

fun List<Double>.getOrderAt(position: Int): Double? {
    if (position <= 0) return this.getNextFirst()
    if (position >= this.size) return getNextLast()
    val elements = this.sorted()
    val before = elements[position - 1]
    val after = elements[position]
    val order = (before + after) / 2
    return if (!(order > before) || !(order < after)) null else order
}

fun List<Double>.checkNeedsResetOrders(): Boolean {

    // we need to reset if any of the current values is the same
    if (this.toSet().size < this.size) return true

    // we need to reset if any of the possible sandwiched positions could not be populated
    return List(this.size) { index ->
        getOrderAt(index)
    }.any { it == null }
}