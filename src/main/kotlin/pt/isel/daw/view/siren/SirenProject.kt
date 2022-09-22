package pt.isel.daw.view.siren

import org.eclipse.jetty.http.HttpMethod
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder
import pt.isel.daw.model.IssueState
import pt.isel.daw.model.Project
import pt.isel.daw.view.correctPostAndDelete
import pt.isel.daw.view.toComboItem


fun sirenProjectRes(project: Project, states: Iterable<IssueState>? = null) =
    sirenResponse(sirenProject(project, states?.toComboItem()))

fun sirenProjectsRes(projects: Iterable<Pair<Project, Iterable<IssueState>>>) = sirenResponse(sirenProjects(projects))


fun sirenProjects(projects: Iterable<Pair<Project, Iterable<IssueState>>>): Siren<Array<Project>> {

    val currUriBuilder = ServletUriComponentsBuilder.fromCurrentRequest()
    fun currPath() = currUriBuilder.cloneBuilder()

    val selfUri = currUriBuilder.toUriString()

    return Siren(
        arrayOf("project", "collection"),
        null,
        projects.map { sirenProject(it.first, it.second.toComboItem(), currPath().path("/${it.first.id}")) }
            .toTypedArray(),
        arrayOf(
            makeSirenAction(
                "post-project",
                "Add a Project",
                HttpMethod.POST,
                selfUri,
                arrayOf(
                    makeSirenField("name", "text", "Project Name"),
                    makeSirenField("description", "text", "Project Description"),
                )
            )
        ),
        arrayOf(
            SirenLinks(arrayOf("self"), selfUri),
        ),
        "Project list"
    )
}

fun sirenProject(
    project: Project,
    states: Iterable<ComboItem>? = null,
    baseUri: UriComponentsBuilder = ServletUriComponentsBuilder.fromCurrentRequest()
): Siren<Project> {
    fun currPath() = baseUri.cloneBuilder()
    fun pathToString(str: String) = currPath().path(str).toUriString()

    val selfUri = correctPostAndDelete(baseUri, currPath(), project.id!!)

    val issuesUri = pathToString("/issue")
    val labelsUri = pathToString("/label")
    val statesUri = pathToString("/issuestate")
    val defaultStateUri = pathToString("/defaultstate")
    val transitionsUri = pathToString("/transition")
    val projectsUri = currPath().path("/..").build().normalize().toUriString() // Go back one

    return Siren(
        arrayOf("project", "item"),
        project,
        arrayOf(
            makeSirenSubEntity(
                arrayOf("issue", "collection"),
                arrayOf("#project-issues"),
                issuesUri,
                "Get project's issues"
            ),
            makeSirenSubEntity(
                arrayOf("label", "collection"),
                arrayOf("#project-labels"),
                labelsUri,
                "Get project's labels"
            ),
            makeSirenSubEntity(
                arrayOf("state", "collection"),
                arrayOf("#project-states"),
                statesUri,
                "Get project's issue states"
            ),
            makeSirenSubEntity(
                arrayOf("defaultstate", "item"),
                arrayOf("#project-defaultstate"),
                defaultStateUri,
                "Get project's default state for new issues"
            ),
            makeSirenSubEntity(
                arrayOf("transition", "collection"),
                arrayOf("#project-transitions"),
                transitionsUri,
                "Get project's state transition rules"
            ),
        ),
        arrayOf(
            makeSirenAction(
                "put-project",
                "Alter this Project",
                HttpMethod.PUT,
                selfUri,
                arrayOf(
                    makeSirenField("name", "text", "Project Name", project.name),
                    makeSirenField("description", "text", "Project Description", project.description)
                )
            ),
            makeSirenAction(
                "delete-project",
                "Delete this Project",
                HttpMethod.DELETE,
                selfUri,
                null
            ),
            makeSirenAction(
                "post-issue",
                "Add Issue to Project",
                HttpMethod.POST,
                issuesUri,
                arrayOf(
                    makeSirenField("name", "text", "Issue Name"),
                    makeSirenField("description", "text", "Issue Description"),
                    if (states != null) makeSirenField(
                        "state_id",
                        "number",
                        "Issue State",
                        states,
                        arrayOf("combos", "state")
                    )
                    else makeSirenField("state_id", "number", "Issue State")
                )
            ),
            makeSirenAction(
                "post-label",
                "Add Label to Project",
                HttpMethod.POST,
                labelsUri,
                arrayOf(
                    makeSirenField("name", "text", "Label Name")
                )
            ),
            makeSirenAction(
                "post-state",
                "Add State to Project",
                HttpMethod.POST,
                statesUri,
                arrayOf(
                    makeSirenField("name", "text", "State Name")
                )
            ),
            makeSirenAction(
                "post-transition",
                "Add state transition rule to Project",
                HttpMethod.POST,
                transitionsUri,
                if (states != null) {
                    arrayOf(
                        makeSirenField(
                            "from_state_id",
                            "number",
                            "id referencing existing state",
                            states,
                            arrayOf("combos", "state")
                        ),
                        makeSirenField(
                            "to_state_id",
                            "number",
                            "id referencing existing state",
                            states,
                            arrayOf("combos", "state")
                        )
                    )
                } else {
                    arrayOf(
                        makeSirenField("from_state_id", "number", "id referencing existing state"),
                        makeSirenField("to_state_id", "number", "id referencing existing state")
                    )
                }

            ),
            makeSirenAction(
                "put-default-state",
                "Make a state default for new issues in this project",
                HttpMethod.PUT,
                defaultStateUri,
                arrayOf(
                    if (states != null) {
                        makeSirenField(
                            "id",
                            "number",
                            "id of the referenced Issue State",
                            states,
                            arrayOf("combos", "state")
                        )
                    } else {
                        makeSirenField("id", "number", "id of the referenced Issue State")
                    }
                )
            )
        ),
        arrayOf(
            SirenLinks(arrayOf("self"), selfUri),
            SirenLinks(arrayOf("up"), projectsUri),
        ),
        "Project ${project.id} - ${project.name}"
    )
}