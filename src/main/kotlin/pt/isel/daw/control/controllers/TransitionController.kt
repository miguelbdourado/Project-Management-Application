package pt.isel.daw.control.controllers

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.control.db.service.IssueStateMapper
import pt.isel.daw.control.db.service.TransitionMapper
import pt.isel.daw.model.IssueState
import pt.isel.daw.model.Transition
import pt.isel.daw.view.problem.notFound
import pt.isel.daw.view.problem.problemResponse
import pt.isel.daw.view.siren.sirenMessageRes
import pt.isel.daw.view.siren.sirenTransitionRes
import pt.isel.daw.view.siren.sirenTransitionsRes

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class TransitionInputModel(
    val fromStateId: Int,
    val toStateId: Int,
)

@RestController
@RequestMapping("/daw/project/{project_id}/transition", headers = ["Accept=application/json"])
class TransitionController(val mapper: TransitionMapper, val stateMapper: IssueStateMapper) {

    private val closed = "closed"
    private val archived = "archived"

    @GetMapping("")
    fun handlerGetTransitions(
        @PathVariable project_id: Int,
    ): ResponseEntity<Any> {
        val transitions = mapper.readAll(project_id)
        if (transitions.isEmpty()) return notFound(
            "Transition not found",
            "No transitions found in issue $project_id, this usually means there is no such project"
        )

        val states = stateMapper.readAll(project_id)

        return sirenTransitionsRes(transitions, states)
    }

    @GetMapping("/{transition_id}")
    fun handlerGetTransitionById(
        @PathVariable project_id: Int,
        @PathVariable transition_id: Int
    ): ResponseEntity<Any> {
        val transition: Transition = mapper.read(transition_id, project_id)
            ?: return notFound("Transition not found", "Transition $transition_id not found in issue $project_id")
        val states = stateMapper.readAll(project_id)
        return sirenTransitionRes(transition, states)
    }

    @PostMapping("")
    fun handlerPostTransition(
        @PathVariable project_id: Int,
        @RequestBody transition: TransitionInputModel
    ): ResponseEntity<Any> {

        if (transition.fromStateId <= 0 || transition.toStateId <= 0)
            return problemResponse(
                "about:blank", "Invalid ID", 400,
                "State IDs may never be 0 or below"
            )

        val actualTransition = mapper.create(
            Transition(
                id = null,
                fromState = IssueState(transition.fromStateId, project_id, null),
                toState = IssueState(transition.toStateId, project_id, null)
            ), project_id
        )
            ?: return problemResponse(
                "about:blank", "Couldn't Create Transition", 400,
                "Request inconsistent, ensure that states " +
                        "${transition.fromStateId} and ${transition.toStateId} both belong to Project $project_id"
            )

        val states = stateMapper.readAll(project_id)
        return sirenTransitionRes(actualTransition, states)
    }

    @PutMapping("/{transition_id}")
    fun handlerPutTransition(
        @PathVariable project_id: Int,
        @PathVariable transition_id: Int,
        @RequestBody transition: TransitionInputModel
    ): ResponseEntity<Any> {

        val readTransition = mapper.read(transition_id, project_id)
            ?: return notFound("This transition doesn't exist", "No transition with id $transition_id")

        if (readTransition.fromState.name == closed && readTransition.toState.name == archived)
            return problemResponse(
                "about:blank", "Cannot update default transition", 403,
                "Transition $transition_id is default and protected"
            )


        val actualTransition = mapper.update(
            Transition(
                id = transition_id,
                fromState = IssueState(transition.fromStateId, project_id, null),
                toState = IssueState(transition.toStateId, project_id, null)
            )
        )
            ?: return problemResponse(
                "about:blank", "Couldn't Update Transition", 400,
                "Request inconsistent, ensure that states " +
                        "${transition.fromStateId} and ${transition.toStateId} both belong to Project $project_id"
            )

        val states = stateMapper.readAll(project_id)
        return sirenTransitionRes(actualTransition, states)
    }

    @DeleteMapping("/{transition_id}")
    fun handlerDeleteTransition(
        @PathVariable project_id: Int,
        @PathVariable transition_id: Int
    ): ResponseEntity<Any> {

        val readTransition = mapper.read(transition_id, project_id)
            ?: return notFound(
                "This transition doesn't exist",
                "No transition with id $transition_id in project $project_id"
            )

        if (readTransition.fromState.name == closed && readTransition.toState.name == archived)
            return problemResponse(
                "about:blank", "Cannot delete default transition", 403,
                "Transition $transition_id is default and protected"
            )

        return if (mapper.delete(transition_id, project_id) > 0)
            return sirenMessageRes("Transition $transition_id deleted successfully", "transition")
        else notFound("Transition not Found", "Couldn't delete Transition $transition_id")
    }
}