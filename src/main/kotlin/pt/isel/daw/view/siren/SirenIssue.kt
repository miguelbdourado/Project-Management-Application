package pt.isel.daw.view.siren

import org.eclipse.jetty.http.HttpMethod
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder
import pt.isel.daw.model.Issue
import pt.isel.daw.model.IssueState
import pt.isel.daw.view.correctPostAndDelete
import pt.isel.daw.view.toComboItem

fun sirenIssueRes(issue: Issue, states: Iterable<IssueState>?) = sirenResponse(sirenIssue(issue, states?.toComboItem()))

fun sirenIssuesRes(issues: Iterable<Issue>, states: Iterable<IssueState>?) =
    sirenResponse(sirenIssues(issues, states?.toComboItem()))


fun sirenIssues(issues: Iterable<Issue>, states: Iterable<ComboItem>? = null): Siren<Array<Issue>> {

    val currUriBuilder = ServletUriComponentsBuilder.fromCurrentRequest()
    fun currPath() = currUriBuilder.cloneBuilder()

    val selfUri = currUriBuilder.toUriString()
    val projectUri = currPath().path("/..").build().normalize().toUriString() // Go back one

    return Siren(
        arrayOf("issue", "collection"),
        null,
        issues.map { sirenIssue(it, states, currPath().path("/${it.id}")) }.toTypedArray(),
        arrayOf(
            makeSirenAction(
                "post-issue",
                "Add an Issue",
                HttpMethod.POST,
                selfUri,
                arrayOf(
                    makeSirenField("name", "text", "Name of the issue"),
                    makeSirenField("description", "text", "Description of the issue"),
                    if (states != null) makeSirenField(
                        "state_id",
                        "number",
                        "Issue State",
                        states,
                        arrayOf("combos", "state")
                    )
                    else makeSirenField("state_id", "number", "Issue State")
                )
            )
        ),
        arrayOf(
            SirenLinks(arrayOf("self"), selfUri),
            SirenLinks(arrayOf("up"), projectUri),
        ),
        "Issue list for project ${projectUri.substringAfterLast("/")}"
    )
}

fun sirenIssue(
    issue: Issue,
    states: Iterable<ComboItem>? = null,
    baseUri: UriComponentsBuilder = ServletUriComponentsBuilder.fromCurrentRequest()
): Siren<Issue> {
    fun currPath() = baseUri.cloneBuilder()

    val selfUri = correctPostAndDelete(baseUri, currPath(), issue.id!!)

    val commentUri = currPath().path("/comment").toUriString()
    val issueLabelsUri = currPath().path("/label").toUriString()
    val issueStateUri = currPath().path("/../../issuestate/${issue.state_id}").build().normalize().toUriString()
    val issuesUri = currPath().path("/..").build().normalize().toUriString() // Go back one

    return Siren(
        arrayOf("issue", "item"),
        issue,
        arrayOf(
            makeSirenSubEntity(
                arrayOf("comment", "collection"),
                arrayOf("#issue-comments"),
                commentUri,
                "Get this issue's comments"
            ),
            makeSirenSubEntity(
                arrayOf("label", "collection"),
                arrayOf("#issue-labels"),
                issueLabelsUri,
                "Get this issue's labels"
            ),
            makeSirenSubEntity(
                arrayOf("state", "item"),
                arrayOf("#issue-state"),
                issueStateUri,
                "Get this issue's state"
            )
        ),
        arrayOf(
            makeSirenAction(
                "post-comment",
                "Add Comment to Issue",
                HttpMethod.POST,
                commentUri,
                arrayOf(
                    makeSirenField("content", "text", "Content of the comment"),
                )
            ),
            makeSirenAction(
                "put-issue",
                "Alter this Issue",
                HttpMethod.PUT,
                selfUri,
                arrayOf(
                    makeSirenField("name", "text", "Name of the issue", issue.name),
                    makeSirenField("description", "text", "Description of the issue", issue.description),
                    if (states != null) {
                        makeSirenField(
                            "state_id",
                            "number",
                            "Issue state's reference",
                            states,
                            arrayOf("combos", "state")
                        )
                    } else {
                        makeSirenField("state_id", "number", "Issue state's reference", issue.state_id)
                    }

                )
            ),
            makeSirenAction(
                "delete-issue",
                "Delete this Issue",
                HttpMethod.DELETE,
                selfUri,
                null
            ),
            makeSirenAction(
                "put-state",
                "Update this Issue's State name",
                HttpMethod.PUT,
                issueStateUri,
                arrayOf(
                    makeSirenField("name", "text", "name of the Issue State")
                )
            )
        ),
        arrayOf(
            SirenLinks(arrayOf("self"), selfUri),
            SirenLinks(arrayOf("up"), issuesUri),
        ),
        "Issue ${issue.id} - ${issue.name}"
    )
}