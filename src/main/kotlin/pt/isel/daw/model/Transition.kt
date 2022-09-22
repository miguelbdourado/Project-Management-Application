package pt.isel.daw.model

data class Transition(
    val id: Int?,
    val fromState: IssueState,
    val toState: IssueState,
)