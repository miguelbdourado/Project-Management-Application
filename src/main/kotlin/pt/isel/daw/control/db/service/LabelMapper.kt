package pt.isel.daw.control.db.service

import java.sql.ResultSet
import java.sql.SQLException
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.StatementContext
import org.springframework.stereotype.Component
import pt.isel.daw.model.Label

@Component
class LabelMapper(
    private val jdbiSource: Jdbi,
) {
    private val checkProject = "FROM label WHERE id = :id  AND project_id = :project_id"
    private val issueSelectAll = "SELECT id, name, project_id FROM label INNER JOIN" +
            " issue_label i on label.id = i.label_id WHERE project_id = :project_id AND issue_id = :issue_id"
    private val insert = "INSERT INTO label VALUES(DEFAULT, :name, :project_id);"
    private val select = "SELECT id, name, project_id $checkProject"
    private val checkId = " AND label.id = :id;"
    private val selectAll = "SELECT id, name, project_id FROM label WHERE project_id = :project_id"
    private val update = "UPDATE label SET name = :name WHERE id = :id AND project_id = :project_id;"
    private val delete = "DELETE $checkProject"
    private val insertIssueLabel =
        """WITH selected AS (SELECT i.id as issue_id, l.id as label_id, l.name, i.project_id
FROM label l INNER JOIN issue i on l.project_id = i.project_id
WHERE i.id = :issue_id AND l.id = :label_id AND i.project_id = :project_id),
     inserted AS (INSERT INTO issue_label SELECT issue_id, label_id FROM selected RETURNING issue_id)
SELECT label_id as id, name, project_id FROM selected s INNER JOIN inserted i ON s.issue_id = i.issue_id;"""
    private val deleteIssueLabel = """DELETE
FROM issue_label USING issue_label il INNER JOIN issue i ON il.issue_id = id
WHERE i.id = :issue_id
  AND il.label_id = :label_id
  AND project_id = :project_id;"""

    private val mapLabel = { rs: ResultSet, _: StatementContext ->
        Label(
            id = rs.getInt("id"),
            name = rs.getString("name"),
            project_id = rs.getInt("project_id")
        )
    }

    fun associateLabelWithIssue(label_id: Int, issue_id: Int, project_id: Int): Label? =
        jdbiSource.withHandle<Label, SQLException> { handle: Handle ->
            handle.createQuery(insertIssueLabel)
                .bind("label_id", label_id)
                .bind("issue_id", issue_id)
                .bind("project_id", project_id)
                .map(mapLabel)
                .firstOrNull()
        }

    fun dissociateLabelWithIssue(label_id: Int, issue_id: Int, project_id: Int): Int =
        jdbiSource.withHandle<Int, SQLException> { handle: Handle ->
            handle.createUpdate(deleteIssueLabel)
                .bind("label_id", label_id)
                .bind("issue_id", issue_id)
                .bind("project_id", project_id)
                .execute()
        }


    fun create(obj: Label): Label? = jdbiSource.withHandle<Label, SQLException> { handle: Handle ->
        handle.createUpdate(insert)
            .bind("name", obj.name)
            .bind("project_id", obj.project_id)
            .executeAndReturnGeneratedKeys()
            .map(mapLabel)
            .firstOrNull()
    }

    fun read(id: Int, project_id: Int): Label? = jdbiSource.withHandle<Label, SQLException> { handle: Handle ->
        handle.createQuery(select)
            .bind("id", id)
            .bind("project_id", project_id)
            .map(mapLabel)
            .list()
            .firstOrNull()
    }

    fun read(id: Int, project_id: Int, issue_id: Int): Label? =
        jdbiSource.withHandle<Label, SQLException> { handle: Handle ->
            handle.createQuery(issueSelectAll + checkId)
                .bind("project_id", project_id)
                .bind("issue_id", issue_id)
                .bind("id", id)
                .map(mapLabel)
                .list()
                .firstOrNull()
        }

    fun update(obj: Label): Label? = jdbiSource.withHandle<Label, SQLException> { handle: Handle ->
        handle.createUpdate(update)
            .bind("name", obj.name)
            .bind("project_id", obj.project_id)
            .bind("id", obj.id)
            .executeAndReturnGeneratedKeys()
            .map(mapLabel)
            .firstOrNull()
    }

    fun delete(id: Int, projectId: Int): Int = jdbiSource.withHandle<Int, SQLException> { handle: Handle ->
        handle.createUpdate(delete)
            .bind("id", id)
            .bind("project_id", projectId)
            .execute()
    }

    fun readAll(id: Int): List<Label> = jdbiSource.withHandle<List<Label>, SQLException> { handle: Handle ->
        handle.createQuery(selectAll)
            .bind("project_id", id)
            .map(mapLabel)
            .list()
    }

    fun readAll(project_id: Int, issue_id: Int): List<Label> =
        jdbiSource.withHandle<List<Label>, SQLException> { handle: Handle ->
            handle.createQuery(issueSelectAll)
                .bind("project_id", project_id)
                .bind("issue_id", issue_id)
                .map(mapLabel)
                .list()
        }
}