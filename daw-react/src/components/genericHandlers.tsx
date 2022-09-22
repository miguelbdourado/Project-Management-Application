import React, {FormEvent, MutableRefObject, useRef, useState} from 'react'
import {EnsureCredentials} from './login/EnsureCredentials'

import * as UserSession from './login/UserSession'
import {Context, Credentials} from './login/UserSession'
import {apiUriSlice, getLink} from '../util'
import {useFetch} from './fetch'
import {ProblemComponent} from './problemComponent'
import {Link, Redirect, useLocation} from 'react-router-dom'

export function HandleActions({actions, id, value}: { actions: Array<SirenAction>, id: string, value: string }) {
    const location = useLocation()
    const [state, setState] = useState(false)

    return <div className="ui segments">
        <button className="ui sub menu basic orange button segments" onClick={() => setState(!state)}>
            {value + (state ? '' : ' (Requires Login)')}</button>
        {state ? <EnsureCredentials loginPageRoute="/login" state={{callback: location?.pathname}}>
                <UserSession.Context.Consumer>
                    {user => actions.map(action => <div className="ui segment" key={id + '-' + action.name}>
                        <HandleAction action={action} id={id + '-' + action.name} credentials={user.credentials}/>
                    </div>)}
                </UserSession.Context.Consumer>
            </EnsureCredentials> :
            undefined
        }</div>

}

interface FieldState {
    ref?: React.MutableRefObject<HTMLInputElement | HTMLSelectElement>
    field: SirenField
    id: string
}


function fieldStateToBody(fields: Array<FieldState>) {
    const body: LooseObject = {}

    fields?.forEach(fieldState => {
        if(fieldState?.ref?.current) body[fieldState.field.name] = fieldState.ref.current.value
    })

    return JSON.stringify(body)
}

function FieldComponent({fieldState}: { fieldState: FieldState }) {
    const {field, id} = fieldState
    fieldState.ref = useRef<HTMLInputElement | HTMLSelectElement>(null)
    return <><label className="ui label" htmlFor={id}>{field.title} : </label>
        {field.class ?
            <select id={id} name={field.name} ref={fieldState.ref as MutableRefObject<HTMLSelectElement>}>
                {(field.value as Array<ComboItem>).map(comboItem =>
                    <option key={id + '-' + comboItem.id}
                            value={comboItem.id}>{comboItem.name}</option>)}
            </select>
            :
            <input minLength={1} id={id} className="ui input" ref={fieldState.ref as MutableRefObject<HTMLInputElement>}
                   name={field.name} type={field.type}
                   defaultValue={field.value?.toString()}/>}</>

}

export function HandleAction({action, id, credentials}: { action: SirenAction, id: string, credentials: Credentials }) {
    const [formState, setFormState] = useState<{ fields: Array<FieldState>, submitted: boolean }>({
        fields: action.fields?.map(field => ({field, id: id + '-' + field.name})),
        submitted: false
    })

    function fetchAction(event: FormEvent) {
        event.preventDefault()
        const newState = Object.assign({}, formState)
        newState.submitted = true
        setFormState(newState)
    }

    function Action() {
        return <form onSubmit={fetchAction} id={id} hidden={true} className="ui form segments">
            <fieldset className="ui segment">
                {formState.fields?.map(fieldState => <div key={fieldState.id}>
                    <FieldComponent fieldState={fieldState}/></div>)}
                <input className="ui button confirm segment" type="submit" value="Confirm"/>
            </fieldset>
        </form>
    }

    function toggle() {
        const elem = document.getElementById(id)
        elem.hidden = !elem.hidden
    }

    return <div>
        {
            (formState.submitted) ?
                <FetchGeneric uri={'/api' + apiUriSlice(action.href)} method={action.method} credentials={credentials}
                              body={fieldStateToBody(formState.fields)}/>
                :
                <>
                    <button className="ui sub menu button" onClick={toggle}>{action.title}</button>
                    <Action/>
                </>
        }
    </div>
}

export function FetchGenericComponent() {
    const location = useLocation()
    const fetchState = useFetch('/api' + location?.pathname)

    if(fetchState.type !== 'response') {
        if(fetchState.type === 'error' && (fetchState.status === 401)) {
            return <Redirect to={{pathname: '/logout', state: {error: fetchState.error, callback: '/login'}}}/>
        }
        return <div className="ui">
            <div className="ui active inverted dimmer">
                <div className="ui large text loader">{fetchState.type}</div>
            </div>
            <p/>
            <p/>
            <p/>
        </div>
    }
    if(fetchState.body.hasOwnProperty('type'))
        return React.createElement(ProblemComponent, fetchState.body as Problem)

    const sirenBody = fetchState.body as Siren<LooseObject>
    return <GenericSirenComponent siren={sirenBody} root/>
}


export function FetchGeneric({uri, method, credentials, body}:
                                 { uri: string, method: HttpMethod, credentials: Credentials, body: string }) {
    const location = useLocation()
    const fetchState = useFetch(uri, method, credentials, body)

    if(fetchState.type !== 'response') return fetchState.type === 'error' && (fetchState.status === 401) ?
        <Redirect to={{pathname: '/logout', state: {error: fetchState.error, callback: '/login'}}}/> :
        <div className="ui">
            <div className="ui active inverted dimmer">
                <div className="ui medium text loader">Loading</div>
            </div>
            <p/>
            <p/>
            <p/>
        </div>

    if(fetchState.body.hasOwnProperty('type'))
        return React.createElement(ProblemComponent, fetchState.body as Problem)

    const sirenBody = fetchState.body as Siren<any>
    const redirect = apiUriSlice(getLink(sirenBody.links, (method == 'DELETE') ? 'up' : 'self'))
    // Redirect does absolutely nothing if we're already at the target location, but we want things to update, so here
    // we redirect to /login with a callback to return to the current page
    if(redirect == location.pathname) return <Redirect to={{pathname: '/login', state: {callback: redirect}}}/>
    return <Redirect to={redirect}>Go to affected {sirenBody.class.join(' ')}</Redirect>
}

function ListProps({obj, id, title, link}: { obj: LooseObject, id: string, title: string, link?: string }) {
    const header = <p className="ui header inverted segment">{title} :</p>
    return <div className="ui segments ">
        {link ? <Link to={link as string}>{header}</Link> : header}
        {Object.getOwnPropertyNames(obj).map(k => {
            const key = id + '-' + k
            if(k.includes('date')) obj[k] = new Date(obj[k]).toLocaleString().split(',')[0]
            if(obj[k].hasOwnProperty('id')) return <div key={key} className="ui segment">
                <ListProps obj={obj[k] as unknown as LooseObject} id={key} title={k}/>
            </div>
            return <p key={key}
                      className="ui segment">{k} : {obj[k]}</p>
        })}</div>
}


function GenericSirenSubEntity({entity}: { entity: SirenSubEntity }) {
    return <div className={'ui segment'}><Link
        to={apiUriSlice(entity.href)}>{entity.title}</Link></div>
}

function ListEntities({entities, id}: { entities: Array<SirenSubEntity | Siren<LooseObject>>, id: string }) {
    id = id + '-entity-'
    return <div className="ui segment">
        {entities.map((entity, idx) =>
            <div key={id + idx}>
                {entity.hasOwnProperty('links') ?
                    <GenericSirenComponent siren={entity as Siren<LooseObject>} id={id + idx}/> :
                    <GenericSirenSubEntity entity={entity as SirenSubEntity}/>}
            </div>
        )}
    </div>
}

export function GenericSirenComponent({siren, id = 'root', root = false}:
                                          { siren: Siren<LooseObject>, id?: string, root?: boolean }) {
    id += '-' + siren.class.join('-') + '-' + (siren.properties?.id || 'NoPropId')
    const backlink = root && getLink(siren.links, 'up')
    if(backlink) {
        if(!siren.entities) siren.entities = []
        if(siren.entities[siren.entities.length - 1]?.title != 'Back') {
            const back = {
                href: backlink,
                title: 'Back',
                rel: ['up'],
                type: 'text/html',
                class: ['back', 'up']
            }
            siren.entities?.push(back)
        }
    }

    return <>
        <div key={id} className="segments ui">

            {siren.properties ?
                <ListProps obj={siren.properties} key={id + '-props'} id={id + '-props'}
                           title={siren.title} link={apiUriSlice(getLink(siren.links, 'self'))}/> : undefined}
            {siren.actions?.length > 0 ?
                <HandleActions key={id + '-actions'} actions={siren.actions} id={id + '-actions'}
                               value={'Actions for ' + siren.class.join(' ')}/>
                : undefined}
            {siren.entities?.length > 0 ?
                <ListEntities entities={siren.entities} id={id + '-entities'} key={id + '-entities'}/> : undefined}
        </div>
        <br/>
    </>
}