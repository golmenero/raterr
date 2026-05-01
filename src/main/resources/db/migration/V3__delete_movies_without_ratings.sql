DELETE FROM movies WHERE id NOT IN (SELECT DISTINCT movie_id FROM ratings);
