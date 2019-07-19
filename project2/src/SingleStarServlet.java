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
 * Servlet implementation class SingleStarServlet
 */
@WebServlet(name = "SingleStarServlet", urlPatterns="/star")
public class SingleStarServlet extends HttpServlet {
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
        out.println("<html><head><title>Star Page</title>");
        
        // Building table style
        out.println("<style>");
        
        out.println("table {border-collapse: collapse; width: 100%;}");
        out.println("th, td {border: 1px solid #dddddd; text-align: left; padding: 5px;}");
        
        // End of page style
        out.println("</style>");
        
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
            
            // Construct star query
            String query = String.format("SELECT * FROM stars "
					 				   + "JOIN (SELECT DISTINCT starId, GROUP_CONCAT(id SEPARATOR ', ') as movies FROM (stars_in_movies JOIN movies on stars_in_movies.movieId = movies.id) GROUP BY starId) as m on stars.id = m.starId "
					 				   + "WHERE id LIKE '%s'", id);
            
            // Get star entry from moviedb
            ResultSet star = statement.executeQuery(query);
            star.next();
            
            // Get data from star entry
            String s_name = star.getString("name");
            String s_dob = star.getString("birthYear");
            String s_movies = star.getString("movies");
            if (s_dob == null) {
            	s_dob = "N/A";
            }
            
            // Split Movie IDs into array
            String[] movieIDs = s_movies.split(", ");
            
            // Building page body
            out.println("<body>");
            out.println(String.format("<h1>%s</h1>", s_name));
            out.println(String.format("<h3>Born:</h3><p>%s</p>", s_dob));
            
            out.println("<h3>Stars in:</h3>");
            for (String m : movieIDs) {
	            ResultSet movie = statement.executeQuery(String.format("SELECT title FROM movies WHERE id LIKE '%s'", m));
	            movie.next();
	            out.println(String.format("<a href='movie?id=%s'>%s</a><br><br>", m, movie.getString("title")));
	            movie.close();
            }
            
            out.println("</body>");
            
            // Close all structures
            star.close();
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