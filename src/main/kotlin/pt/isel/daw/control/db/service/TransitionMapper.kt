package pt.isel.daw.control.db.service

import java.sql.ResultSet
import java.sql.SQLException
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.StatementContext
import org.springframework.stereotype.Component
import pt.isel.daw.model.IssueState
import pt.isel.daw.model.Transition

@Component
class TransitionMapper(
    private val jdbiSource: Jdbi,
) {
    private val check = " AND project_id = :project_id"
    private val checkUpdate = " AND f.project_id = :project_id"
    private val checkReadAll = " WHERE f.project_id = :project_id;"
    private val insert = """WITH selected AS
         (SELECT f.id   AS from_state_id,
                 t.id   AS to_state_id,
                 f.project_id,
                 f.name as from_state_name,
                 t.name as to_state_name
          FROM issue_state f
                   INNER JOIN issue_state t ON f.project_id = t.project_id
          WHERE f.project_id = :project_id
            AND f.id = :from_state_id
            AND t.id = :to_state_id),
     inserted
         AS (INSERT INTO state_transitions (from_state_id, to_state_id)
             SELECT from_state_id, to_state_id FROM selected
         RETURNING id, from_state_id, to_state_id)
SELECT i.id, i.from_state_id, i.to_state_id, project_id, from_state_name, to_state_name
FROM inserted i
         INNER JOIN selected s on i.from_state_id = s.from_state_id AND i.to_state_id = s.to_state_id;"""
    private val selectAll = """SELECT state_transitions.id,
       from_state_id,
       to_state_id,
       f.project_id,
       f.name as from_state_name,
       t.name as to_state_name
    FROM state_transitions
         INNER JOIN issue_state f on f.id = from_state_id
         INNER JOIN issue_state t on t.id = to_state_id"""

    private val select = " WHERE state_transitions.id = :id"
    private val update = """UPDATE state_transitions st SET from_state_id = :from_state_id, to_state_id = :to_state_id
FROM issue_state f INNER JOIN issue_state t on f.project_id = t.project_id
WHERE st.id = :id AND f.id = :from_state_id AND t.id = :to_state_id AND f.project_id = :project_id
RETURNING st.id, st.from_state_id, st.to_state_id, f.project_id, f.name AS from_state_name, t.name AS to_state_name;"""
    private val delete = "DELETE FROM state_transitions USING issue_state WHERE " +
            "issue_state.id = state_transitions.from_state_id AND state_transitions.id = :id"
    private val selectByState = " WHERE from_state_id = :from_state_id AND to_state_id = :to_state_id"


    private val mapTransition = { rs: ResultSet, _: StatementContext ->
        Transition(
            id = rs.getInt("id"),
            fromState = IssueState(
                id = rs.getInt("from_state_id"),
                project_id = rs.getInt("project_id"),
                name = rs.getString("from_state_name")
            ),
            toState = IssueState(
                id = rs.getInt("to_state_id"),
                project_id = rs.getInt("project_id"),
                name = rs.getString("to_state_name")
            )
        )
    }

    fun create(obj: Transition, project_id: Int): Transition? =
        jdbiSource.withHandle<Transition, SQLException> { handle: Handle ->
            handle.createQuery(insert)
                .bind("from_state_id", obj.fromState.id)
                .bind("to_state_id", obj.toState.id)
                .bind("project_id", project_id)
                .map(mapTransition)
                .firstOrNull()
        }

    fun read(id: Int, project_id: Int): Transition? =
        jdbiSource.withHandle<Transition, SQLException> { handle: Handle ->
            handle.createQuery(selectAll + select + checkUpdate)
                .bind("id", id)
                .bind("project_id", project_id)
                .map(mapTransition)
                .list()
                .firstOrNull()
        }

    fun readByState(fromStateId: Int, toStateId: Int): Transition? =
        jdbiSource.withHandle<Transition, SQLException> { handle: Handle ->
            handle.createQuery(selectAll + selectByState)
                .bind("from_state_id", fromStateId)
                .bind("to_state_id", toStateId)
                .map(mapTransition)
                .list()
                .firstOrNull()
        }

    fun update(obj: Transition): Transition? = jdbiSource.withHandle<Transition, SQLException> { handle: Handle ->
        handle.createQuery(update)
            .bind("from_state_id", obj.fromState.id)
            .bind("to_state_id", obj.toState.id)
            .bind("id", obj.id)
            .bind("project_id", obj.fromState.project_id)
            .map(mapTransition)
            .firstOrNull()
    }

    fun delete(id: Int, project_id: Int): Int = jdbiSource.withHandle<Int, SQLException> { handle: Handle ->
        handle.createUpdate(delete + check)
            .bind("id", id)
            .bind("project_id", project_id)
            .execute()
    }

    fun readAll(id: Int): List<Transition> = jdbiSource.withHandle<List<Transition>, SQLException> { handle: Handle ->
        handle.createQuery(selectAll + checkReadAll)
            .bind("project_id", id)
            .map(mapTransition)
            .list()
    }

}