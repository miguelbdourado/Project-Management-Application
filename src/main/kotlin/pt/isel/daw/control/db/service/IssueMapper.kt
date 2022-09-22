package pt.isel.daw.control.db.service

import java.sql.ResultSet
import java.sql.SQLException
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.StatementContext
import org.springframework.stereotype.Component
import pt.isel.daw.model.Issue

@Component
class IssueMapper(
    private val jdbiSource: Jdbi,
) {
    private val checks = "FROM issue WHERE id = :id AND project_id = :project_id;"
    private val insert = "INSERT INTO issue(project_id, close_date, name, description, state) " +
            "VALUES(:project_id, :close_date, :name, :description, :state);"
    private val select = "SELECT id, project_id, name, description, creation_date, close_date, state $checks"
    private val selectAll = "SELECT id, project_id, name, description, creation_date, close_date, state " +
            "FROM issue WHERE project_id = :project_id;"
    private val update = "UPDATE issue SET close_date = :close_date, name = :name, description = :description, " +
            "state = :state WHERE id = :id AND project_id = :project_id;"
    private val delete = "DELETE $checks"


    private val mapIssue = { rs: ResultSet, _: StatementContext? ->
        Issue(
            id = rs.getInt("id"),
            project_id = rs.getInt("project_id"),
            name = rs.getString("name"),
            description = rs.getString("description"),
            creation_date = rs.getDate("creation_date"),
            close_date = rs.getDate("close_date"),
            state_id = rs.getInt("state")
        )
    }

    fun create(obj: Issue): Issue? = jdbiSource.withHandle<Issue, SQLException> { handle: Handle ->
        handle.createUpdate(insert)
            .bind("project_id", obj.project_id)
            .bind("close_date", obj.close_date)
            .bind("name", obj.name)
            .bind("description", obj.description)
            .bind("state", obj.state_id)
            .executeAndReturnGeneratedKeys()
            .map(mapIssue)
            .firstOrNull()
    }


    fun read(id: Int, projectId: Int): Issue? = jdbiSource.withHandle<Issue, SQLException> { handle: Handle ->
        handle.createQuery(select)
            .bind("id", id)
            .bind("project_id", projectId)
            .map(mapIssue)
            .list()
            .firstOrNull()
    }

    fun update(obj: Issue): Issue? = jdbiSource.withHandle<Issue, SQLException> { handle: Handle ->
        handle.createUpdate(update)
            .bind("id", obj.id)
            .bind("project_id", obj.project_id)
            .bind("close_date", obj.close_date)
            .bind("name", obj.name)
            .bind("description", obj.description)
            .bind("state", obj.state_id)
            .executeAndReturnGeneratedKeys()
            .map(mapIssue)
            .firstOrNull()
    }

    fun delete(id: Int, projectId: Int): Int = jdbiSource.withHandle<Int, SQLException> { handle: Handle ->
        handle.createUpdate(delete)
            .bind("id", id)
            .bind("project_id", projectId)
            .execute()
    }

    fun readAll(id: Int): List<Issue> = jdbiSource.withHandle<List<Issue>, SQLException> { handle: Handle ->
        handle.createQuery(selectAll)
            .bind("project_id", id)
            .map(mapIssue)
            .list()
    }
}