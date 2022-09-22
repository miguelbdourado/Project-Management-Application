import React from 'react'

export function ProblemComponent(problem: Problem) {
    return (
        <div className=" ui error message Problem">
            <h3 className="ui ">{problem.title}</h3>
            <ul className="list">
                <li>{problem.detail}</li>
            </ul>
        </div>
    )
}