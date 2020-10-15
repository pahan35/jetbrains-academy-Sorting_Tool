package sorting

import java.util.*
import kotlin.system.exitProcess

enum class DataType(val value: String) {
    LONG("long"),
    LINE("line"),
    WORD("word");

    companion object {
        const val OPTION = "-dataType"

        fun findByValue(value: String?): DataType? {
            if (value == null) return null
            return values().first { it.value == value }
        }
    }
}

enum class SortingType(val value: String) {
    NATURAL("natural"),
    BY_COUNT("byCount");

    companion object {
        const val OPTION = "-sortingType"

        fun findByValue(value: String?): SortingType? {
            if (value == null) return null
            return values().first { it.value == value }
        }
    }
}

fun buildTimesPart(input: List<Any>, timesAppeared: Int): String {
    val percentage = timesAppeared * 100 / input.size
    return "$timesAppeared time(s), $percentage%"
}

fun itemsByWhitespaces(input: List<String>) = input
        .joinToString(" ")
        .trim()
        .replace(Regex("\\s+"), " ")
        .split(" ")

fun <T> stringComparator(a: T?, b: T?): Boolean {
    if (a == null) {
        return false
    }
    if (b == null) {
        return true
    }
    a as String
    b as String
    return a.compareTo(b) < 1
}

fun <T> longComparator(a: T?, b: T?): Boolean {
    if (a == null) {
        return false
    }
    if (b == null) {
        return true
    }
    a as Long
    b as Long
    return a < b
}


fun findByDataType(input: List<String>, dataType: DataType, sortingType: SortingType) {
    if (input.isEmpty()) {
        println("Nothing to process!")
        exitProcess(1)
    }

    val sorted = when (dataType) {
        DataType.LONG -> {
            val numbers = itemsByWhitespaces(input)
                    .map { it.toLong() }
            println("Total numbers: ${numbers.size}.")
            mergeSort(numbers, ::longComparator)
        }
        DataType.LINE -> {
            println("Total lines: ${input.size}.")
            mergeSort(input, ::stringComparator)
        }
        DataType.WORD -> {
            val words = itemsByWhitespaces(input)
            println("Total words: ${words.size}.")
            mergeSort(words, ::stringComparator)
        }
    }
    postProcessSorted(sorted, dataType, sortingType)
}

fun <T> postProcessSorted(sortedList: List<T>, dataType: DataType, sortingType: SortingType) {
    when (sortingType) {
        SortingType.NATURAL -> {
            print("Sorted data:")
            if (dataType == DataType.LINE) {
                println()
                for (line in sortedList) {
                    println(line)
                }
            } else {
                print(" ${sortedList.joinToString(" ")}")
            }
        }
        SortingType.BY_COUNT -> {
            val usageMap = buildMapByCount(sortedList)
            val valueComparator: (T?, T?) -> Boolean = if (dataType == DataType.LONG) ::longComparator else ::stringComparator
            val sortedByCount = mergeSort(usageMap.toList()) { a: Pair<T, Int>?, b: Pair<T, Int>? ->
                if (a == null) {
                    return@mergeSort false
                }
                if (b == null) {
                    return@mergeSort true
                }
                if (a.second == b.second) {
                    return@mergeSort valueComparator(a.first, b.first)
                }
                a.second < b.second
            }
            for ((entry, count) in sortedByCount) {
                println("$entry: ${buildTimesPart(sortedList as List<Any>, count)}")
            }
        }
    }
}

fun <T> buildMapByCount(sortedList: List<T>): Map<T, Int> {
    var current = sortedList[0]
    val usageMap = mutableMapOf(Pair(current, 1))

    for (item in sortedList.subList(1, sortedList.size)) {
        if (item != current) {
            current = item
            usageMap[current] = 0
        }
        usageMap[current] = usageMap[current]!! + 1
    }
    return usageMap
}

fun <T> mergeSort(list: List<T>, comparator: (T?, T?) -> Boolean): List<T> {
    if (list.size == 1) return list
    val middle = list.size / 2
    val left = mergeSort(list.subList(0, middle), comparator)
    val right = mergeSort(list.subList(middle, list.size), comparator)
    var leftPointer = 0
    var rightPointer = 0
    val result = mutableListOf<T>()
    for (i in list.indices) {
        val nextLeft = if (leftPointer < left.size) left[leftPointer] else null
        val nextRight = if (rightPointer < right.size) right[rightPointer] else null

        result.add(if (comparator(nextLeft, nextRight)) {
            leftPointer++
            nextLeft!!
        } else {
            rightPointer++
            nextRight!!
        })
    }
    return result.toList()
}

fun getOptionValue(args: Array<String>, option: String): String? {
    val optionNameIndex = args.indexOf(option)
    val optionValueIndex = optionNameIndex + 1
    if (optionNameIndex == -1 || args.lastIndex < optionValueIndex) {
        return null
    }
    return args[optionValueIndex]
}

fun main(args: Array<String>) {
    val scanner = Scanner(System.`in`)
    val input = mutableListOf<String>()
    while (scanner.hasNextLine()) {
        input.add(scanner.nextLine())
    }
    val dataType = DataType.findByValue(getOptionValue(args, DataType.OPTION)) ?: DataType.WORD
    val sortingType = SortingType.findByValue(getOptionValue(args, SortingType.OPTION)) ?: SortingType.NATURAL
    findByDataType(input, dataType, sortingType)
}
