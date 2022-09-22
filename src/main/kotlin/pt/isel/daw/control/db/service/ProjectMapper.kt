package pt.isel.daw.control.db.service

import java.sql.ResultSet
import java.sql.SQLException
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.StatementContext
import org.springframework.stereotype.Component
import pt.isel.daw.model.Project

@Component
class ProjectMapper(
    private val jdbiSource: Jdbi,
) {
    private val insert = "INSERT INTO project VALUES(DEFAULT, :name, :description);"
    private val select = "SELECT id, name, description FROM project WHERE id = :id;"
    private val selectAll = "SELECT id, name, description FROM project;"
    private val update = "UPDATE project SET name = :name, description = :description WHERE id = :id;"
    private val delete = "DELETE FROM project WHERE id = :id;"

    private val mapProject = { rs: ResultSet, _: StatementContext ->
        Project(
            id = rs.getInt("id"),
            name = rs.getString("name"),
            description = rs.getString("description")
        )
    }

    fun create(obj: Project): Project? = jdbiSource.withHandle<Project, SQLException> { handle: Handle ->
        handle.createUpdate(insert)
            .bind("name", obj.name)
            .bind("description", obj.description)
            .executeAndReturnGeneratedKeys()
            .map(mapProject)
            .firstOrNull()
    }

    fun read(id: Int): Project? = jdbiSource.withHandle<Project, SQLException> { handle: Handle ->
        handle.createQuery(select)
            .bind("id", id)
            .map(mapProject)
            .list()
            .firstOrNull()
    }

    fun update(obj: Project): Project? = jdbiSource.withHandle<Project, SQLException> { handle: Handle ->
        handle.createUpdate(update)
            .bind("name", obj.name)
            .bind("description", obj.description)
            .bind("id", obj.id)
            .executeAndReturnGeneratedKeys()
            .map(mapProject)
            .firstOrNull()
    }

    fun delete(id: Int): Int = jdbiSource.withHandle<Int, SQLException> { handle: Handle ->
        handle.createUpdate(delete)
            .bind("id", id)
            .execute()
    }

    fun readAll(): List<Project> = jdbiSource.withHandle<List<Project>, SQLException> { handle: Handle ->
        handle.createQuery(selectAll)
            .map(mapProject)
            .list()
    }

}