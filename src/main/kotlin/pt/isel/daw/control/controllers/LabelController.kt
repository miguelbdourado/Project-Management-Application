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
import pt.isel.daw.control.db.service.IssueMapper
import pt.isel.daw.control.db.service.LabelMapper
import pt.isel.daw.control.db.service.ProjectMapper
import pt.isel.daw.model.Label
import pt.isel.daw.view.problem.notFound
import pt.isel.daw.view.problem.problemResponse
import pt.isel.daw.view.siren.sirenLabelRes
import pt.isel.daw.view.siren.sirenLabelsRes
import pt.isel.daw.view.siren.sirenMessageRes

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class IssueLabelInputModel(
    val id: Int,
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class LabelInputModel(
    val name: String,
)

@RestController
@RequestMapping("/daw/project/{project_id}/label", headers = ["Accept=application/json"])
class LabelProjectController(val mapper: LabelMapper, val auxProjectMapper: ProjectMapper) {

    @GetMapping("")
    fun handlerGetLabels(
        @PathVariable project_id: Int,
    ): ResponseEntity<Any> {
        auxProjectMapper.read(project_id)
            ?: return notFound(
                "Project not found",
                "Since there is no project with id $project_id, there are also no labels"
            )
        val labels: List<Label> = mapper.readAll(project_id)
        return sirenLabelsRes(labels)
    }

    @GetMapping("/{label_id}")
    fun handlerGetLabelById(
        @PathVariable project_id: Int,
        @PathVariable label_id: Int,
    ): ResponseEntity<Any> {
        val label: Label? = mapper.read(label_id, project_id)
        if (label != null) return sirenLabelRes(label)
        return notFound("Label not found", "Label $label_id not found in project $project_id")
    }

    @PostMapping("")
    fun handlerPostLabel(
        @PathVariable project_id: Int,
        @RequestBody label: LabelInputModel
    ): ResponseEntity<Any> {

        if (label.name.length > 32)
            return problemResponse(
                "about:blank", "Exceeds Character limit", 400,
                "Name exceeds maximum size of 32 characters"
            )

        val actualLabel = mapper.create(
            Label(
                id = null,
                name = label.name,
                project_id = project_id
            )
        )
        if (actualLabel != null) return sirenLabelRes(actualLabel)
        return problemResponse("about:blank", "Couldn't Create", 500, "Couldn't Create Label")
    }

    @PutMapping("/{label_id}")
    fun handlerPutLabel(
        @PathVariable project_id: Int,
        @PathVariable label_id: Int,
        @RequestBody label: LabelInputModel,
    ): ResponseEntity<Any> {

        if (label.name.length > 32)
            return problemResponse(
                "about:blank", "Exceeds Character limit", 400,
                "Name exceeds maximum size of 32 characters"
            )

        val actualLabel = mapper.update(
            Label(
                id = label_id,
                name = label.name,
                project_id = project_id
            )
        )
        if (actualLabel != null) return sirenLabelRes(actualLabel)
        return notFound("Couldn't Update", "Label $label_id not found in project $project_id")
    }

    @DeleteMapping("/{label_id}")
    fun handlerDeleteLabel(
        @PathVariable project_id: Int,
        @PathVariable label_id: Int,
    ): ResponseEntity<Any> {

        return if (mapper.delete(label_id, project_id) > 0)
            sirenMessageRes("Label $label_id deleted from $project_id successfully", "label")
        else notFound("Label not Found", "Couldn't delete Label $label_id from project id $project_id")
    }
}


@RestController
@RequestMapping("/daw/project/{project_id}/issue/{issue_id}/label", headers = ["Accept=application/json"])
class LabelIssueController(val mapper: LabelMapper, val auxIssueMapper: IssueMapper) {

    @GetMapping("")
    fun handlerGetIssueLabels(
        @PathVariable project_id: Int,
        @PathVariable issue_id: Int,
    ): ResponseEntity<Any> {
        auxIssueMapper.read(issue_id, project_id)
            ?: return notFound(
                "Issue not found",
                "Since there is no issue with id $issue_id, there are also no labels"
            )
        val labels: List<Label> = mapper.readAll(project_id, issue_id)
        return sirenLabelsRes(labels, mapper.readAll(project_id).minus(labels))

    }

    @GetMapping("/{label_id}")
    fun handlerGetIssueLabelById(
        @PathVariable project_id: Int,
        @PathVariable issue_id: Int,
        @PathVariable label_id: Int,
    ): ResponseEntity<Any> {
        val label: Label? = mapper.read(label_id, project_id, issue_id)
        if (label != null) return sirenLabelRes(label, true)
        return notFound(
            "Label not found",
            "Label $label_id not found in project $project_id, issue $issue_id"
        )
    }

    @PostMapping("")
    fun handlerPostIssueLabel(
        @PathVariable project_id: Int,
        @PathVariable issue_id: Int,
        @RequestBody label: IssueLabelInputModel
    ): ResponseEntity<Any> {

        val actualLabel: Label = mapper.associateLabelWithIssue(label.id, issue_id, project_id)
            ?: return problemResponse(
                "about:blank", "Couldn't Create", 400, "Request inconsistent, " +
                        "ensure that Issue $issue_id and Label ${label.id} both belong to Project $project_id"
            )

        return sirenLabelRes(actualLabel, true)

    }

    @DeleteMapping("/{label_id}")
    fun handlerDeleteIssueLabel(
        @PathVariable project_id: Int,
        @PathVariable issue_id: Int,
        @PathVariable label_id: Int,
    ): ResponseEntity<Any> {

        return if (mapper.dissociateLabelWithIssue(label_id, issue_id, project_id) > 0)
            sirenMessageRes("Label $label_id dissociated from issue $issue_id successfully", "label")
        else notFound(
            "Label not Found",
            "Couldn't delete relation between Label $label_id and Issue $issue_id"
        )
    }
}