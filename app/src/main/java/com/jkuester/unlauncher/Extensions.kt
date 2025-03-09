package com.jkuester.unlauncher

fun <T> List<T>.swap(fromIndex: Int, toIndex: Int): List<T> {
    if (fromIndex !in indices || toIndex !in indices) {
        throw IndexOutOfBoundsException("Index out of bounds")
    }
    if (fromIndex == toIndex) {
        return this.toList()
    }

    val mutableList = this.toMutableList()
    val temp = mutableList[fromIndex]
    mutableList[fromIndex] = mutableList[toIndex]
    mutableList[toIndex] = temp
    return mutableList.toList()
}
