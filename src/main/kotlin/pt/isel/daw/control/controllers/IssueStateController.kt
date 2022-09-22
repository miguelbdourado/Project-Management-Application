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
import pt.isel.daw.control.db.service.ProjectMapper
import pt.isel.daw.model.IssueState
import pt.isel.daw.view.problem.notFound
import pt.isel.daw.view.problem.problemResponse
import pt.isel.daw.view.siren.sirenMessageRes
import pt.isel.daw.view.siren.sirenStateRes
import pt.isel.daw.view.siren.sirenStatesRes

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class DefaultStateInputModel(
    val id: Int,
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class IssueStateInputModel(
    val name: String,
)


@RestController
@RequestMapping("/daw/project/{project_id}/defaultstate", headers = ["Accept=application/json"])
class DefaultStateController(val mapper: IssueStateMapper) {

    @GetMapping("")
    fun handlerGetDefaultState(
        @PathVariable project_id: Int,
    ): ResponseEntity<Any> {
        val issueState: IssueState = mapper.readDefault(project_id)
            ?: return notFound(
                "Default Issue State not found",
                "Default Issue State not found on project $project_id, meaning that project likely doesn't exist"
            )
        return sirenStateRes(issueState, mapper.readAll(project_id), true)
    }

    @PutMapping("")
    fun handlerPutDefaultState(
        @PathVariable project_id: Int,
        @RequestBody issueState: DefaultStateInputModel
    ): ResponseEntity<Any> {

        val defaultState: IssueState = mapper.updateDefault(
            IssueState(
                id = issueState.id,
                name = null,
                project_id = project_id
            )
        ) ?: return notFound("Couldn't Update", "No such project?")

        return sirenStateRes(defaultState, mapper.readAll(project_id), true)
    }
}

@RestController
@RequestMapping("/daw/project/{project_id}/issuestate", headers = ["Accept=application/json"])
class IssueStateController(val mapper: IssueStateMapper, val auxProjectMapper: ProjectMapper) {

    private val closed = "closed"
    private val archived = "archived"

    @GetMapping("")
    fun handlerGetIssueStates(
        @PathVariable project_id: Int,
    ): ResponseEntity<Any> {
        auxProjectMapper.read(project_id)
            ?: return notFound(
                "Project not found",
                "Since there is no project with id $project_id, there are also no states"
            )
        val issueStates: List<IssueState> = mapper.readAll(project_id)
        return sirenStatesRes(issueStates)
    }

    @GetMapping("/{issue_state_id}")
    fun handlerGetIssueById(
        @PathVariable project_id: Int,
        @PathVariable issue_state_id: Int
    ): ResponseEntity<Any> {
        val issueState: IssueState = mapper.read(issue_state_id, project_id)
            ?: return notFound("State not found", "IssueState $issue_state_id not found in project $project_id")
        return sirenStateRes(issueState, mapper.readAll(project_id))
    }

    @PostMapping("")
    fun handlerPostIssue(
        @PathVariable project_id: Int,
        @RequestBody issueState: IssueStateInputModel
    ): ResponseEntity<Any> {

        if (issueState.name.length > 32)
            return problemResponse(
                "about:blank", "Exceeds character limit", 400,
                "Name exceeds 32 character limit"
            )

        val actualIssue = mapper.create(
            IssueState(
                id = null,
                project_id = project_id,
                name = issueState.name,
            )
        ) ?: return problemResponse(
            "about:blank", "Couldn't Create", 500,
            "Couldn't Create IssueState"
        )

        return sirenStateRes(actualIssue, mapper.readAll(project_id))
    }

    @PutMapping("/{issue_state_id}")
    fun handlerPutIssue(
        @PathVariable project_id: Int,
        @PathVariable issue_state_id: Int,
        @RequestBody issueState: IssueStateInputModel,
    ): ResponseEntity<Any> {

        if (issueState.name.length > 32)
            return problemResponse("about:blank", "Exceeds character limit", 400, "Name exceeds 32 character limit")

        val name = mapper.read(issue_state_id, project_id)?.name
            ?: return notFound("State not found", "IssueState $issue_state_id not found in project $project_id")

        when (name) {
            closed, archived ->
                return problemResponse(
                    "about:blank", "Cannot update default state", 403,
                    "State $name is default and protected"
                )
            else -> {

                val actualIssueState = mapper.update(
                    IssueState(
                        id = issue_state_id,
                        project_id = project_id,
                        name = issueState.name,
                    )
                ) ?: return problemResponse("about:blank", "Couldn't Update", 500, "Couldn't Update IssueState")

                return sirenStateRes(actualIssueState, mapper.readAll(project_id))
            }
        }
    }

    @DeleteMapping("/{issue_state_id}")
    fun handlerDeleteIssue(
        @PathVariable project_id: Int,
        @PathVariable issue_state_id: Int
    ): ResponseEntity<Any> {
        val name = mapper.read(issue_state_id, project_id)?.name
            ?: return notFound(
                "State not found",
                "IssueState $issue_state_id not found in project $project_id"
            )
        when (name) {
            closed, archived ->
                return problemResponse(
                    "about:blank", "Cannot delete default state", 403,
                    "State $name is default and protected"
                )
            else -> return if (mapper.delete(issue_state_id, project_id) > 0)
                return sirenMessageRes("State $name deleted successfully", "state")
            else problemResponse(
                "about:blank", "Couldn't delete", 500,
                "IssueState $issue_state_id could not be deleted from project $project_id"
            )
        }
    }


}