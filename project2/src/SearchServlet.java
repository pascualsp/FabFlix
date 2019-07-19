import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
//import java.sql.Statement;
//import java.util.List;

/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedb,
 * generates output as a html <table>
 */

// Declaring a WebServlet called SearchServlet, which maps to url "/search"
@WebServlet(name = "SearchServlet", urlPatterns = "/search")
public class SearchServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;


    // Use http GET
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
    	
    	// Start calculation for TS
    	long TS_start = System.currentTimeMillis();

    	// Response mime type
        response.setContentType("text/html");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Initialize page head with title
        out.println("<html><head><title>Search Results</title>");
        
        // Building table style
        out.println("<style>");
        
        out.println("table {border-collapse: collapse; width: 100%;}");
        out.println("th, td{text-align: left; padding: 5px;}");
        out.println("tr:nth-child(even) {background-color: #cddddd;}");
        
        // End of page style
        out.println("</style>");
        
        // End of page head
        out.println("</head>");

        // Building page body
        out.println("<body>");
        out.println("<a href=\"index.html\">Home</a>");
        out.println("<a href=\"cart\">My Cart</a>");
        out.println("<h1>Search Results</h1>");

        try {
        	// the following few lines are for connection pooling
            // Obtain our environment naming context

            Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
                out.println("envCtx is NULL");
            
            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/moviedb");

            // Create a new connection to database
            Connection dbCon = ds.getConnection();

            // Retrieve parameters from the http request
            String title = request.getParameter("title");
            String s_offset = request.getParameter("offset");
            int offset = Integer.parseInt(s_offset);
            String s_limit = request.getParameter("limit");
            int limit = Integer.parseInt(s_limit);
            String order_by = request.getParameter("order");
            String order_dir = request.getParameter("ad");
            
	        // Building page limit prompt
            out.println("<form ACTION='search' METHOD='GET'>"
						+ "<input TYPE='HIDDEN' name='title' value='" + title + "'>"
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
            
            // Build title query
            String title_query = "";
            String[] queries = title.split(" ");
            title_query += "+" + queries[0] + "*";
            if (queries.length > 1) { 
            	for (int i = 1; i < queries.length; i++){
                	title_query += " +" + queries[i] + "*";
                }
            }
            
            // Checkpoint
            // Generate a SQL query
            String query = String.format("SELECT * FROM movies "
					 				   + "LEFT OUTER JOIN ratings on movies.id = ratings.movieId "
					 				   + "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(name SEPARATOR ', ') as genres FROM (genres_in_movies JOIN genres on genres_in_movies.genreId = genres.id) GROUP BY movieId) as g on movies.id = g.movieId "
					 				   + "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(id SEPARATOR ', ') as stars FROM (stars_in_movies JOIN stars on stars_in_movies.starId = stars.id) GROUP BY movieId) as s on movies.id = s.movieId "
					 				   + "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(name SEPARATOR ', ') as star_names FROM (stars_in_movies JOIN stars on stars_in_movies.starId = stars.id) GROUP BY movieId) as sn on movies.id = sn.movieId "
					 				   + "WHERE MATCH (title) AGAINST (?"
					 				   + " IN BOOLEAN MODE) "
					 				   + sort_query
					 				   + "LIMIT %d, %d", offset, limit);
            //out.println(query);
            String query_count = "SELECT COUNT(*) as total FROM movies "
					 		   + "LEFT OUTER JOIN ratings on movies.id = ratings.movieId "
					 		   + "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(name SEPARATOR ', ') as genres FROM (genres_in_movies JOIN genres on genres_in_movies.genreId = genres.id) GROUP BY movieId) as g on movies.id = g.movieId "
					 		   + "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(id SEPARATOR ', ') as stars FROM (stars_in_movies JOIN stars on stars_in_movies.starId = stars.id) GROUP BY movieId) as s on movies.id = s.movieId "
					 		   + "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(name SEPARATOR ', ') as star_names FROM (stars_in_movies JOIN stars on stars_in_movies.starId = stars.id) GROUP BY movieId) as sn on movies.id = sn.movieId "
					 		   + "WHERE MATCH (title) AGAINST (?"
			 				   + " IN BOOLEAN MODE) ";

            // Create and perform the query as a PreparedStatement
            PreparedStatement pStatement = dbCon.prepareStatement(query);
            pStatement.setString(1, title_query);
            PreparedStatement pStatement_c = dbCon.prepareStatement(query_count);
            pStatement_c.setString(1, title_query);
            //pStatement_c.setString(1, "%" + title + "%");
            
            // Start calculation for TJ
            long TJ_start = System.currentTimeMillis();
            
            ResultSet rs = pStatement.executeQuery();
            ResultSet rsc = pStatement_c.executeQuery();
            
            // Calc TJ
            long TJ_end = System.currentTimeMillis();
            long TJ = TJ_end - TJ_start;
            
            
            
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
            
            out.println("<tr><td></td><td><b>ID<b></td>"
            		+ "<td><a href='search?title=" + title + "&offset=0&limit=" + s_limit + "&order=title&ad=" + new_order_dir + "'><b>Title</b></a></td>"
            		+ "<td><b>Year</b></td>"
            		+ "<td><b>Director</b></td>"
            		+ "<td><b>Genre(s)</b></td>"
            		+ "<td><b>Stars</b></td>"
            		+ "<td><a href='search?title=" + title + "&offset=0&limit=" + s_limit + "&order=rating&ad=" + new_order_dir + "'><b>Rating</b></a></td>");
            
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
                
                String query_star = "SELECT name FROM stars WHERE id LIKE ? ";
                PreparedStatement pStatement_s = dbCon.prepareStatement(query_star);
                pStatement_s.setString(1, "%" + starIDs[0]);
                ResultSet star_q = pStatement_s.executeQuery();
                star_q.next();
                String star_entry = String.format("<a href='star?id=%s'>%s</a>", starIDs[0], star_q.getString("name"));
                star_q.close();
                if (starIDs.length > 1) {
	                for (int i = 1; i < starIDs.length; i++) {
	                	pStatement_s.setString(1, "%" + starIDs[i]);
	                    ResultSet star_q2 = pStatement_s.executeQuery();
	                    star_q2.next();
	                    star_entry += String.format(", <a href='star?id=%s'>%s</a>", starIDs[i], star_q2.getString("name"));
	                    star_q2.close();
	                }
                }
                pStatement_s.close();
                
                out.println(String.format("<tr>"
                		+ "<td><form ACTION='' METHOD='GET' target=\"hiddenFrame\">"
                		+ "<input TYPE='HIDDEN' name='title' value='" + title + "'>"
						+ "<input TYPE='HIDDEN' name='offset' value='" + s_offset + "'>"
            			+ "<input TYPE='HIDDEN' name='limit' value='" + s_limit + "'>"
	            		+ "<input TYPE='HIDDEN' name='order' value='" + order_by + "'>"
	            		+ "<input TYPE='HIDDEN' name='ad' value='" + order_dir + "'>"
                		+ "<input TYPE='HIDDEN' name=movie value='%s'>"
                		+ "<input TYPE='SUBMIT' value='Add to Cart'></form></td>"
                		+ "<td>%s</td><td><a href='movie?id=%s'>%s</a></td><td>%s</td><td>%s</td><td>%s</td>"
                		+ "<td>%s</td><td>%s</td></tr>", m_ID, m_ID, m_ID, m_Title, m_Year, m_Dir, m_Genres, star_entry, m_Rating));
                
            }
            out.println("</table>");
            
//            // should check if request.getSession(false) might return null!
//            User myUser = (User) request.getSession(false).getAttribute("user");
//            String movieId = request.getParameter("movie");
//            if (movieId != null  && movieId != "") {
//            	if (! myUser.getCart().contains(movieId)){
//            		myUser.addToCart(movieId);
//            		myUser.sortCart();
//            		myUser.addToQuantities(myUser.posCart(movieId), 1);
//            	}
//            }
            
            // Get total rows
            rsc.next();
            String s_total = rsc.getString("total");
            int total = Integer.parseInt(s_total);
            
            out.print("<br>");
            
            if (offset > 0) {
            	// Build Previous page query
            	int new_offset = Math.max(offset-limit, 0);
	            String pp_query = String.format("<form ACTION='search' METHOD='GET'>"
						+ "<input TYPE='HIDDEN' name='title' value='" + title + "'>"
						+ "<input TYPE='HIDDEN' name='offset' value=%d>"
	        			+ "<input TYPE='HIDDEN' name='limit' value=%d>"
						+ "<input TYPE='HIDDEN' name='order' value=%s>"
	        			+ "<input TYPE='HIDDEN' name='ad' value=%s>"
	    			  + "<input type='SUBMIT' value='Previous Page'>"
	    			  + "</form>", new_offset, limit, order_by, order_dir);
	            
	            // Previous page button
	            out.print(pp_query);
            }
            
            if (offset+count < total) {
            	// Build Next page query
	            String np_query = String.format("<form ACTION='search' METHOD='GET'>"
						+ "<input TYPE='HIDDEN' name='title' value='" + title + "'>"
						+ "<input TYPE='HIDDEN' name='offset' value=%d>"
	        			+ "<input TYPE='HIDDEN' name='limit' value=%d>"
	        			+ "<input TYPE='HIDDEN' name='order' value=%s>"
	        			+ "<input TYPE='HIDDEN' name='ad' value=%s>"
	    			  + "<input type='SUBMIT' value='Next Page'>"
	    			  + "</form>", offset+limit, limit, order_by, order_dir);
	            
	            // Next page button
	            out.println(np_query);
            }

            
            // Calc TS
            long TS_end = System.currentTimeMillis();
            long TS = TS_end - TS_start;
            
            // Write to log file
//            String contextPath = getServletContext().getRealPath("/");
            String contextPath = "//home//ubuntu//tomcat//webapps//project2";
            String filePath=contextPath+"//logStats.txt";
            out.println("<p>" + filePath + "<p>");
            
            File file = new File(filePath);
            if (!file.exists()) {
				file.createNewFile();
			}
            
            FileWriter logFile = new FileWriter(filePath, true);
            BufferedWriter bw = new BufferedWriter(logFile);
            bw.write(String.valueOf(TS) + ", ");
            bw.write(String.valueOf(TJ));
            bw.newLine();
            
            bw.close();
            
            // Close all structures
            rs.close();
            rsc.close();
            pStatement.close();
            pStatement_c.close();
            dbCon.close();

        } catch (Exception ex) {

            // Output Error Massage to html
            out.println(String.format("<html><head><title>MovieDB: Error</title></head>\n<body><p>SQL error in doGet: %s</p></body></html>", ex.getMessage()));
            return;
        } finally {
        	
        }
        
        out.println("</body></html>");
        
        out.close();
    }
}