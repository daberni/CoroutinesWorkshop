package part2blocking.exrecsice

import kotlin.concurrent.*

fun main(args: Array<String>) {
    val jobs = List(100_000) {
        thread {
            Thread.sleep(5000)
            print(".")
        }
    }
    jobs.forEach { it.join() }
}