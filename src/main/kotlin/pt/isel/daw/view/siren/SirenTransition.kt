package pt.isel.daw.view.siren

import org.eclipse.jetty.http.HttpMethod
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder
import pt.isel.daw.model.IssueState
import pt.isel.daw.model.Transition
import pt.isel.daw.view.correctPostAndDelete
import pt.isel.daw.view.toComboItem

fun sirenTransitionRes(transition: Transition, states: Iterable<IssueState>? = null) =
    sirenResponse(sirenTransition(transition, states?.toComboItem()))

fun sirenTransitionsRes(transitions: Iterable<Transition>, states: Iterable<IssueState>? = null) =
    sirenResponse(sirenTransitions(transitions, states?.toComboItem()))


fun sirenTransitions(transitions: Iterable<Transition>, states: Iterable<ComboItem>? = null): Siren<Array<Transition>> {

    val currUriBuilder = ServletUriComponentsBuilder.fromCurrentRequest()
    fun currPath() = currUriBuilder.cloneBuilder()

    val selfUri = currUriBuilder.toUriString()
    val projectUri = currPath().path("/..").build().normalize().toUriString() // Go back one

    return Siren(
        arrayOf("transition", "collection"),
        null,
        transitions.map { sirenTransition(it, states, currPath().path("/${it.id}")) }.toTypedArray(),
        arrayOf(
            makeSirenAction(
                "post-transition",
                "Add a Transition",
                HttpMethod.POST,
                selfUri,
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
            )
        ),
        arrayOf(
            SirenLinks(arrayOf("self"), selfUri),
            SirenLinks(arrayOf("up"), projectUri),
        ),
        "Transition list for project ${projectUri.substringAfterLast("/")}"
    )
}

fun sirenTransition(
    transition: Transition,
    states: Iterable<ComboItem>? = null,
    baseUri: UriComponentsBuilder = ServletUriComponentsBuilder.fromCurrentRequest()
): Siren<Transition> {
    fun currPath() = baseUri.cloneBuilder()

    val selfUri = correctPostAndDelete(baseUri, currPath(), transition.id!!)

    val helperUri = currPath().path("/..")
    val transitionsUri = helperUri.cloneBuilder().build().normalize().toUriString() // Go back one
    helperUri.path("/../issuestate")
    val fromStateUri = helperUri.cloneBuilder().path("/${transition.fromState.id}").build().normalize().toUriString()
    val toStateUri = helperUri.path("/${transition.toState.id}").build().normalize().toUriString()

    return Siren(
        arrayOf("transition", "item"),
        transition,
        arrayOf(
            makeSirenSubEntity(
                arrayOf("state", "item"),
                arrayOf("#transition-state"),
                fromStateUri,
                "Get this transition rule's initial state"
            ),
            makeSirenSubEntity(
                arrayOf("state", "item"),
                arrayOf("#transition-state"),
                toStateUri,
                "Get this transition rule's final state"
            ),
        ),
        arrayOf(
            makeSirenAction(
                "change-transition",
                "Change Transition",
                HttpMethod.PUT,
                selfUri,
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
                "delete-transition",
                "Delete Transition",
                HttpMethod.DELETE,
                selfUri,
                null
            )
        ),
        arrayOf(
            SirenLinks(arrayOf("self"), selfUri),
            SirenLinks(arrayOf("up"), transitionsUri),
        ),
        "Transition ${transition.id} - ${transition.fromState.name} -> ${transition.toState.name}"
    )
}
