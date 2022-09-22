export function getLink(links: Array<SirenLinks>, rel: string) {
    for(let i = 0; i < links?.length; i++) {
        if(links[i].rel.includes(rel)) return links[i].href
    }
    return ''
}

export function apiUriSlice(uri: string) {
    return uri.split('/daw')[1]
}