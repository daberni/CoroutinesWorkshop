package project

suspend fun loadContributorsProgress(req: RequestData, callback: (List<User>) -> Unit) {
    val service = createGitHubService(req.username, req.password)
    log.info("Loading ${req.org} repos")

    val repos = service.listOrgRepos(req.org).await()
    log.info("${req.org}: loaded ${repos.size} repos")

    var contribs = listOf<User>()
    for (repo in repos) {
        val users = service.listRepoContributors(req.org, repo.name).await()
        log.info("${repo.name}: loaded ${users.size} contributors")

        contribs = (contribs + users).aggregate()
        callback(contribs)
    }

    log.info("Total: ${contribs.size} contributors")
    callback(contribs)
}
