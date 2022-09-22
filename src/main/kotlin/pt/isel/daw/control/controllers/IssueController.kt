package pt.isel.daw.control.controllers

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.sql.Timestamp
import java.time.LocalDateTime
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.control.db.service.IssueMapper
import pt.isel.daw.control.db.service.IssueStateMapper
import pt.isel.daw.control.db.service.ProjectMapper
import pt.isel.daw.control.db.service.TransitionMapper
import pt.isel.daw.model.Issue
import pt.isel.daw.model.Transition
import pt.isel.daw.view.problem.notFound
import pt.isel.daw.view.problem.problemResponse
import pt.isel.daw.view.siren.sirenIssueRes
import pt.isel.daw.view.siren.sirenIssuesRes
import pt.isel.daw.view.siren.sirenMessageRes

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class IssueInputModel(
    val name: String,
    val description: String?,
    val state_id: Int?,
)

@RestController
@RequestMapping("/daw/project/{project_id}/issue", headers = ["Accept=application/json"])
class IssueController(
    val mapper: IssueMapper,
    val auxProjectMapper: ProjectMapper,
    val auxIssueStateMapper: IssueStateMapper,
    val auxTransitionMapper: TransitionMapper
) {
    private val closed = "closed"

    @GetMapping("")
    fun handlerGetIssues(
        @PathVariable project_id: Int,
    ): ResponseEntity<Any> {
        auxProjectMapper.read(project_id)
            ?: return notFound(
                "Project not found",
                "Since there is no project with id $project_id, there are also no issues"
            )

        val issues: List<Issue> = mapper.readAll(project_id)
        return sirenIssuesRes(issues, auxIssueStateMapper.readAll(project_id))

    }

    @GetMapping("/{issue_id}")
    fun handlerGetIssueById(
        @PathVariable project_id: Int,
        @PathVariable issue_id: Int
    ): ResponseEntity<Any> {
        val issue: Issue = mapper.read(issue_id, project_id)
            ?: return notFound("Issue not found", "Issue $issue_id not found in project $project_id")
        return sirenIssueRes(issue, auxIssueStateMapper.possibleTransitions(issue.state_id, project_id))
    }

    @PostMapping("")
    fun handlerPostIssue(
        @PathVariable project_id: Int,
        @RequestBody issue: IssueInputModel,
    ): ResponseEntity<Any> {

        if (issue.name.length > 32)
            return problemResponse(
                "about:blank", "Exceeds character limit", 400,
                "Name exceeds 32 character limit"
            )

        if (issue.description != null && issue.description.length > 256)
            return problemResponse(
                "about:blank", "Exceeds character limit", 400,
                "Description exceeds 256 character limit"
            )

        val defaultIssueState = auxIssueStateMapper.readDefault(project_id)
            ?: return notFound("Project not Found", "Project does not exist")


        val actualIssue = mapper.create(
            Issue(
                id = null,
                project_id = project_id,
                name = issue.name,
                description = issue.description ?: "none provided",
                creation_date = null,
                close_date = if (defaultIssueState.name == closed) Timestamp.valueOf(LocalDateTime.now()) else null,
                state_id = issue.state_id ?: defaultIssueState.id!!
            )
        )
            ?: return problemResponse("about:blank", "Couldn't Create", 500, "Couldn't Create Issue")

        return sirenIssueRes(actualIssue, auxIssueStateMapper.possibleTransitions(actualIssue.state_id, project_id))
    }

    @PutMapping("/{issue_id}")
    fun handlerPutIssue(
        @PathVariable project_id: Int,
        @PathVariable issue_id: Int,
        @RequestBody issue: IssueInputModel,
    ): ResponseEntity<Any> {

        if (issue.name.length > 32)
            return problemResponse(
                "about:blank", "Exceeds character limit", 400,
                "Name exceeds 32 character limit"
            )

        if (issue.description != null && issue.description.length > 256)
            return problemResponse(
                "about:blank", "Exceeds character limit", 400,
                "Description exceeds 256 character limit"
            )

        val previousIssue = mapper.read(issue_id, project_id)
            ?: return notFound("Issue not found", "Could not find Issue")

        var transition: Transition? = null
        if (issue.state_id != null && issue.state_id != previousIssue.state_id)
            transition = auxTransitionMapper.readByState(previousIssue.state_id, issue.state_id)
                ?: return problemResponse(
                    "about:blank", "Illegal Transition", 403,
                    "This transition is not listed under this project"
                )

        val actualIssue = mapper.update(
            Issue(
                id = issue_id,
                project_id = project_id,
                name = issue.name,
                description = issue.description ?: "none provided",
                creation_date = null,
                close_date = if (transition?.toState?.name == closed) Timestamp.valueOf(LocalDateTime.now()) else null,
                state_id = issue.state_id ?: previousIssue.state_id
            )
        )
            ?: return problemResponse("about:blank", "Couldn't Update", 500, "Couldn't Update Issue")

        return sirenIssueRes(actualIssue, auxIssueStateMapper.possibleTransitions(actualIssue.state_id, project_id))
    }

    @DeleteMapping("/{issue_id}")
    fun handlerDeleteIssue(
        @PathVariable project_id: Int,
        @PathVariable issue_id: Int,
    ): ResponseEntity<Any> {

        return if (mapper.delete(issue_id, project_id) > 0)
            return sirenMessageRes("Issue $issue_id deleted successfully", "issue")
        else notFound("Issue not Found", "Couldn't delete Issue $issue_id")
    }
}