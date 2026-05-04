CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(500) NOT NULL,
    created_at_epoch_ms BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS movies (
    tmdb_id INTEGER PRIMARY KEY,
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
    tmdb_id INTEGER PRIMARY KEY,
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
    movie_tmdb_id INTEGER NOT NULL,
    user_username VARCHAR(50),
    directing REAL NOT NULL,
    cinematography REAL NOT NULL,
    acting REAL NOT NULL,
    soundtrack REAL NOT NULL,
    screenplay REAL NOT NULL,
    created_at_epoch_ms BIGINT NOT NULL,
    FOREIGN KEY (movie_tmdb_id) REFERENCES movies(tmdb_id),
    FOREIGN KEY (user_username) REFERENCES users(username)
);

CREATE INDEX IF NOT EXISTS idx_ratings_movie_tmdb_id ON ratings(movie_tmdb_id);
CREATE INDEX IF NOT EXISTS idx_ratings_user_username ON ratings(user_username);

CREATE TABLE IF NOT EXISTS tv_ratings (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tv_show_tmdb_id INTEGER NOT NULL,
    user_username VARCHAR(50),
    directing REAL NOT NULL,
    cinematography REAL NOT NULL,
    acting REAL NOT NULL,
    soundtrack REAL NOT NULL,
    screenplay REAL NOT NULL,
    created_at_epoch_ms BIGINT NOT NULL,
    FOREIGN KEY (tv_show_tmdb_id) REFERENCES tv_shows(tmdb_id),
    FOREIGN KEY (user_username) REFERENCES users(username)
);

CREATE INDEX IF NOT EXISTS idx_tv_ratings_tv_show_tmdb_id ON tv_ratings(tv_show_tmdb_id);
CREATE INDEX IF NOT EXISTS idx_tv_ratings_user_username ON tv_ratings(user_username);

CREATE TABLE IF NOT EXISTS follows (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_username VARCHAR(50) NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    content_tmdb_id INTEGER NOT NULL,
    created_at_epoch_ms BIGINT NOT NULL,
    FOREIGN KEY (user_username) REFERENCES users(username),
    UNIQUE(user_username, content_type, content_tmdb_id)
);

CREATE INDEX IF NOT EXISTS idx_follows_user_username ON follows(user_username);
