import React, {useContext, useEffect, useRef, useState} from 'react'
import {Redirect, useLocation} from 'react-router-dom'

import * as UserSession from './UserSession'


/**
 * The application's login page.
 * @argument props - the page's props object.
 * @returns The React Element used to render the page.
 */
export function Login({redirectPath}: { redirectPath: string }) {
    const location = useLocation()

    const userNameInputRef = useRef<HTMLInputElement>(null)
    const pwdInputRef = useRef<HTMLInputElement>(null)

    type CredentialsState = { usernameOK: boolean, passwordOK: boolean }
    const [credentialsState, setCredentialsState] = useState<CredentialsState | undefined>()
    const userSession = useContext(UserSession.Context)
    const stateFrom = location?.state as LooseObject
    if(stateFrom?.callback) redirectPath = stateFrom.callback

    function credentialsAreOK() {
        return credentialsState?.usernameOK && credentialsState?.passwordOK
    }

    const loginReady = userSession?.credentials || credentialsAreOK()
    if(loginReady && stateFrom?.callback) stateFrom.callback = ''

    function handleSubmit() {
        const username = userNameInputRef.current?.value
        const password = pwdInputRef.current?.value

        const enteredCredentials: CredentialsState = {
            usernameOK: username !== undefined && (username.length > 0) && !username.includes(' ') && (username.length < 64),
            passwordOK: password !== undefined && (password.length > 0) && (password.length < 64)
        }

        if(!enteredCredentials.usernameOK) userNameInputRef.current?.focus()
        else if(!enteredCredentials.passwordOK) pwdInputRef.current?.focus()

        if(username && password && userSession)
            userSession.login(username, password)

        setCredentialsState(enteredCredentials)
    }

    const showError = !credentialsAreOK() && credentialsState
    // noinspection HtmlUnknownTarget
    return loginReady ? <Redirect to={{pathname: redirectPath, state: stateFrom}}/> : (
        <div className="ui middle aligned center aligned grid" style={{marginTop: 125}}>
            <div className="column" style={{maxWidth: 380}}>
                <h2 className="ui header centered">
                    <img src="/Icon.png" className="ui centered small image " alt="ALL KNOWING EYE OF REACT"/>
                    <div className="content">DAW Web App</div>
                </h2>
                <form className={`ui large form ${showError || stateFrom?.error ? ' error' : ''}`}>
                    <div className="ui segment">
                        <div className={`field ${credentialsState && !credentialsState.usernameOK ? 'error' : ''}`}>
                            <div className="ui input left icon">
                                <i className="user icon"/>
                                <input type="text" name="username" placeholder="Your username" ref={userNameInputRef}/>
                            </div>
                        </div>
                        <div className={`field ${credentialsState && !credentialsState.passwordOK ? 'error' : ''}`}>
                            <div className="ui input left icon">
                                <i className="key icon"/>
                                <input type="password" name="password" placeholder="Your password" ref={pwdInputRef}/>
                            </div>
                        </div>
                        <button className="ui fluid large submit button teal" type="button" onClick={handleSubmit}>
                            <i className="sign in icon"/>Sign in
                        </button>
                    </div>
                    {
                        showError ?
                            <div className="ui error message">
                                <p>Enter a username with no whitespace characters and a non empty password</p>
                            </div> : undefined
                    }
                    {
                        stateFrom?.error ?
                            <div className="ui error message">
                                <p>{stateFrom.error}</p>
                            </div>
                            : undefined
                    }
                </form>
            </div>
        </div>
    )
}

export function Logout({redirectPath}: { redirectPath: string }) {
    const userSession = useContext(UserSession.Context)
    const [done, setDone] = useState(false)
    const location = useLocation()
    const state = location?.state as LooseObject
    if(state?.callback) {
        redirectPath = state.callback
        state.callback = ''
    }
    useEffect(() => {
        userSession.logout()
        setDone(true)
    })
    return done ? <div className="ui">
            <div className="ui active inverted dimmer">
                <div className="ui large text loader">Logging out</div>
            </div>
            <p/><p/><p/></div>
        : <Redirect to={{pathname: redirectPath, state}}/>
}