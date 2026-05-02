CREATE TABLE IF NOT EXISTS migrations (
    name VARCHAR(255) PRIMARY KEY NOT NULL,
    executed INTEGER DEFAULT 0,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS movies (
    tmdb_id INTEGER PRIMARY KEY NOT NULL,
    title VARCHAR(255) NOT NULL,
    original_title VARCHAR(255),
    overview TEXT,
    release_date VARCHAR(20),
    release_year INTEGER,
    poster_path VARCHAR(255),
    tmdb_vote_average DOUBLE
);

CREATE TABLE IF NOT EXISTS ratings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tmdb_id INTEGER NOT NULL,
    username VARCHAR(50) NOT NULL,
    directing DOUBLE NOT NULL,
    cinematography DOUBLE NOT NULL,
    acting DOUBLE NOT NULL,
    soundtrack DOUBLE NOT NULL,
    screenplay DOUBLE NOT NULL,
    created_at_epoch_ms INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) PRIMARY KEY NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
