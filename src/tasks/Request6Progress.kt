package tasks

import contributors.*

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .body() ?: emptyList()

    val users = mutableListOf<User>()
    repos.forEachIndexed { index, repo ->
        users += service
            .getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()

        updateResults(users.aggregate(), index == repos.lastIndex)
    }
}
