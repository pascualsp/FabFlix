DROP PROCEDURE IF EXISTS add_movie;

DELIMITER $$

CREATE PROCEDURE add_movie (OUT message1 VARCHAR(100), OUT message2 VARCHAR(100), OUT message3 VARCHAR(100), OUT message4 VARCHAR(100), OUT message5 VARCHAR(100), IN m_id VARCHAR(10), IN mtitle VARCHAR(100), IN myear INT, IN mdirector VARCHAR(100), IN s_id VARCHAR(10), IN sname VARCHAR(100), IN syear INT, IN mgenre VARCHAR(32))
BEGIN
	IF EXISTS (SELECT * FROM movies WHERE movies.title = mtitle AND movies.year = myear AND movies.director = mdirector) THEN
		SELECT 'Movie already exists' INTO message1;
        SELECT '' INTO message2;
        SELECT '' INTO message3;
        SELECT '' INTO message4;
        SELECT '' INTO message5;
	ELSE
		INSERT INTO movies VALUES(m_id,mtitle,myear,mdirector);
        
        IF EXISTS (SELECT * FROM stars WHERE stars.name = sname AND stars.birthYear = syear) THEN
			INSERT INTO stars_in_movies VALUES((SELECT id FROM stars WHERE stars.name = sname AND stars.birthYear = syear LIMIT 1),m_id);
			SELECT 'Star already exists' INTO message2;
            SELECT 'Linked Star with Movie Successfully' INTO message4;
        ELSE
			IF (syear = 0) THEN
				INSERT INTO stars (id, name) VALUES(s_id, sname);
                SELECT 'Added New Star Successfully' INTO message2;
            ELSE
            	INSERT INTO stars (id, name, birthYear) VALUES(s_id,sname,syear);
                SELECT 'Added New Star Successfully' INTO message2;
			END IF;
			INSERT INTO stars_in_movies VALUES(s_id,m_id);
            SELECT 'Linked Star with Movie Successfully' INTO message4;
		END IF;
        
        IF EXISTS (SELECT * FROM genres WHERE genres.name = mgenre) THEN
			INSERT INTO genres_in_movies VALUES((SELECT id FROM genres WHERE genres.name = mgenre LIMIT 1),m_id);
            SELECT 'Genre already exists' INTO message3;
            SELECT 'Linked Genre with Movie Successfully' INTO message5;
		ELSE
			INSERT INTO genres VALUES(NULL,mgenre);
            SELECT 'Added New Genre Successfully' INTO message3;
			INSERT INTO genres_in_movies VALUES((SELECT id FROM genres WHERE genres.name = mgenre LIMIT 1),m_id);
            SELECT 'Linked Genre with Movie Successfully' INTO message5;
		END IF;
		SELECT 'Added Movie Successfully' INTO message1;
	END IF;
END
$$
DELIMITER ;