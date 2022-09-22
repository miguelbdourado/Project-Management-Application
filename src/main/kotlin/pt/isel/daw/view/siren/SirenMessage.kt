package pt.isel.daw.view.siren

import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import pt.isel.daw.control.util.Message

fun sirenMessageRes(msg: String, type: String) = sirenResponse(sirenMessage(msg, type))

fun sirenMessage(msg: String, type: String): Siren<Message> {
    val message = Message(msg)
    val currUriBuilder = ServletUriComponentsBuilder.fromCurrentRequest()
    fun currPath(): ServletUriComponentsBuilder = currUriBuilder.cloneBuilder()

    val selfUri = currUriBuilder.toUriString()
    val baseUri = currPath().path("/..").build().normalize().toUriString() // Go back one

    return Siren(
        arrayOf(type, "item"),
        message,
        null,
        null,
        arrayOf(
            SirenLinks(arrayOf("self"), selfUri),
            SirenLinks(arrayOf("up"), baseUri),
        ),
        message.message
    )
}