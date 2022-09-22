package pt.isel.daw.model

import java.util.Date

data class Comment(
    val id: Int?,
    val content: String,
    val creation_date: Date?,
    val issue_id: Int,
)