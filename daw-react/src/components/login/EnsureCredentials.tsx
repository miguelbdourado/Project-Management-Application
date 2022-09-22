import {Redirect} from 'react-router-dom'
import React, {ReactNode} from 'react'

import * as UserSession from './UserSession'

/**
 * Type that specifies the props object for the EnsureCredentials component.
 */
type EnsureCredentialsProps = {
    loginPageRoute: string,
    state: LooseObject,
    children?: ReactNode
}

/**
 * Component responsible for verifying if the user has already entered his credentials.
 */
export function EnsureCredentials({loginPageRoute, state, children}: EnsureCredentialsProps) {
    return <UserSession.Context.Consumer>
        {user => user?.credentials ? <> {children} </> : <Redirect to={{pathname: loginPageRoute, state}}/>}
    </UserSession.Context.Consumer>
}