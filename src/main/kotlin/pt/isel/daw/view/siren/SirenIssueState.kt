package pt.isel.daw.view.siren

import org.eclipse.jetty.http.HttpMethod
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder
import pt.isel.daw.model.IssueState
import pt.isel.daw.view.correctPostAndDelete
import pt.isel.daw.view.toComboItem

fun sirenStateRes(state: IssueState, altStates: Iterable<IssueState>?, default: Boolean = false) =
    sirenResponse(
        if (default) sirenDefaultState(state, altStates?.toComboItem()) else sirenIssueState(
            state,
            altStates?.toComboItem()
        )
    )

fun sirenStatesRes(states: Iterable<IssueState>) = sirenResponse(sirenIssueStates(states))


fun sirenIssueStates(states: Iterable<IssueState>): Siren<Array<IssueState>> {

    val currUriBuilder = ServletUriComponentsBuilder.fromCurrentRequest()
    fun currPath() = currUriBuilder.cloneBuilder()

    val selfUri = currUriBuilder.toUriString()
    val projectUri = currPath().path("/..").build().normalize().toUriString() // Go back one

    return Siren(
        arrayOf("state", "collection"),
        null,
        states.map { sirenIssueState(it, states.toComboItem(), currPath().path("/${it.id}")) }.toTypedArray(),
        arrayOf(
            makeSirenAction(
                "post-state",
                "Add a State",
                HttpMethod.POST,
                selfUri,
                arrayOf(
                    makeSirenField("name", "text", "Name of the state")
                )
            )
        ),
        arrayOf(
            SirenLinks(arrayOf("self"), selfUri),
            SirenLinks(arrayOf("up"), projectUri),
        ),
        "State list for project ${projectUri.substringAfterLast("/")}"
    )
}

fun sirenIssueState(
    issueState: IssueState,
    states: Iterable<ComboItem>? = null,
    baseUri: UriComponentsBuilder = ServletUriComponentsBuilder.fromCurrentRequest()
): Siren<IssueState> {
    fun currPath() = baseUri.cloneBuilder()

    val selfUri = correctPostAndDelete(baseUri, currPath(), issueState.id!!)

    val issueStatesUri = currPath().path("/..").build().normalize().toUriString() // Go back one
    val defaultStateUri = currPath().path("/../../defaultstate").build().normalize().toUriString()

    return Siren(
        arrayOf("state", "item"),
        issueState,
        null,
        arrayOf(
            makeSirenAction(
                "delete-state",
                "Delete Issue State",
                HttpMethod.DELETE,
                selfUri,
                null
            ),
            makeSirenAction(
                "put-state",
                "Change Issue State",
                HttpMethod.PUT,
                selfUri,
                arrayOf(
                    makeSirenField("name", "text", "name of the Issue State", issueState.name)
                )
            ),
            makeSirenAction(
                "put-default-state",
                "Make this state default for new issues",
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
                        makeSirenField("id", "number", "id of the referenced Issue State", issueState.id)
                    }

                )
            )
        ),
        arrayOf(
            SirenLinks(arrayOf("self"), selfUri),
            SirenLinks(arrayOf("up"), issueStatesUri),
        ),
        "IssueState ${issueState.id} - ${issueState.name}"
    )
}

fun sirenDefaultState(
    defaultState: IssueState,
    states: Iterable<ComboItem>? = null,
    baseUri: UriComponentsBuilder = ServletUriComponentsBuilder.fromCurrentRequest()
): Siren<IssueState> {
    fun currPath() = baseUri.cloneBuilder()

    val selfUri = baseUri.toUriString()
    val projectUri = currPath().path("/..").build().normalize().toUriString() // Go back one
    val stateUri = currPath().path("/../issuestate/${defaultState.id}").build().normalize().toUriString()

    return Siren(
        arrayOf("defaultstate", "state", "item"),
        defaultState,
        arrayOf(
            makeSirenSubEntity(
                arrayOf("state", "item"),
                arrayOf("#defaultstate-state"),
                stateUri,
                "Obtain Issue State"
            )
        ),
        arrayOf(
            makeSirenAction(
                "put-state",
                "Change Issue State's name",
                HttpMethod.PUT,
                stateUri,
                arrayOf(
                    makeSirenField("name", "text", "name of the Issue State", defaultState.name)
                )
            ),
            makeSirenAction(
                "put-default-state",
                "Change default state for new issues",
                HttpMethod.PUT,
                selfUri,
                arrayOf(
                    if (states != null) {
                        makeSirenField(
                            "id",
                            "number",
                            "id of the referenced Issue State",
                            states,
                            arrayOf("combo", "state")
                        )
                    } else {
                        makeSirenField("id", "number", "id of the referenced Issue State", defaultState.id)
                    }

                )
            )
        ),
        arrayOf(
            SirenLinks(arrayOf("self"), selfUri),
            SirenLinks(arrayOf("up"), projectUri),
        ),
        "Default state for new issues: ${defaultState.id} - ${defaultState.name}"
    )
}