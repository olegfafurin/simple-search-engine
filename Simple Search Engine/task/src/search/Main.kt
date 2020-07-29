package search

import java.util.Scanner
import java.io.File
import java.lang.NumberFormatException

enum class Strategy(val impl: Iterable<Iterable<Int>>.(a: Int) -> Boolean) {
    ALL(allStrategyImpl),
    ANY(anyStrategyImpl),
    NONE(noneStrategyImpl);
}

object SearchParameters {
    var strategy: Strategy = Strategy.ALL

    fun changeStrategy(newStrategy: Strategy) {
        strategy = newStrategy
    }
}

val allStrategyImpl: Iterable<Iterable<Int>>.(a: Int) -> Boolean = { elem : Int ->
    all { container -> elem in container }
}

val anyStrategyImpl: Iterable<Iterable<Int>>.(a: Int) -> Boolean = { elem : Int ->
    any { container -> elem in container }
}

val noneStrategyImpl: Iterable<Iterable<Int>>.(a: Int) -> Boolean = { elem : Int ->
    !any { container -> elem in container }
}



val indexMap = mutableMapOf<String, MutableList<Int>>()
val recordList = mutableListOf<String>()
val scanner = Scanner(System.`in`)

fun printAllRecords() {
    recordList.forEach {
        println(it)
    }
    println()
}

fun findRecord(strategy: Strategy) {
    println("Enter the query:")
    val queryRaw = scanner.nextLine().toLowerCase()
    val query = queryRaw.split(Regex("\\s+"))
    val partialResults: MutableList<Set<Int>> = mutableListOf()
    for (searchWord in query) {
        partialResults.add(indexMap[searchWord]?.toSet() ?: emptySet())
    }

    fun <T> lambda(partRes: Iterable<Iterable<T>>, rule: Iterable<Iterable<T>>.(Int) -> Boolean): (Int) -> Boolean {
        return { arg -> partRes.rule(arg) }
    }

    val results = recordList.indices.filter(lambda(partialResults, SearchParameters.strategy.impl)).map { recordList[it] }

    if (results.isNotEmpty()) {
        println("Found results:")
        for (record in results) {
            println(record)
        }
    } else println("Not found")
    println()
}

fun insertRecord() {
    val line = scanner.nextLine()
    processIndex(line)
    recordList.add(line)
}

fun processIndex(line: String) {
    val words = line.split(Regex("\\s+")).map { it.toLowerCase() }
    val lineNumber = recordList.size
    for (word in words) {
        indexMap.getOrPut(word, { mutableListOf() }).add(lineNumber)
    }
}

fun showMenu() {
    println("=== MENU ===\n" +
            "1. Find a record\n" +
            "2. Print all records\n" +
            "0. Exit\n")
}

fun readRecordsCli() {
    println("Enter the number of records:")
    val n = scanner.nextLine().toInt()
    println("Enter the records:")
    repeat(n) { insertRecord() }
}

fun readRecordsFile(filename: String) {
    File(filename).forEachLine { line ->
        if (line.isNotBlank()) {
            processIndex(line)
            recordList.add(line)
        }
    }
}

fun getCliArg(args: Array<String>, name: String): String? {
    assert(name.startsWith("--"))
    val pos = args.indexOf(name)
    return if (pos != -1) args[pos + 1] else null
}

fun setSearchStrategy() {
    println("Choose a search strategy: <ALL, ANY, NONE> of the query keywords")
    try {
        val newStrategy = Strategy.valueOf(scanner.nextLine().toUpperCase())
        println("Using $newStrategy:")
        SearchParameters.changeStrategy(newStrategy)
    } catch (e: IllegalArgumentException) {
        SearchParameters.changeStrategy(Strategy.ANY)
        println("Illegal search strategy: using default ANY:")
    }
}

fun main(args: Array<String>) {

    val filename = getCliArg(args, "--data")
    if (filename != null) {
        readRecordsFile(filename)
    } else {
        readRecordsCli()
    }

    while (true) {
        showMenu()
        try {
            when (scanner.nextLine().toInt()) {
                0 -> {
                    println("GOODBYE!")
                    return
                }
                1 -> {
                    setSearchStrategy()
                    findRecord(SearchParameters.strategy)
                }
                2 -> {
                    printAllRecords()
                }
                else -> {
                    println("No such option")
                }
            }
        } catch (e: NumberFormatException) {
            println("Not a valid option")
        }
    }
}


