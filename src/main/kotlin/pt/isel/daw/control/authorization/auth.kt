package pt.isel.daw.control.authorization

import java.util.Base64
import org.jdbi.v3.core.Jdbi
import pt.isel.daw.control.db.service.ProfileMapper
import pt.isel.daw.model.Profile

// Utility file for 'Basic' HTTP Authorization helper functions


fun verifyUserCredentials(userCredentials: String, jdbi: Jdbi): Profile? {
    val profile = decodeAuth(userCredentials) ?: return null
    val actual = ProfileMapper(jdbi).read(profile.name) ?: return null
    if (actual.password == profile.password) return actual
    return null
}

//fun encodeAuth(userId: String, password: String): String {
//    val encodedString: String = Base64.getEncoder().encodeToString("$userId:$password".toByteArray())
//
//    println(encodedString)
//    return encodedString
//}

fun decodeAuth(encoded: String): Profile? {
    if (encoded == "") return null

    val decodedString = Base64.getDecoder().decode(encoded.split(" ").last())

    val splitList = decodedString.toString(Charsets.US_ASCII).split(":", ignoreCase = false, limit = 2)
    val username = splitList.first()
    val password = splitList.last()
    println("User: $username \nPassword: $password")
    return Profile(null, username, password)
}
