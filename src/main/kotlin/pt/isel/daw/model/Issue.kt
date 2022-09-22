package pt.isel.daw.model

import java.util.Date

data class Issue(
    val id: Int?,
    val project_id: Int,
    val name: String,
    val description: String,
    val creation_date: Date?,
    val close_date: Date?,
    val state_id: Int,
)