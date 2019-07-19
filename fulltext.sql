#ALTER TABLE movies ADD FULLTEXT (title);

#SELECT title FROM movies WHERE MATCH (title) AGAINST ('+baby*' IN BOOLEAN MODE) AND (MATCH (title) AGAINST ('+the*' IN BOOLEAN MODE) OR title LIKE 'the%') LIMIT 10;

#SELECT title FROM movies WHERE MATCH (title) AGAINST ('+baby*' IN BOOLEAN MODE) AND (MATCH (title) AGAINST ('+of*' IN BOOLEAN MODE) OR title LIKE 'of%') LIMIT 10;

SELECT title FROM movies WHERE (title LIKE 'bun%' OR SIMILARTO(title, 'bnya', 2)) LIMIT 10;
