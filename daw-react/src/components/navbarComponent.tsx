import React from 'react'
import {Link, useLocation} from 'react-router-dom'
import * as UserSession from './login/UserSession'

export interface NavbarItem {
    uri: string,
    title: string,
}

const defaultList: Array<NavbarItem> = [
    {title: 'Home', uri: '/'},
    {title: 'Projects', uri: '/project'},
    {title: 'Login', uri: '/login'},
    {title: 'Logout', uri: '/logout'}
]

export function NavbarComponent({items = defaultList}: { items?: Array<NavbarItem> }) {

    const location = useLocation()
    const login = items.find(item => item.title == 'Login')
    const logout = items.find(item => item.title == 'Logout')
    if(login && logout) items = items.filter(item => item !== login && item !== logout)

    return <div className="ui segment">
        <div className="ui inverted menu">
            <div className="ui header item">DAW</div>
            {
                items.map((item, idx) =>
                    <Link to={item.uri} key={idx}
                          className={location.pathname === item.uri ? 'active item' : 'item'}>{item.title}</Link>
                )
            }
            {login && logout ? <UserSession.Context.Consumer>
                {user => {
                    const curr = user?.credentials ? logout : login
                    return <Link to={{pathname: curr.uri, state: {callback: location.pathname}}} key={curr.title}
                                 className={location.pathname === curr.uri ? 'active item' : 'item'}>{curr.title}</Link>
                }}
            </UserSession.Context.Consumer> : undefined}
        </div>
    </div>
}