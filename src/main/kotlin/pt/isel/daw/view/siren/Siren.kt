@file:Suppress("ArrayInDataClass")

package pt.isel.daw.view.siren

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.eclipse.jetty.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

const val MEDIA_TYPE_SIREN = "application/vnd.siren+json"
const val DOCS_SIREN = "https://github.com/isel-leic-daw/daw-project-li61d-g010/wiki/Siren"

interface SirenEntity {
    val klass: Array<String>
    val title: String?
}

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class Siren<T>(
    @JsonProperty("class")
    override val klass: Array<String>,
    val properties: T?,
    val entities: Array<SirenEntity>?,
    val actions: Array<SirenAction>?,
    val links: Array<SirenLinks>?,
    override val title: String?
) : SirenEntity

data class SirenSubEntity(
    @JsonProperty("class")
    override val klass: Array<String>,
    val rel: Array<String>,
    val href: String,
    val type: String?,
    override val title: String?,
) : SirenEntity

data class SirenAction(
    val name: String,
    val title: String,
    val method: String,
    val href: String,
    val type: String,
    val fields: Array<SirenField>?,
)

data class SirenField(
    val name: String,
    val type: String,
    val title: String?,
    val value: Any?,
    @JsonProperty("class")
    val klass: Array<String>?
)

data class SirenLinks(
    val rel: Array<String>,
    val href: String,
)

data class ComboItem(
    val id: Int,
    val name: String
)

fun makeSirenSubEntity(
    klass: Array<String>,
    rel: Array<String>,
    href: String,
    title: String?,
    type: String = MEDIA_TYPE_SIREN
): SirenSubEntity {
    val parsed = rel.map { if (it.startsWith('#')) DOCS_SIREN + it else it }
    return SirenSubEntity(klass, parsed.toTypedArray(), href, type, title)
}

fun makeSirenAction(
    name: String, title: String, method: HttpMethod, href: String, fields: Array<SirenField>?,
    type: String = MediaType.APPLICATION_JSON.toString()
): SirenAction = SirenAction(name, title, method.asString(), href, type, fields)

fun makeSirenField(
    name: String,
    type: String,
    title: String?,
    value: Any? = null,
    klass: Array<String>? = null
): SirenField = SirenField(name, type, title, value, klass)

fun <T> sirenResponse(body: Siren<T>): ResponseEntity<Any> = ResponseEntity.status(200)
    .contentType(MediaType.parseMediaType(MEDIA_TYPE_SIREN))
    .body(body)