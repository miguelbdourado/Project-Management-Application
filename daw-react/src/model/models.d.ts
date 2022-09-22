interface Comment {
    id: number,
    content: string,
    creation_date: Date,
    issue_id: number,
}

interface Issue {
    id: number,
    project_id: number,
    name: string,
    description: string,
    creation_date: Date,
    close_date: Date,
    state_id: number,
}

interface IssueState {
    id: number,
    project_id: number,
    name: string,
}

interface Label {
    id: number,
    name: string,
    project_id: number,
}

interface Profile {
    id: number,
    name: string,
    password: string,
}

interface Transition {
    id: number,
    fromState: IssueState,
    toState: IssueState,
}

interface Project {
    id: number,
    name: string,
    description: string,
}

interface Problem {
    type: string,
    title: string,
    status: number,
    detail: string,
    instance: string,
}