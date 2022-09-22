package pt.isel.daw.view

import org.eclipse.jetty.http.HttpMethod
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.util.UriComponentsBuilder
import pt.isel.daw.model.IssueState
import pt.isel.daw.model.Label
import pt.isel.daw.view.siren.ComboItem

fun Iterable<IssueState>.toComboItem(): Iterable<ComboItem> = map { ComboItem(it.id!!, it.name!!) }

@JvmName("toComboItemLabel")
fun Iterable<Label>.toComboItem(): Iterable<ComboItem> = map { ComboItem(it.id!!, it.name) }

/**
 * When performing a POST request, the uri does not include the entity's id, since it is not known beforehand
 * Since we are using the uri to send as 'self' for siren links, and other links are built upon it, this function
 * alters the baseUri to conform.
 * When performing DELETE requests, we have a symmetrical problem, as the entity no longer exists, the 'self' link
 * should no longer point to it, so we must alter it to become the same as 'up' (Go back one)
 * @param baseUri {UriComponentsBuilder} - The base path, to be altered should the request be a POST
 * @param self {UriComponentsBuilder} - The self path, to be altered should the request be a DELETE
 * @return {String} - The self path in string form, after being altered if necessary
 */
fun correctPostAndDelete(baseUri: UriComponentsBuilder, self: UriComponentsBuilder, id: Int): String {
    val requestAttributes = RequestContextHolder.getRequestAttributes()
    if (requestAttributes is ServletRequestAttributes)
        when (requestAttributes.request.method) {
            HttpMethod.POST.asString() -> {
                baseUri.path("/$id").build()
                return baseUri.toUriString()
            }
            HttpMethod.DELETE.asString() -> return self.path("/..").build().normalize().toUriString()
        }
    return self.toUriString()
}