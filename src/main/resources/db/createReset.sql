DROP TABLE IF EXISTS issue_label;
DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS label;
DROP TABLE IF EXISTS issue;
DROP TABLE IF EXISTS state_transitions;
DROP TABLE IF EXISTS default_state;
DROP TABLE IF EXISTS issue_state;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS profile;


CREATE TABLE project
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(32) UNIQUE NOT NULL,
    description VARCHAR(256)
);

CREATE TABLE issue_state
(
    id         SERIAL PRIMARY KEY,
    project_id INT         NOT NULL,
    name       VARCHAR(32) NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE CASCADE,
    UNIQUE (project_id, name)
);

CREATE TABLE default_state
(
    project_id INT PRIMARY KEY,
    state_id   INT NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE CASCADE,
    FOREIGN KEY (state_id) REFERENCES issue_state (id)
);

CREATE TABLE state_transitions
(
    id            SERIAL PRIMARY KEY,
    from_state_id INT NOT NULL,
    to_state_id   INT NOT NULL,
    FOREIGN KEY (from_state_id) REFERENCES issue_state (id) ON DELETE CASCADE,
    FOREIGN KEY (to_state_id) REFERENCES issue_state (id) ON DELETE CASCADE,
    UNIQUE (from_state_id, to_state_id)
);

CREATE OR REPLACE FUNCTION initialize_project() RETURNS trigger
    language plpgsql
as
$$
declare
    closed_id   INT;
    archived_id INT;
begin
    -- Insert the default and mandatory "closed" and "archived" states
    INSERT INTO issue_state VALUES (DEFAULT, new.id, 'closed') RETURNING id INTO closed_id;
    INSERT INTO issue_state VALUES (DEFAULT, new.id, 'archived') RETURNING id INTO archived_id;
    -- Insert the default and mandatory transition from closed to archived
    INSERT INTO state_transitions VALUES (DEFAULT, closed_id, archived_id);
    -- Choose a default *default* state
    INSERT INTO default_state VALUES (new.id, closed_id);
    RETURN new;
end;
$$;

DROP TRIGGER IF EXISTS project_init ON project;
CREATE TRIGGER project_init
    AFTER INSERT
    ON project
    FOR EACH ROW
EXECUTE PROCEDURE initialize_project();


CREATE TABLE issue
(
    id            SERIAL PRIMARY KEY,
    project_id    INT          NOT NULL,
    creation_date DATE         NOT NULL DEFAULT CURRENT_DATE,
    close_date    DATE,
    name          VARCHAR(32)  NOT NULL,
    description   VARCHAR(256) NOT NULL,
    state         INT          NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE CASCADE,
    FOREIGN KEY (state) REFERENCES issue_state (id)
);

CREATE TABLE label
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(32) NOT NULL,
    project_id INT         NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE CASCADE,
    UNIQUE (name, project_id)
);

CREATE TABLE issue_label
(
    issue_id INT NOT NULL,
    label_id INT NOT NULL,
    PRIMARY KEY (issue_id, label_id),
    FOREIGN KEY (label_id) REFERENCES label (id) ON DELETE CASCADE,
    FOREIGN KEY (issue_id) REFERENCES issue (id) ON DELETE CASCADE
);

CREATE TABLE comment
(
    id            SERIAL PRIMARY KEY,
    content       VARCHAR(256) NOT NULL,
    creation_date DATE         NOT NULL DEFAULT CURRENT_DATE,
    issue_id      INT          NOT NULL,
    FOREIGN KEY (issue_id) REFERENCES issue (id) ON DELETE CASCADE
);

CREATE TABLE profile
(
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(64) UNIQUE NOT NULL,
    password VARCHAR(64)        NOT NULL
);