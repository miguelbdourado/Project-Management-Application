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
import pt.isel.daw.control.db.service.CommentMapper
import pt.isel.daw.control.db.service.IssueMapper
import pt.isel.daw.model.Comment
import pt.isel.daw.view.problem.notFound
import pt.isel.daw.view.problem.problemResponse
import pt.isel.daw.view.siren.sirenCommentRes
import pt.isel.daw.view.siren.sirenCommentsRes
import pt.isel.daw.view.siren.sirenMessageRes

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class CommentInputModel(
    val content: String,
)

@RestController
@RequestMapping("/daw/project/{project_id}/issue/{issue_id}/comment", headers = ["Accept=application/json"])
class CommentController(
    val mapper: CommentMapper,
    val auxIssueMapper: IssueMapper
) {

    @GetMapping("")
    fun handlerGetComments(
        @PathVariable project_id: Int,
        @PathVariable issue_id: Int,
    ): ResponseEntity<Any> {

        auxIssueMapper.read(issue_id, project_id)
            ?: return notFound(
                "Issue not found",
                "Since there is no issue $issue_id, there are also no comments"
            )

        val comments: List<Comment> = mapper.readAll(issue_id)

        return sirenCommentsRes(comments)

    }

    @GetMapping("/{comment_id}")
    fun handlerGetCommentById(
        @PathVariable project_id: Int,
        @PathVariable issue_id: Int,
        @PathVariable comment_id: Int
    ): ResponseEntity<Any> {
        val comment: Comment? = mapper.read(comment_id, issue_id, project_id)
        if (comment != null) return sirenCommentRes(comment)
        return notFound("Comment not found", "Comment $comment_id not found in issue $issue_id")
    }

    @PostMapping("")
    fun handlerPostComment(
        @PathVariable project_id: Int,
        @PathVariable issue_id: Int,
        @RequestBody comment: CommentInputModel
    ): ResponseEntity<Any> {
        if (comment.content.length > 256)
            return problemResponse(
                "about:blank", "Exceeds character limit", 400,
                "Content exceeds 256 character limit"
            )

        auxIssueMapper.read(issue_id, project_id)
            ?: return notFound(
                "Issue not found",
                "Since there is no issue $issue_id, comment cannot be added"
            )

        val actualComment = mapper.create(
            Comment(
                id = null,
                content = comment.content,
                creation_date = null,
                issue_id = issue_id
            )
        )

        if (actualComment != null) return sirenCommentRes(actualComment)

        return problemResponse("about:blank", "Couldn't Create", 500, "Couldn't Create Comment")
    }

    @PutMapping("/{comment_id}")
    fun handlerPutComment(
        @PathVariable project_id: Int,
        @PathVariable issue_id: Int,
        @PathVariable comment_id: Int,
        @RequestBody comment: CommentInputModel
    ): ResponseEntity<Any> {
        if (comment.content.length > 256)
            return problemResponse(
                "about:blank", "Exceeds character limit", 400,
                "Content exceeds 256 character limit"
            )

        val actualComment = mapper.update(
            Comment(
                id = comment_id,
                content = comment.content,
                creation_date = null,
                issue_id = issue_id
            ),
            project_id
        )

        if (actualComment != null) return sirenCommentRes(actualComment)

        return notFound("Couldn't Update", "Issue $issue_id may not exist")
    }

    @DeleteMapping("/{comment_id}")
    fun handlerDeleteComment(
        @PathVariable project_id: Int,
        @PathVariable issue_id: Int,
        @PathVariable comment_id: Int
    ): ResponseEntity<Any> {

        return if (mapper.delete(comment_id, issue_id, project_id) > 0)
            return sirenMessageRes("Comment $comment_id deleted successfully", "comment")
        else notFound("Comment not Found", "Couldn't delete Comment $comment_id")
    }
}