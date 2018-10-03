package project

import kotlinx.coroutines.*
import org.slf4j.*

val log: Logger = LoggerFactory.getLogger("Contributors")

fun List<User>.aggregate(): List<User> = this
        .groupBy { it.login }
        .map { User(it.key, it.value.sumBy { it.contributions }) }
        .sortedByDescending { it.contributions }

private val computation = newFixedThreadPoolContext(2, "computation")

suspend fun List<User>.aggregateSlow(): List<User> = withContext(computation) {
    this@aggregateSlow
            .aggregate()
            .also {
                Thread.sleep(500)
            }
}
