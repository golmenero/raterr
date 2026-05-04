CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(500) NOT NULL,
    created_at_epoch_ms BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS movies (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tmdb_id INTEGER NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    original_title VARCHAR(255),
    overview TEXT,
    release_date VARCHAR(20),
    release_year INTEGER,
    poster_path VARCHAR(255),
    tmdb_vote_average REAL,
    genres VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS tv_shows (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tmdb_id INTEGER NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255),
    overview TEXT,
    first_air_date VARCHAR(20),
    first_air_year INTEGER,
    poster_path VARCHAR(255),
    tmdb_vote_average REAL,
    genres VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS ratings (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    directing REAL NOT NULL,
    cinematography REAL NOT NULL,
    acting REAL NOT NULL,
    soundtrack REAL NOT NULL,
    screenplay REAL NOT NULL,
    created_at_epoch_ms BIGINT NOT NULL,
    FOREIGN KEY (movie_id) REFERENCES movies(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS tv_ratings (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tv_show_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    directing REAL NOT NULL,
    cinematography REAL NOT NULL,
    acting REAL NOT NULL,
    soundtrack REAL NOT NULL,
    screenplay REAL NOT NULL,
    created_at_epoch_ms BIGINT NOT NULL,
    FOREIGN KEY (tv_show_id) REFERENCES tv_shows(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS follows (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    content_tmdb_id INTEGER NOT NULL,
    created_at_epoch_ms BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE(user_id, content_type, content_tmdb_id)
);