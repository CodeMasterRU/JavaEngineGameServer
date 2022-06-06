CREATE DATABASE IF NOT EXISTS jdbc;
USE jdbc;

CREATE TABLE IF NOT EXISTS user (
    id INT(11) NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password TEXT NOT NULL,
    online BIT DEFAULT 0,
    creationDate DATE NOT NULL,
    sessionID VARCHAR(10) NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS lobby (
    id INT(11) NOT NULL AUTO_INCREMENT,
    idMatch VARCHAR(10) NOT NULL UNIQUE,
    hostId INT(11),
    guestId INT(11) NULL,
    isOpen BIT DEFAULT 1,
    PRIMARY KEY(id),
    FOREIGN KEY (hostId) REFERENCES user(id),
    FOREIGN KEY (guestId) REFERENCES user(id)
);







