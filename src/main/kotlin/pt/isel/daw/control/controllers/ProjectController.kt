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
import pt.isel.daw.model.Project
import pt.isel.daw.view.problem.notFound
import pt.isel.daw.view.problem.problemResponse
import pt.isel.daw.view.siren.sirenMessageRes
import pt.isel.daw.view.siren.sirenProjectRes
import pt.isel.daw.view.siren.sirenProjectsRes

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class ProjectInputModel(
    val name: String,
    val description: String?,
)

@RestController
@RequestMapping("/daw/project", headers = ["Accept=application/json"])
class ProjectController(val mapper: ProjectMapper, val stateMapper: IssueStateMapper) {

    @GetMapping("")
    fun handlerGetProject() =
        sirenProjectsRes(mapper.readAll().map { Pair<Project, Iterable<IssueState>>(it, stateMapper.readAll(it.id!!)) })

    @GetMapping("/{id}")
    fun handlerGetProjectById(
        @PathVariable id: Int,
    ): ResponseEntity<Any> {
        val proj: Project = mapper.read(id) ?: return notFound("Project not found", "Project $id not found")
        val states = stateMapper.readAll(id)
        return sirenProjectRes(proj, states)
    }

    @PostMapping("")
    fun handlerPostProject(
        @RequestBody project: ProjectInputModel
    ): ResponseEntity<Any> {

        if (project.name.length > 32)
            return problemResponse(
                "about:blank", "Exceeds Character limit", 400,
                "Name exceeds maximum size of 32 characters"
            )

        if (project.description != null && project.description.length > 256)
            return problemResponse(
                "about:blank", "Exceeds Character limit", 400,
                "Description exceeds maximum size of 256 characters"
            )

        val actualProject = mapper.create(
            Project(
                id = null,
                name = project.name,
                description = project.description ?: "none provided"
            )
        )
        if (actualProject != null) return sirenProjectRes(actualProject)
        return problemResponse("about:blank", "Couldn't Create", 500, "Couldn't Create Project")
    }

    @PutMapping("/{id}")
    fun handlerPutProject(
        @PathVariable id: Int,
        @RequestBody project: ProjectInputModel,
    ): ResponseEntity<Any> {

        if (project.name.length > 32)
            return problemResponse(
                "about:blank", "Exceeds Character limit", 400,
                "Name exceeds maximum size of 32 characters"
            )

        if (project.description != null && project.description.length > 256)
            return problemResponse(
                "about:blank", "Exceeds Character limit", 400,
                "Description exceeds maximum size of 256 characters"
            )

        val actualProject: Project? = mapper.update(
            Project(
                id = id,
                name = project.name,
                description = project.description ?: "none provided"
            )
        )
        if (actualProject != null) return sirenProjectRes(actualProject)
        return problemResponse("about:blank", "Couldn't Update", 500, "Couldn't update Project")
    }

    @DeleteMapping("/{id}")
    fun handlerDeleteProject(
        @PathVariable id: Int,
    ): ResponseEntity<Any> {


        return if (mapper.delete(id) > 0)
            return sirenMessageRes("Project $id deleted successfully", "project")
        else notFound("Project not found", "Couldn't delete Project $id")
    }
}