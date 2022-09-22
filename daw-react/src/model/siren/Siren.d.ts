type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'

interface Siren<T> {
    class: Array<string>,
    properties: T,
    entities: Array<Siren<any> | SirenSubEntity>,
    actions: Array<SirenAction>,
    links: Array<SirenLinks>,
    title: string,
}

interface SirenSubEntity {
    class: Array<string>,
    rel: Array<string>,
    href: string,
    type: string,
    title: string,
}

interface SirenAction {
    name: string,
    title: string,
    method: HttpMethod,
    href: string,
    type: string,
    fields: Array<SirenField>,
}

interface SirenField {
    name: string,
    type: string,
    title: string,
    value: object,
    class: Array<string>,
}

interface SirenLinks {
    rel: Array<string>,
    href: string,
}

interface ComboItem {
    id: number,
    name: string,
}

// In TypeScript, this denotes a loose object containing properties identified by strings and containing objects,
// In plain JavaScript, we call that an Object
interface LooseObject {
    [k: string]: string
}
