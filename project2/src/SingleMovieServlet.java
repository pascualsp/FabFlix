import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Servlet implementation class SingleMovieServlet
 */
@WebServlet(name = "SingleMovieServlet", urlPatterns="/movie")
public class SingleMovieServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	// Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
       

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		// Response mime type
        response.setContentType("text/html");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        
        // Initialize page head with title
        out.println("<html><head><title>Movie Page</title>");
        
//        // Building table style
//        out.println("<style>");
//        
//        out.println("table {border-collapse: collapse; width: 100%;}");
//        out.println("th, td {border: 1px solid #dddddd; text-align: left; padding: 5px;}");
//        
//        // End of page style
//        out.println("</style>");
        
        // End of page head
        out.println("</head>");
        
        // Building page body
        out.println("<body>");
        out.println("<a href=\"index.html\">Home</a>");
        out.println("<a href=\"cart\">My Cart</a>");
        
        try {
        	Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
                out.println("envCtx is NULL");
            
            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/moviedb");

            // Create a new connection to database
            Connection dbCon = ds.getConnection();

            // Declare a new statement
            Statement statement = dbCon.createStatement();
            
            // Get movie id
            String id = request.getParameter("id");
            
            // Construct 
            String query = String.format("SELECT * FROM movies "
	 				   + "LEFT OUTER JOIN ratings on movies.id = ratings.movieId "
	 				   + "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(name SEPARATOR ', ') as genres FROM (genres_in_movies JOIN genres on genres_in_movies.genreId = genres.id) GROUP BY movieId) as g on movies.id = g.movieId "
	 				   + "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(id SEPARATOR ', ') as stars FROM (stars_in_movies JOIN stars on stars_in_movies.starId = stars.id) GROUP BY movieId) as s on movies.id = s.movieId "
	 				   + "WHERE id LIKE '%s' ", id);
            
            // Get movie entry from moviedb
            ResultSet movie = statement.executeQuery(query);
            movie.next();
            
            //
            out.println(String.format("<iframe style= \"width:0; height:0; border:0; border:none\" name=\"hiddenFrame\" class=\"hide\"></iframe>"));
            
            // Get data from movie entry
            String m_Id = movie.getString("id");
            String m_Title = movie.getString("title");
            String m_Year = movie.getString("year");
            String m_Dir = movie.getString("director");
            String m_Genres = movie.getString("genres");
            String m_Stars = movie.getString("stars");
            String m_Rating = movie.getString("rating");
            
            // Split Genre and Stars into array
            String[] genres = m_Genres.split(", ");
            String[] starIDs = m_Stars.split(", ");
            
            out.println("<br>");
            out.println("<br>");
            
            // Add cart button
            out.println(String.format("<tr>"
            		+ "<td><form ACTION='' METHOD='GET' target=\"hiddenFrame\">"
            		+ "<input TYPE='HIDDEN' name='id' value='" + id + "'>"
            		+ "<input TYPE='HIDDEN' name='movie' value='" + id + "'>"
            		+ "<input TYPE='SUBMIT' value='Add to Cart'>"
            		+ "</form></td></tr>"));
            
            out.println(String.format("<h1>%s</h1>", m_Title));
            out.println(String.format("<p><b>ID: </b>%s</p>", m_Id));
            out.println(String.format("<h3>Rating:</h3><p>%s</p>", m_Rating));
            out.println(String.format("<h3>Released:</h3><p>%s</p>", m_Year));
            out.println(String.format("<h3>Directed by:</h3><p>%s</p>", m_Dir));
            
            out.println("<h3>Genre:</h3>");
            for (String genre : genres) {
            	out.println(String.format("<a href=\"browse?id=%s&offset=0&limit=5&order=&ad=\">%s</a><br><br>", genre, genre));
            }
            
            out.println("<h3>Starring:</h3>");
            for (String s : starIDs) {
	            ResultSet star = statement.executeQuery(String.format("SELECT name FROM stars WHERE id LIKE '%s'", s));
	            star.next();
	            out.println(String.format("<a href='star?id=%s'>%s</a><br><br>", s, star.getString("name")));
	            star.close();
            }
            
            out.println("</body>");
            
            
            // When Add to Cart is Clicked; should check if request.getSession(false) might return null!
            User myUser = (User) request.getSession(false).getAttribute("user");
            String movieId = request.getParameter("movie");
            if (movieId != null && movieId != "") {
            	if (! myUser.getCart().contains(movieId)){
            		myUser.addToCart(movieId);
            		myUser.sortCart();
            		myUser.addToQuantities(myUser.posCart(movieId), 1);
            	}
            }
            
            // Close all structures
            movie.close();
            statement.close();
            dbCon.close();
            
        }
        
        catch (Exception ex) {
	        // Output Error Massage to html
	        out.println(String.format("<html><head><title>MovieDB: Error</title></head>\n<body><p>SQL error in doGet: %s</p></body></html>", ex.getMessage()));
	        return;
	    }
        
        out.println("</html>");
        
        out.close();
        
	}


}