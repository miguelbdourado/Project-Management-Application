package pt.isel.daw.view.siren

import org.eclipse.jetty.http.HttpMethod
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder
import pt.isel.daw.model.Label
import pt.isel.daw.view.correctPostAndDelete
import pt.isel.daw.view.toComboItem


fun sirenLabelRes(label: Label, issue: Boolean = false) =
    sirenResponse(if (issue) sirenIssueLabel(label) else sirenLabel(label))

fun sirenLabelsRes(labels: Iterable<Label>, altLabels: Iterable<Label>? = null) =
    sirenResponse(if (altLabels != null) sirenIssueLabels(labels, altLabels.toComboItem()) else sirenLabels(labels))


fun sirenLabels(labels: Iterable<Label>): Siren<Array<Label>> {

    val currUriBuilder = ServletUriComponentsBuilder.fromCurrentRequest()
    fun currPath() = currUriBuilder.cloneBuilder()

    val selfUri = currUriBuilder.toUriString()
    val projectUri = currPath().path("/..").build().normalize().toUriString() // Go back one

    return Siren(
        arrayOf("label", "collection"),
        null,
        labels.map { sirenLabel(it, currPath().path("/${it.id}")) }.toTypedArray(),
        arrayOf(
            makeSirenAction(
                "post-label",
                "Add a Label",
                HttpMethod.POST,
                selfUri,
                arrayOf(
                    makeSirenField("name", "text", "Name of the label"),
                )
            )
        ),
        arrayOf(
            SirenLinks(arrayOf("self"), selfUri),
            SirenLinks(arrayOf("up"), projectUri),
        ),
        "Label list for project ${projectUri.substringAfterLast("/")}"
    )
}

fun sirenLabel(
    label: Label,
    baseUri: UriComponentsBuilder = ServletUriComponentsBuilder.fromCurrentRequest()
): Siren<Label> {
    fun currPath() = baseUri.cloneBuilder()

    val selfUri = correctPostAndDelete(baseUri, currPath(), label.id!!)

    val labelsUri = currPath().path("/..").build().normalize().toUriString() // Go back one

    return Siren(
        arrayOf("label", "item"),
        label,
        null,
        arrayOf(
            makeSirenAction(
                "put-label",
                "Alter this Label",
                HttpMethod.PUT,
                selfUri,
                arrayOf(
                    makeSirenField("name", "text", "Name of the label", label.name),
                )
            ), makeSirenAction(
                "delete-label",
                "Delete Label from project",
                HttpMethod.DELETE,
                selfUri,
                null
            )
        ),
        arrayOf(
            SirenLinks(arrayOf("self"), selfUri),
            SirenLinks(arrayOf("up"), labelsUri),
        ),
        "Label ${label.id} - ${label.name}"
    )
}


fun sirenIssueLabels(labels: Iterable<Label>, altLabels: Iterable<ComboItem>? = null): Siren<Array<Label>> {

    val currUriBuilder = ServletUriComponentsBuilder.fromCurrentRequest()
    fun currPath() = currUriBuilder.cloneBuilder()

    val selfUri = currUriBuilder.toUriString()
    val issueUri = currPath().path("/..").build().normalize().toUriString() // Go back one

    return Siren(
        arrayOf("label", "collection"),
        null,
        labels.map { sirenIssueLabel(it, currPath().path("/${it.id}")) }.toTypedArray(),
        arrayOf(
            makeSirenAction(
                "post-label",
                "Add a Label to this issue",
                HttpMethod.POST,
                selfUri,
                arrayOf(
                    if (altLabels != null) {
                        makeSirenField(
                            "id",
                            "number",
                            "Id referencing existing label",
                            altLabels,
                            arrayOf("Combos", "Label")
                        )
                    } else {
                        makeSirenField("id", "number", "Id referencing existing label")
                    }

                )
            )
        ),
        arrayOf(
            SirenLinks(arrayOf("self"), selfUri),
            SirenLinks(arrayOf("up"), issueUri),
        ),
        "Label list for issue ${issueUri.substringAfterLast("/")}"
    )
}

fun sirenIssueLabel(
    label: Label,
    basePath: UriComponentsBuilder = ServletUriComponentsBuilder.fromCurrentRequest()
): Siren<Label> {
    fun currPath() = basePath.cloneBuilder()

    val selfUri = correctPostAndDelete(basePath, currPath(), label.id!!)

    val issueLabelsUri = currPath().path("/..").build().normalize().toUriString() // Go back one
    val labelUri = currPath().path("/../../../../label/${label.id}").build().normalize().toUriString()
    return Siren(
        arrayOf("label", "item"),
        label,
        arrayOf(
            makeSirenSubEntity(
                arrayOf("label", "item"),
                arrayOf("#issue-label-project-label"),
                labelUri,
                "Get label in project"
            )
        ),
        arrayOf(
            makeSirenAction(
                "delete-label",
                "Remove Label from issue",
                HttpMethod.DELETE,
                selfUri,
                null
            )
        ),
        arrayOf(
            SirenLinks(arrayOf("self"), selfUri),
            SirenLinks(arrayOf("up"), issueLabelsUri),
        ),
        "Label ${label.id} - ${label.name}"
    )
}