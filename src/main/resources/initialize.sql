CREATE DATABASE IF NOT EXISTS jdbc;
USE jdbc;

CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password TEXT NOT NULL
);







