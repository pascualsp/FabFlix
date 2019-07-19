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
 * A servlet that takes input from a html <form> and talks to MySQL moviedb,
 * generates output as a html <table>
 */

// Declaring a WebServlet called SearchServlet, which maps to url "/browse"
@WebServlet(name = "BrowseServlet", urlPatterns = "/browse")
public class BrowseServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    
    // Use http GET
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html");    // Response mime type
        
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Building page head with title
        out.println("<html><head><title>Browse Results</title>");
        
        // Building table style
        out.println("<style>");
        
        out.println("table {border-collapse: collapse; width: 100%;}");
        out.println("th, td {text-align: left; padding: 5px;}");
        out.println("tr:nth-child(even) {background-color: #cddddd;}");
        
        // End of page style
        out.println("</style>");
        
        // End of page head
        out.println("</head>");

        // Building page body
        out.println("<body>");
        out.println("<a href=\"index.html\">Home</a>");
        out.println("<a href=\"cart\">My Cart</a>");
        out.println("<a href='movie-browse'>Return to Browse</a>");
        out.println("<h1>Browse Results</h1>");


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
            Statement statement_c = dbCon.createStatement();
            Statement statement_s = dbCon.createStatement();

            // Retrieve parameter "name" from the http request, which refers to the value of <input name="name"> in search.html
            //String name = request.getParameter("name");
            String id = request.getParameter("id");
            String s_offset = request.getParameter("offset");
            int offset = Integer.parseInt(s_offset);
            String s_limit = request.getParameter("limit");
            int limit = Integer.parseInt(s_limit);
            String order_by = request.getParameter("order");
            String order_dir = request.getParameter("ad");
            
            // Building page limit prompt
            out.println("<form ACTION='browse' METHOD='GET'>"
            			+ "<input TYPE='HIDDEN' name='id' value='" + id + "'>"
						+ "<input TYPE='HIDDEN' name='offset' value='" + s_offset + "'>"
            			+ "Movies displayed per page: <select name='limit'>"
            				+ "<option value='5'>5</option>"
	            		  	+ "<option value='10'>10</option>"
	            		  	+ "<option value='20'>20</option>"
	            		  	+ "<option value='50'>50</option>"
	            		  	+ "<option value='100'>100</option>"
	            		+ "</select>"
	            		+ "<input TYPE='HIDDEN' name='order' value='" + order_by + "'>"
	            		+ "<input TYPE='HIDDEN' name='ad' value='" + order_dir + "'>"
	            	  + "<input type='SUBMIT' value='Go'>"
	            	  + "</form>");
            
            // Build sorting query if applicable
            String sort_query = "";
            
            if (order_by.equals("title")) {
            	sort_query = "ORDER BY movies.title " + order_dir + " ";
            }
            else if (order_by.equals("rating")) {
            	sort_query = "ORDER BY ratings.rating " + order_dir + " ";
            }
            
            
            // Generate a SQL query
            String query = "";
            String query_count = "";
            if (id.length() > 1) {
            	/*
            	query = String.format("SELECT * from movies " + 
   					 "JOIN genres_in_movies ON movies.id = genres_in_movies.movieId " +
   					 "JOIN genres ON genres.id = genres_in_movies.genreId " + 
   					 "WHERE genres.name = \"%s\"", id);
            	*/
            	
                query = String.format("SELECT * FROM movies "
    					 			+ "LEFT OUTER JOIN ratings on movies.id = ratings.movieId "
    					 			+ "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(name SEPARATOR ', ') as genres FROM (genres_in_movies JOIN genres on genres_in_movies.genreId = genres.id) GROUP BY movieId) as g on movies.id = g.movieId "
					 				+ "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(id SEPARATOR ', ') as stars FROM (stars_in_movies JOIN stars on stars_in_movies.starId = stars.id) GROUP BY movieId) as s on movies.id = s.movieId "
					 				+ "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(name SEPARATOR ', ') as star_names FROM (stars_in_movies JOIN stars on stars_in_movies.starId = stars.id) GROUP BY movieId) as sn on movies.id = sn.movieId "
    					 			+ "WHERE genres LIKE '%%%s%%' "
    					 			+ sort_query
    					 			+ "LIMIT %d, %d", id, offset, limit);
    					 			//+ "WHERE g.name = %s%s%s", "\"", id, "\"");
                
                query_count = String.format("SELECT COUNT(*) as total FROM movies "
						 			+ "LEFT OUTER JOIN ratings on movies.id = ratings.movieId "
						 			+ "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(name SEPARATOR ', ') as genres FROM (genres_in_movies JOIN genres on genres_in_movies.genreId = genres.id) GROUP BY movieId) as g on movies.id = g.movieId "
					 				+ "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(id SEPARATOR ', ') as stars FROM (stars_in_movies JOIN stars on stars_in_movies.starId = stars.id) GROUP BY movieId) as s on movies.id = s.movieId "
					 				+ "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(name SEPARATOR ', ') as star_names FROM (stars_in_movies JOIN stars on stars_in_movies.starId = stars.id) GROUP BY movieId) as sn on movies.id = sn.movieId "
						 			+ "WHERE genres LIKE '%%%s%%' ", id);
                
            }
            else {
            	query = String.format("SELECT * FROM movies "
		 				   			+ "LEFT OUTER JOIN ratings on movies.id = ratings.movieId "
			 				   		+ "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(name SEPARATOR ', ') as genres FROM (genres_in_movies JOIN genres on genres_in_movies.genreId = genres.id) GROUP BY movieId) as g on movies.id = g.movieId "
					 				+ "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(id SEPARATOR ', ') as stars FROM (stars_in_movies JOIN stars on stars_in_movies.starId = stars.id) GROUP BY movieId) as s on movies.id = s.movieId "
					 				+ "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(name SEPARATOR ', ') as star_names FROM (stars_in_movies JOIN stars on stars_in_movies.starId = stars.id) GROUP BY movieId) as sn on movies.id = sn.movieId "
		 				   			+ "WHERE title LIKE '%s%s' "
		 				   			+ sort_query
		 				   			+ "LIMIT %d, %d ", id, "%", offset, limit);
            	
            	query_count = String.format("SELECT COUNT(*) as total FROM movies "
				   			+ "LEFT OUTER JOIN ratings on movies.id = ratings.movieId "
	 				   		+ "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(name SEPARATOR ', ') as genres FROM (genres_in_movies JOIN genres on genres_in_movies.genreId = genres.id) GROUP BY movieId) as g on movies.id = g.movieId "
			 				+ "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(id SEPARATOR ', ') as stars FROM (stars_in_movies JOIN stars on stars_in_movies.starId = stars.id) GROUP BY movieId) as s on movies.id = s.movieId "
			 				+ "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(name SEPARATOR ', ') as star_names FROM (stars_in_movies JOIN stars on stars_in_movies.starId = stars.id) GROUP BY movieId) as sn on movies.id = sn.movieId "
				   			+ "WHERE title LIKE '%s%s'", id, "%");
            	
            }
             
            // Perform the query
            ResultSet rs = statement.executeQuery(query);
            ResultSet rsc = statement_c.executeQuery(query_count);
            
            // Browse by title
            if (id.length() == 1) {
	            out.println("<th><a href=browse?id=0&offset=0&limit=5&order=&ad=>0</a></th> | <th><a href=browse?id=1&offset=0&limit=5&order=&ad=>1</a></th> | <th><a href=browse?id=2&offset=0&limit=5&order=&ad=>2</a></th> | "
	            + "<th><a href=browse?id=3&offset=0&limit=5&order=&ad=>3</a></th> | <th><a href=browse?id=4&offset=0&limit=5&order=&ad=>4</a></th> | <th><a href=browse?id=5&offset=0&limit=5&order=&ad=>5</a></th> | "
	    		+ "<th><a href=browse?id=6&offset=0&limit=5&order=&ad=>6</a></th> | <th><a href=browse?id=7&offset=0&limit=5&order=&ad=>7</a></th> | <th><a href=browse?id=8&offset=0&limit=5&order=&ad=>8</a></th> | "
	    		+ "<th><a href=browse?id=9&offset=0&limit=5&order=&ad=>9</a></th> | <th><a href=browse?id=A&offset=0&limit=5&order=&ad=>A</a></th> | <th><a href=browse?id=B&offset=0&limit=5&order=&ad=>B</a></th> | "
	    		+ "<th><a href=browse?id=C&offset=0&limit=5&order=&ad=>C</a></th> | <th><a href=browse?id=D&offset=0&limit=5&order=&ad=>D</a></th> | <th><a href=browse?id=E&offset=0&limit=5&order=&ad=>E</a></th> | "
	    		+ "<th><a href=browse?id=F&offset=0&limit=5&order=&ad=>F</a></th> | <th><a href=browse?id=G&offset=0&limit=5&order=&ad=>G</a></th> | <th><a href=browse?id=H&offset=0&limit=5&order=&ad=>H</a></th> | "
	    		+ "<th><a href=browse?id=I&offset=0&limit=5&order=&ad=>I</a></th> | <th><a href=browse?id=J&offset=0&limit=5&order=&ad=>J</a></th> | <th><a href=browse?id=K&offset=0&limit=5&order=&ad=>K</a></th> | "
	    		+ "<th><a href=browse?id=L&offset=0&limit=5&order=&ad=>L</a></th> | <th><a href=browse?id=M&offset=0&limit=5&order=&ad=>M</a></th> | <th><a href=browse?id=N&offset=0&limit=5&order=&ad=>N</a></th> | "
	    		+ "<th><a href=browse?id=O&offset=0&limit=5&order=&ad=>O</a></th> | <th><a href=browse?id=P&offset=0&limit=5&order=&ad=>P</a></th> | <th><a href=browse?id=Q&offset=0&limit=5&order=&ad=>Q</a></th> | "
	    		+ "<th><a href=browse?id=R&offset=0&limit=5&order=&ad=>R</a></th> | <th><a href=browse?id=S&offset=0&limit=5&order=&ad=>S</a></th> | <th><a href=browse?id=T&offset=0&limit=5&order=&ad=>T</a></th> | "
	    		+ "<th><a href=browse?id=U&offset=0&limit=5&order=&ad=>U</a></th> | <th><a href=browse?id=V&offset=0&limit=5&order=&ad=>V</a></th> | <th><a href=browse?id=W&offset=0&limit=5&order=&ad=>W</a></th> | "
	    		+ "<th><a href=browse?id=X&offset=0&limit=5&order=&ad=>X</a></th> | <th><a href=browse?id=Y&offset=0&limit=5&order=&ad=>Y</a></th> | <th><a href=browse?id=Z&offset=0&limit=5&order=&ad=>Z</a></th>"
	    		);
	            out.println("<br>");
	            out.println("<br>");
            }
            
            // Create hidden frame so add to cart keeps user on page
            out.println(String.format("<iframe style= \"width:0; height:0; border:0; border:none\" name=\"hiddenFrame\" class=\"hide\"></iframe>"));

            
            // Create a html <table>
            out.println("<table>");

            // Iterate through each row of rs and create a table row <tr>
            int count = 0;
            
            String new_order_dir = "";
            if (order_dir.equals("DESC")) {
            	new_order_dir = "ASC";
            }
            else {
            	new_order_dir = "DESC";
            }
            		
            out.println("<tr><td></td><td><b>ID</b></td>"
            		+ "<td><a href='browse?id=" + id + "&offset=0&limit=" + s_limit + "&order=title&ad=" + new_order_dir + "'><b>Title</b></a></td>"
            		+ "<td><b>Year</b></td>"
            		+ "<td><b>Director</b></td>"
            		+ "<td><b>Genre(s)</b></td>"
            		+ "<td><b>Stars</b></td>"
            		+ "<td><a href='browse?id=" + id + "&offset=0&limit=" + s_limit + "&order=rating&ad=" + new_order_dir + "'><b>Rating</b></a></td>");
            
            while (rs.next()) {
            	count++;
                String m_ID = rs.getString("id");
                String m_Title = rs.getString("title");
                String m_Year = rs.getString("year");
                String m_Dir = rs.getString("director");
                String m_Genres = rs.getString("genres");
                String m_Stars = rs.getString("stars");
                String m_Rating = rs.getString("rating");
                
                // Split Stars into array
                String[] starIDs = m_Stars.split(", ");
                
                ResultSet star_q = statement_s.executeQuery(String.format("SELECT name FROM stars WHERE id LIKE '%s'", starIDs[0]));
                star_q.next();
                String star_entry = String.format("<a href='star?id=%s'>%s</a>", starIDs[0], star_q.getString("name"));
                star_q.close();
                if (starIDs.length > 1) {
	                for (int i = 1; i < starIDs.length; i++) {
	                	ResultSet star_q2 = statement_s.executeQuery(String.format("SELECT name FROM stars WHERE id LIKE '%s'", starIDs[i]));
	                    star_q2.next();
	                    star_entry += String.format(", <a href='star?id=%s'>%s</a>", starIDs[i], star_q2.getString("name"));
	                    star_q2.close();
	                }
                }
                
                
                
                out.println(String.format("<tr>"
                		+ "<td><form ACTION='' METHOD='GET' target=\"hiddenFrame\">"
                		+ "<input TYPE='HIDDEN' name='id' value='" + id + "'>"
                		+ "<input TYPE='HIDDEN' name=movie value='%s'>"
                		+ "<input TYPE='HIDDEN' name='offset' value='" + s_offset + "'>"
						+ "<input TYPE='HIDDEN' name='limit' value='" + limit + "'>"
            			+ "<input TYPE='HIDDEN' name='order' value='" + order_by + "'>"
	            		+ "<input TYPE='HIDDEN' name='ad' value='" + order_dir + "'>"
	            		+ "<input TYPE='SUBMIT' value='Add to Cart'>"
                		+ "</form></td>"
                		
                		+ "<td>%s</td><td><a href='movie?id=%s&movie='>%s</a></td><td>%s</td><td>%s</td><td>%s</td>"
                		+ "<td>%s</td><td>%s</td></tr>", m_ID, 
                		m_ID, m_ID, m_Title, m_Year, m_Dir, m_Genres, star_entry, m_Rating));
                
            }
            out.println("</table>");
            
            // Get total rows
            rsc.next();
            String s_total = rsc.getString("total");
            int total = Integer.parseInt(s_total);
            
            out.println("<br>");
            
            if (offset > 0) {
            	// Build Previous page query
            	int new_offset = Math.max(offset-limit, 0);
	            String pp_query = String.format("<form ACTION='browse' METHOD='GET'>"
	            		+ "<input TYPE='HIDDEN' name='id' value=%s>"
						+ "<input TYPE='HIDDEN' name='offset' value=%d>"
	        			+ "<input TYPE='HIDDEN' name='limit' value=%d>"
						+ "<input TYPE='HIDDEN' name='order' value=%s>"
	        			+ "<input TYPE='HIDDEN' name='ad' value=%s>"
	    			  + "<input type='SUBMIT' value='Previous Page'>"
	    			  + "</form>", id, new_offset, limit, order_by, order_dir);
	            
	            // Previous page button
	            out.print(pp_query);
            }
            
            if (offset+count < total) {
            	// Build Next page query
	            String np_query = String.format("<form ACTION='browse' METHOD='GET'>"
	            		+ "<input TYPE='HIDDEN' name='id' value=%s>"
						+ "<input TYPE='HIDDEN' name='offset' value=%d>"
	        			+ "<input TYPE='HIDDEN' name='limit' value=%d>"
	        			+ "<input TYPE='HIDDEN' name='order' value=%s>"
	        			+ "<input TYPE='HIDDEN' name='ad' value=%s>"
	    			  + "<input type='SUBMIT' value='Next Page'>"
	    			  + "</form>", id, offset+limit, limit, order_by, order_dir);
	            
	            // Next page button
	            out.println(np_query);
            }
            
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
            //rs2.close();
            statement.close();
            dbCon.close();

        } catch (Exception ex) {

            // Output Error Massage to html
            out.println(String.format("<html><head><title>MovieDB: Error</title></head>\n<body><p>SQL error in doGet: %s</p></body></html>", ex.getMessage()));
            return;
        }
        out.close();
    }
}