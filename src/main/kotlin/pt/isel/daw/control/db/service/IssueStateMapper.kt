package pt.isel.daw.control.db.service

import java.sql.ResultSet
import java.sql.SQLException
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.StatementContext
import org.springframework.stereotype.Component
import pt.isel.daw.model.IssueState

@Component
class IssueStateMapper(
    private val jdbiSource: Jdbi,
) {

    private val checks = " FROM issue_state WHERE id = :id AND project_id = :project_id"
    private val insert = "INSERT INTO issue_state VALUES(DEFAULT, :project_id, :name);"
    private val select = "SELECT id, project_id, name$checks"
    private val selectAll = "SELECT id, project_id, name FROM issue_state WHERE project_id = :project_id;"
    private val update = "UPDATE issue_state SET name = :name WHERE id = :id AND project_id = :project_id"
    private val delete = "DELETE$checks"
    private val selectDefault = "SELECT i.id, i.project_id, i.name FROM default_state d INNER JOIN " +
            "issue_state i on i.id = d.state_id WHERE d.project_id = :project_id;"
    private val updateDefault = "WITH updated AS (UPDATE default_state SET state_id = :id" +
            " WHERE project_id = :project_id RETURNING state_id AS id) SELECT issue_state.id, project_id, name " +
            "FROM issue_state INNER JOIN updated u ON issue_state.id = u.id;"
    private val possibleTransitions = "SELECT t.id, t.project_id, t.name FROM state_transitions " +
            "INNER JOIN issue_state t on t.id = to_state_id " +
            " WHERE from_state_id = :id AND t.project_id = :project_id UNION ALL $select"


    private val mapIssueState = { rs: ResultSet, _: StatementContext? ->
        IssueState(
            id = rs.getInt("id"),
            project_id = rs.getInt("project_id"),
            name = rs.getString("name"),
        )
    }

    fun create(obj: IssueState): IssueState? = jdbiSource.withHandle<IssueState, SQLException> { handle: Handle ->
        handle.createUpdate(insert)
            .bind("project_id", obj.project_id)
            .bind("name", obj.name)
            .executeAndReturnGeneratedKeys()
            .map(mapIssueState)
            .firstOrNull()
    }

    fun read(id: Int, project_id: Int): IssueState? =
        jdbiSource.withHandle<IssueState, SQLException> { handle: Handle ->
            handle.createQuery(select)
                .bind("id", id)
                .bind("project_id", project_id)
                .map(mapIssueState)
                .list()
                .firstOrNull()
        }

    fun readDefault(project_id: Int): IssueState? = jdbiSource.withHandle<IssueState, SQLException> { handle: Handle ->
        handle.createQuery(selectDefault)
            .bind("project_id", project_id)
            .map(mapIssueState)
            .list()
            .firstOrNull()
    }

    fun possibleTransitions(id: Int, project_id: Int): List<IssueState>? =
        jdbiSource.withHandle<List<IssueState>, SQLException> { handle: Handle ->
            handle.createQuery(possibleTransitions)
                .bind("id", id)
                .bind("project_id", project_id)
                .map(mapIssueState)
                .list()
        }

    fun update(obj: IssueState): IssueState? = jdbiSource.withHandle<IssueState, SQLException> { handle: Handle ->
        handle.createUpdate(update)
            .bind("name", obj.name)
            .bind("id", obj.id)
            .bind("project_id", obj.project_id)
            .executeAndReturnGeneratedKeys()
            .map(mapIssueState)
            .firstOrNull()
    }

    fun updateDefault(obj: IssueState): IssueState? =
        jdbiSource.withHandle<IssueState, SQLException> { handle: Handle ->
            handle.createQuery(updateDefault)
                .bind("id", obj.id)
                .bind("project_id", obj.project_id)
                .map(mapIssueState)
                .firstOrNull()
        }

    fun delete(id: Int, project_id: Int): Int = jdbiSource.withHandle<Int, SQLException> { handle: Handle ->
        handle.createUpdate(delete)
            .bind("id", id)
            .bind("project_id", project_id)
            .execute()
    }

    fun readAll(id: Int): List<IssueState> = jdbiSource.withHandle<List<IssueState>, SQLException> { handle: Handle ->
        handle.createQuery(selectAll)
            .bind("project_id", id)
            .map(mapIssueState)
            .list()
    }

}