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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedb,
 * generates output as a html <table>
 */

// Declaring a WebServlet called SearchServlet, which maps to url "/browse"
@WebServlet(name = "NewMovieServlet", urlPatterns = "/newmovie")
public class NewMovieServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/masterdb")
    private DataSource dataSource;

    
    // Use http GET
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html");    // Response mime type
        
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Building page head with title
        out.println("<html><head><title>New Movie</title>");
        
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
        out.println("<a href=\"index2.html\">Home</a>");
        out.println("<h1>New Movie</h1>");

        try {
        	Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
                out.println("envCtx is NULL");
            
            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/masterdb");

            // Create a new connection to database
            Connection dbCon = ds.getConnection();

            // Declare a new statement
            Statement statement1 = dbCon.createStatement();
            Statement statement2 = dbCon.createStatement();
            Statement statement3 = dbCon.createStatement();
            Statement statement4 = dbCon.createStatement();
            
            // Retrieve parameter and check each if empty
            String title = request.getParameter("title");
            String myear = request.getParameter("year");
            int year;
            if (myear.equals("")) {
            	throw new NumberFormatException();
            }
            else {
            	year = Integer.parseInt(myear);
            }
            String director = request.getParameter("director");
            String star = request.getParameter("star");
            String syear = request.getParameter("byear");
            int byear;
            if (syear.equals("")) {
            	byear = 0;
            }
            else {
            	byear = Integer.parseInt(syear);
            }
            String genre = request.getParameter("genre");
            if (title.equals("") || director.equals("") || star.equals("") || genre.equals("")) {
            	throw new NumberFormatException();
            }
            
            // Queries to generate ids for movie and star
        	String query_maxmovieid = String.format("SELECT MAX(id) as max_mid FROM movies"); 
        	ResultSet rs3 = statement3.executeQuery(query_maxmovieid);
            rs3.next();
            int new_midnum = Integer.parseInt(rs3.getString("max_mid").substring(3)) + 1;
        	String new_mid = "tt0" + new_midnum;
        	
        	String query_maxstarid = String.format("SELECT MAX(id) as max_sid FROM stars"); 
        	ResultSet rs4 = statement4.executeQuery(query_maxstarid);
            rs4.next();
            int new_sidnum = Integer.parseInt(rs4.getString("max_sid").substring(2)) + 1;
        	String new_sid = "nm" + new_sidnum;
        	
            
            // Generate a SQL query
            String query1 = String.format("CALL add_movie(@a, @b, @c, @d, @e, ?, ?, %d, ?, ?, ?, %d, ?)", year, byear);
            String query2 = String.format("SELECT @a, @b, @c, @d, @e");
            
            // Create and perform the query as a PreparedStatement
            PreparedStatement pStatement1 = dbCon.prepareStatement(query1);
            pStatement1.setString(1, new_mid);
            pStatement1.setString(2, title);
            pStatement1.setString(3, director);
            pStatement1.setString(4, new_sid);
            pStatement1.setString(5, star);
            pStatement1.setString(6, genre);
            
            
            // Perform the query
            ResultSet rs1 = pStatement1.executeQuery();
            ResultSet rs2 = statement2.executeQuery(query2);
            while (rs2.next()) {
            	String message1 = rs2.getString("@a");
            	String message2 = rs2.getString("@b");
            	String message3 = rs2.getString("@c");
            	String message4 = rs2.getString("@d");
            	String message5 = rs2.getString("@e");
            	out.printf("<tr><td>%s</td><br />", message2);
            	out.printf("<tr><td>%s</td><br />", message3);
            	out.printf("<tr><td>%s</td><br />", message4);
            	out.printf("<tr><td>%s</td><br />", message5);
            	out.printf("<tr><td>%s</td><br />", message1);
            } 
            
            // Close all structures
            rs1.close();
            rs2.close();
            rs3.close();
            rs4.close();
            pStatement1.close();
            statement2.close();
            statement3.close();
            statement4.close();
            dbCon.close();

        } catch (NumberFormatException ex) {

            // Output Error Massage to html
            out.println(String.format("<html><head><title>MovieDB: Error</title></head>\n<body><p>Invalid Inputs</p></body></html>"));
            out.println("<a href=\"newmovie.html\">Back to Add New Movie</a>");
            return;
        }
        
        catch (Exception ex) {

            // Output Error Massage to html
            out.println(String.format("<html><head><title>MovieDB: Error</title></head>\n<body><p>SQL error in doGet: %s</p></body></html>", ex.getMessage()));
            return;
        }
        
        out.close();
    }
    
}