import {useEffect, useReducer} from 'react'
import {Credentials} from './login/UserSession'

type State =
    {
        type: 'started'
    }
    | {
    type: 'fetching'
}
    | {
    type: 'error',
    error: string,
    status: number,
}
    | {
    type: 'response',
    status: number,
    body: object,
}

type Action =
    {
        type: 'fetching',
    }
    | {
    type: 'error',
    error: string,
    status: number,
}
    | {
    type: 'response',
    status: number,
    body: object,
}

function actionError(error: string, status: number = 500): Action {
    return {type: 'error', error, status}
}

function actionResponse(status: number, body: object): Action {
    return {type: 'response', status: status, body: body}
}

function reducer(state: State, action: Action): State {
    switch(action.type) {
        case 'fetching':
            return {type: 'fetching'}
        case 'error':
            return {type: 'error', error: action.error, status: action.status}
        case 'response':
            return {type: 'response', status: action.status, body: action.body}
    }
}

export function useFetch(uri: string, method: HttpMethod = 'GET', credentials?: Credentials, body?: string): State {
    const [state, dispatcher] = useReducer(reducer, {type: 'started'})

    useEffect(() => {
        if(!uri) return
        let isCancelled = false
        const abortController = new AbortController()
        const signal = abortController.signal

        async function doFetch() {
            try {
                dispatcher({type: 'fetching'})
                const headers: { Authorization?: string, 'Content-Type'?: string } = credentials ?
                    {Authorization: credentials.type + ' ' + credentials.content} : {}
                if(body) headers['Content-Type'] = 'application/json'
                const options = {signal, method, headers, body}
                const resp = await fetch(uri, options)
                if(isCancelled) return
                if(resp.status === 401) {
                    dispatcher(actionError('Invalid Credentials ', resp.status))
                    return
                }
                const respBody = await resp.json()
                if(isCancelled) return
                dispatcher(actionResponse(resp.status, respBody))
            } catch(error) {
                if(isCancelled) return
                dispatcher(actionError(error.message))
            }
        }

        doFetch()
        return () => {
            isCancelled = true
            abortController.abort()
        }
    }, [uri])

    return state
}