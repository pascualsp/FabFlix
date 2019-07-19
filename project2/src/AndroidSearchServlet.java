import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
//import java.sql.Statement;
import java.util.List;

/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedb,
 * generates output as a html <table>
 */

// Declaring a WebServlet called SearchServlet, which maps to url "/search"
@WebServlet(name = "AndroidSearchServlet", urlPatterns = "/android-search")
public class AndroidSearchServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;


    // Use http GET
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

    	// Response mime type
        response.setContentType("text/html");

        try {

            // Create a new connection to database
            Connection dbCon = dataSource.getConnection();

            // Retrieve parameters from the http request
            String title = request.getParameter("title");
            String s_offset = request.getParameter("offset");
            int offset = Integer.parseInt(s_offset);
            String s_limit = request.getParameter("limit");
            int limit = Integer.parseInt(s_limit);

            // Build title query
            String title_query = "";
            String[] queries = title.split(" ");
            title_query += "+" + queries[0] + "*";
            if (queries.length > 1) { 
            	for (int i = 1; i < queries.length; i++){
                	title_query += " +" + queries[i] + "*";
                }
            }
            
            // Generate a SQL query
            String query = String.format("SELECT * FROM movies "
					 				   + "LEFT OUTER JOIN ratings on movies.id = ratings.movieId "
					 				   + "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(name SEPARATOR ', ') as genres FROM (genres_in_movies JOIN genres on genres_in_movies.genreId = genres.id) GROUP BY movieId) as g on movies.id = g.movieId "
					 				   + "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(id SEPARATOR ', ') as stars FROM (stars_in_movies JOIN stars on stars_in_movies.starId = stars.id) GROUP BY movieId) as s on movies.id = s.movieId "
					 				   + "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(name SEPARATOR ', ') as star_names FROM (stars_in_movies JOIN stars on stars_in_movies.starId = stars.id) GROUP BY movieId) as sn on movies.id = sn.movieId "
					 				   + "WHERE MATCH (title) AGAINST ('" + title_query
					 				   + "' IN BOOLEAN MODE) "
					 				   + "LIMIT %d, %d ", offset, limit);

            // Create and perform the query as a PreparedStatement
            PreparedStatement pStatement = dbCon.prepareStatement(query);
//            pStatement.setString(1, "%" + title + "%");
                        
            ResultSet rs = pStatement.executeQuery();
            
            JsonObject responseJsonObject = new JsonObject();
            
            while (rs.next()) {
            	JsonObject entry = new JsonObject();
            	
                String m_ID = rs.getString("id");
                String m_Title = rs.getString("title");
                String m_Year = rs.getString("year");
                String m_Dir = rs.getString("director");
                String m_Genres = rs.getString("genres");
                String m_Stars = rs.getString("star_names");
//                String m_Rating = rs.getString("rating");
                
                entry.addProperty("title", m_Title);
                entry.addProperty("year", m_Year);
                entry.addProperty("director", m_Dir);
                entry.addProperty("genres", m_Genres);
                entry.addProperty("stars", m_Stars);
                
                responseJsonObject.add(m_ID, entry);
                
                
            }
            
            response.getWriter().write(responseJsonObject.toString());
            
            // should check if request.getSession(false) might return null!
            User myUser = (User) request.getSession(false).getAttribute("user");
            String movieId = request.getParameter("movie");
            if (movieId != null  && movieId != "") {
            	if (! myUser.getCart().contains(movieId)){
            		myUser.addToCart(movieId);
            		myUser.sortCart();
            		myUser.addToQuantities(myUser.posCart(movieId), 1);
            	}
            }
            
            // Close all structures
            rs.close();
            pStatement.close();
            dbCon.close();

        } catch (Exception ex) {
        	
            return;
        }
        
    }
}