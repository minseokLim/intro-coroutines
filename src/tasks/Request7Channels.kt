package tasks

import contributors.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    coroutineScope {
        val repos = service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .body() ?: emptyList()

        val channel = Channel<List<User>>()
        repos.forEach { repo ->
            launch {
                log("starting loading for ${repo.name}")
                val users = service
                    .getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
                channel.send(users)
            }
        }

        val allUsers = mutableListOf<User>()
        repeat(repos.size) {
            allUsers += channel.receive()
            updateResults(allUsers.aggregate(), it == repos.lastIndex)
        }
    }
}
