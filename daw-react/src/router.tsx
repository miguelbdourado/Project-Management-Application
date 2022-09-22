import React, {useState} from 'react'
import ReactDOM from 'react-dom'
import {BrowserRouter as Router, Route, Switch} from 'react-router-dom'
import {Login, Logout} from './components/login/Login'
import * as UserSession from './components/login/UserSession'
import {NavbarComponent} from './components/navbarComponent'
import {FetchGenericComponent} from './components/genericHandlers'


function App() {
    const userSessionRepo = UserSession.createRepository()
    const [userCredentials, setUserCredentials] = useState<UserSession.Credentials | undefined>(userSessionRepo.isLoggedIn())

    const currentSessionContext = {
        credentials: userCredentials,
        login: (username: string, password: string) => {
            setUserCredentials(userSessionRepo.login(username, password))
        },
        logout: () => {
            userSessionRepo.logout()
            setUserCredentials(undefined)
        }
    }

    return (
        <UserSession.Context.Provider value={currentSessionContext}>
            <NavbarComponent/>
            <Switch>
                <Route path="/project/:pid/issue/:iid/comment/:cid">
                    <FetchGenericComponent/>
                </Route>
                <Route path="/project/:pid/issue/:iid/label">
                    <FetchGenericComponent/>
                </Route>
                <Route path="/project/:pid/issue/:iid/comment">
                    <FetchGenericComponent/>
                </Route>
                <Route path="/project/:pid/label/:lid">
                    <FetchGenericComponent/>
                </Route>
                <Route path="/project/:pid/issue/:iid">
                    <FetchGenericComponent/>
                </Route>
                <Route path="/project/:pid/issuestate/:sid">
                    <FetchGenericComponent/>
                </Route>
                <Route path="project/:pid/transition">
                    <FetchGenericComponent/>
                </Route>
                <Route path="/project/:pid/label">
                    <FetchGenericComponent/>
                </Route>
                <Route path="/project/:pid/issue">
                    <FetchGenericComponent/>
                </Route>
                <Route path="/project/:pid/issuestate">
                    <FetchGenericComponent/>
                </Route>
                <Route path="/project/:pid">
                    <FetchGenericComponent/>
                </Route>
                <Route path="/project">
                    <FetchGenericComponent/>
                </Route>
                <Route path="/logout">
                    <Logout redirectPath="/project"/>
                </Route>
                <Route path="/login">
                    <Login redirectPath="/project"/>
                </Route>
                <Route path="/">
                    <Login redirectPath="/project"/>
                </Route>
            </Switch>
        </UserSession.Context.Provider>
    )
}


export function main() {
    ReactDOM.render(
        <Router>

            <App/>
        </Router>,
        document.getElementById('root')
    )
}