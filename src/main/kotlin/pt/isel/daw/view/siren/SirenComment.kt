package pt.isel.daw.view.siren

import org.eclipse.jetty.http.HttpMethod
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder
import pt.isel.daw.model.Comment
import pt.isel.daw.view.correctPostAndDelete

fun sirenCommentRes(comment: Comment) = sirenResponse(sirenComment(comment))

fun sirenCommentsRes(comments: Iterable<Comment>) = sirenResponse(sirenComments(comments))


fun sirenComments(comments: Iterable<Comment>): Siren<Array<Comment>> {

    val currUriBuilder = ServletUriComponentsBuilder.fromCurrentRequest()
    fun currPath() = currUriBuilder.cloneBuilder()

    val selfUri = currUriBuilder.toUriString()
    val issueUri = currPath().path("/..").build().normalize().toUriString() // Go back one

    return Siren(
        arrayOf("comment", "collection"),
        null,
        comments.map { sirenComment(it, currPath().path("/${it.id}")) }.toTypedArray(),
        arrayOf(
            makeSirenAction(
                "post-comment",
                "Add a Comment",
                HttpMethod.POST,
                selfUri,
                arrayOf(
                    makeSirenField("content", "text", "Content of the comment")
                )
            )
        ),
        arrayOf(
            SirenLinks(arrayOf("self"), selfUri),
            SirenLinks(arrayOf("up"), issueUri),
        ),
        "Comment list for issue ${issueUri.substringAfterLast("/")}"
    )
}

fun sirenComment(
    comment: Comment,
    basePath: UriComponentsBuilder = ServletUriComponentsBuilder.fromCurrentRequest()
): Siren<Comment> {
    fun currPath() = basePath.cloneBuilder()

    val selfUri = correctPostAndDelete(basePath, currPath(), comment.id!!)

    val commentsUri = currPath().path("/..").build().normalize().toUriString() // Go back one

    return Siren(
        arrayOf("comment", "item"),
        comment,
        null,
        arrayOf(
            makeSirenAction(
                "delete-comment",
                "Delete Comment",
                HttpMethod.DELETE,
                selfUri,
                null
            ),
            makeSirenAction(
                "put-comment",
                "Change Comment",
                HttpMethod.PUT,
                selfUri,
                arrayOf(
                    makeSirenField("content", "text", "Content of the comment", comment.content)
                )
            )
        ),
        arrayOf(
            SirenLinks(arrayOf("self"), selfUri),
            SirenLinks(arrayOf("up"), commentsUri),
        ),
        "Comment ${comment.id} - ${comment.content}"
    )
}