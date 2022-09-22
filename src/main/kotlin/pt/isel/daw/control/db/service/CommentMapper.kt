package pt.isel.daw.control.db.service

import java.sql.ResultSet
import java.sql.SQLException
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.StatementContext
import org.springframework.stereotype.Component
import pt.isel.daw.model.Comment

@Component
class CommentMapper(
    private val jdbiSource: Jdbi,
) {
    private val byId = " AND c.id = :id"
    private val checks = "FROM comment c INNER JOIN issue i on i.id = c.issue_id " +
            "WHERE i.id = :issue_id AND project_id = :project_id"
    private val select = "SELECT c.id, content, c.creation_date, issue_id $checks$byId"
    private val insert = "INSERT INTO comment(content, issue_id) VALUES(:content, :issue_id);"

    private val selectAll = "SELECT id, content, creation_date, issue_id FROM comment WHERE issue_id = :issue_id;"
    private val update = "UPDATE comment SET content = :content $checks$byId"
    private val delete = "DELETE FROM comment c USING issue i WHERE i.id = c.issue_id " +
            "AND i.id = :issue_id AND project_id = :project_id $byId"


    fun create(obj: Comment): Comment? = jdbiSource.withHandle<Comment, SQLException> { handle: Handle ->
        handle.createUpdate(insert)
            .bind("content", obj.content)
            .bind("issue_id", obj.issue_id)
            .executeAndReturnGeneratedKeys()
            .map(mapComment)
            .firstOrNull()
    }

    private val mapComment = { rs: ResultSet, _: StatementContext ->
        Comment(
            id = rs.getInt("id"),
            content = rs.getString("content"),
            creation_date = rs.getDate("creation_date"),
            issue_id = rs.getInt("issue_id")
        )
    }

    fun read(id: Int, issue_id: Int, project_id: Int): Comment? =
        jdbiSource.withHandle<Comment, SQLException> { handle: Handle ->
            handle.createQuery(select + byId)
                .bind("id", id)
                .bind("issue_id", issue_id)
                .bind("project_id", project_id)
                .map(mapComment)
                .list()
                .firstOrNull()
        }

    fun update(obj: Comment, project_id: Int): Comment? =
        jdbiSource.withHandle<Comment, SQLException> { handle: Handle ->
            handle.createUpdate(update)
                .bind("id", obj.id)
                .bind("content", obj.content)
                .bind("issue_id", obj.issue_id)
                .bind("project_id", project_id)
                .executeAndReturnGeneratedKeys()
                .map(mapComment)
                .firstOrNull()
        }

    fun delete(id: Int, issue_id: Int, project_id: Int): Int =
        jdbiSource.withHandle<Int, SQLException> { handle: Handle ->
            handle.createUpdate(delete)
                .bind("id", id)
                .bind("issue_id", issue_id)
                .bind("project_id", project_id)
                .execute()
        }

    fun readAll(id: Int): List<Comment> = jdbiSource.withHandle<List<Comment>, SQLException> { handle: Handle ->
        handle.createQuery(selectAll)
            .bind("issue_id", id)
            .map(mapComment)
            .list()
    }
}