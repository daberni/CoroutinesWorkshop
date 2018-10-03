package project

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun loadContributorsGather(req: RequestData, callback: suspend (List<User>) -> Unit) {
    coroutineScope {
        val service = createGitHubService(req.username, req.password)
        log.info("Loading ${req.org} repos")

        val repos = service.listOrgRepos(req.org).await()
        log.info("${req.org}: loaded ${repos.size} repos")

        val channel = Channel<List<User>>()

        suspend fun fetch(repos: List<Repo>, channel: Channel<List<User>>) {
            for (repo in repos) {
                launch {
                    val users = service.listRepoContributors(req.org, repo.name).await()
                    log.info("${repo.name}: loaded ${users.size} contributors")
                    channel.send(users)
                }
            }
        }
        fetch(repos, channel)

        var contribs = listOf<User>()
        suspend fun receive(repos: List<Repo>, channel: Channel<List<User>>) {
            repeat(repos.size) {
                val users = channel.receive()
                contribs = (contribs + users).aggregateSlow()
                callback(contribs)
            }
        }
        receive(repos, channel)
        log.info("Total: ${contribs.size} contributors")
    }
}
