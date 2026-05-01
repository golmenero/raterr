-- Migrate data from movie (singular) to movies (plural) if movie table exists
INSERT OR IGNORE INTO movies (id, tmdb_id, title, original_title, overview, release_date, release_year, poster_path, tmdb_vote_average)
SELECT id, tmdb_id, title, original_title, overview, release_date, release_year, poster_path, tmdb_vote_average
FROM movie
WHERE EXISTS (SELECT 1 FROM sqlite_master WHERE type='table' AND name='movie');

-- Migrate data from rating (singular) to ratings (plural) if rating table exists
-- Mapping Spanish columns to English
INSERT OR IGNORE INTO ratings (id, movie_id, directing, cinematography, acting, soundtrack, screenplay, created_at_epoch_ms)
SELECT id, movie_id, direccion, fotografia, actuacion, banda_sonora, guion, created_at_epoch_ms
FROM rating
WHERE EXISTS (SELECT 1 FROM sqlite_master WHERE type='table' AND name='rating');

-- Drop old tables if they existed
DROP TABLE IF EXISTS rating;
DROP TABLE IF EXISTS movie;
