package pt.isel.daw.control.db.service

import java.sql.ResultSet
import java.sql.SQLException
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.StatementContext
import org.springframework.stereotype.Component
import pt.isel.daw.model.Profile

@Component
class ProfileMapper(
    private val jdbiSource: Jdbi,
) {
    private val byId = " id = :id;"
    private val byName = "name = :name;"
    private val insert = "INSERT INTO profile VALUES(DEFAULT, :name, :password);"
    private val select = "SELECT id, name, password FROM profile WHERE "
    private val selectAll = "SELECT id, name, password FROM profile;"
    private val update = "UPDATE profile SET name = :name, password = :password WHERE$byId"
    private val delete = "DELETE FROM profile WHERE$byId"


    private val mapProfile = { rs: ResultSet, _: StatementContext ->
        Profile(
            id = rs.getInt("id"),
            name = rs.getString("name"),
            password = rs.getString("password")
        )
    }

    fun create(obj: Profile): Profile? = jdbiSource.withHandle<Profile, SQLException> { handle: Handle ->
        handle.createUpdate(insert)
            .bind("name", obj.name)
            .bind("password", obj.password)
            .executeAndReturnGeneratedKeys()
            .map(mapProfile)
            .firstOrNull()
    }

    fun read(id: Int): Profile? = jdbiSource.withHandle<Profile, SQLException> { handle: Handle ->
        handle.createQuery(select + byId)
            .bind("id", id)
            .map(mapProfile)
            .list()
            .firstOrNull()
    }

    fun read(username: String): Profile? = jdbiSource.withHandle<Profile, SQLException> { handle: Handle ->
        handle.createQuery(select + byName)
            .bind("name", username)
            .map(mapProfile)
            .list()
            .firstOrNull()
    }

    fun update(obj: Profile): Profile? = jdbiSource.withHandle<Profile, SQLException> { handle: Handle ->
        handle.createUpdate(update)
            .bind("name", obj.name)
            .bind("password", obj.password)
            .bind("id", obj.id)
            .executeAndReturnGeneratedKeys()
            .map(mapProfile)
            .firstOrNull()
    }

    fun delete(id: Int): Int = jdbiSource.withHandle<Int, SQLException> { handle: Handle ->
        handle.createUpdate(delete)
            .bind("id", id)
            .execute()
    }

    fun readAll(id: Int): List<Profile> = jdbiSource.withHandle<List<Profile>, SQLException> { handle: Handle ->
        handle.createQuery(selectAll)
            .map(mapProfile)
            .list()
    }


}