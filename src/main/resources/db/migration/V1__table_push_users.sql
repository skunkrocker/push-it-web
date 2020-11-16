-- 1° create push users table
CREATE TABLE PUSH_USER
(
    ROLE      VARCHAR(240),
    PASSWORD  VARCHAR(240),
    USER_NAME VARCHAR(240),
    APP_NAME  VARCHAR(240),
    ID        BIGINT GENERATED BY DEFAULT AS IDENTITY,
    PRIMARY KEY (ID)
);

-- 2° insert test user
INSERT INTO PUSH_USER(ROLE, PASSWORD, USER_NAME, APP_NAME)
VALUES ('TEST_ADMIN', 'test-password', 'test-user', 'test-app');
