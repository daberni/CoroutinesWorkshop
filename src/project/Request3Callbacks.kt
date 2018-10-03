package project

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun loadContributorsCallbacks(req: RequestData, callback: (List<User>) -> Unit) {
    val service = createGitHubService(req.username, req.password)
    log.info("Loading ${req.org} repos")

    service.listOrgRepos(req.org).responseCallback { repos ->
        log.info("${req.org}: loaded ${repos.size} repos")
        val contribs = mutableListOf<User>()

        fun requestRepo(i: Int) {
            if (i < repos.size) {
                val repo = repos[i]
                service.listRepoContributors(req.org, repo.name).responseCallback { users ->
                    log.info("${repo.name}: loaded ${users.size} contributors")
                    contribs.addAll(users)

                    requestRepo(i + 1)
                }
            } else {
                log.info("Total: ${contribs.size} contributors")
                callback(contribs.aggregate())
            }
        }

        requestRepo(0)
    }
}

inline fun <T> Call<T>.responseCallback(crossinline callback: (T) -> Unit) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            checkResponse(response)
            callback(response.body()!!)
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            log.error("Call failed", t)
        }
    })
}
