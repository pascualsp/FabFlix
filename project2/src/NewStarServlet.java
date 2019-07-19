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
@WebServlet(name = "NewStarServlet", urlPatterns = "/newstar")
public class NewStarServlet extends HttpServlet {

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
        out.println("<html><head><title>New Star</title>");
        
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
        out.println("<h1>New Star</h1>");
        

        
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
            Statement statement = dbCon.createStatement();
            Statement statement2 = dbCon.createStatement();
            Statement statement3 = dbCon.createStatement();
            
            // Retrieve parameters
            String name = request.getParameter("name");
            String syear = request.getParameter("birthyear");
            int birthyear;
            if (syear.equals("")) {
            	birthyear = 0;
            }
            else {
            	birthyear = Integer.parseInt(syear);
            }
            
            
            if (name.equals("")) {
            	out.println("<a>No star was added</a>");
            }
            else {
            	out.println("<a>New Star Added Information:</a>");
            	
            	// Generate a SQL query
            	String query_maxstarid = String.format("SELECT MAX(id) as max_id FROM stars"); 
            	
            	// Perform the query
	            ResultSet rs = statement2.executeQuery(query_maxstarid);
	            rs.next();
	            int new_idnum = Integer.parseInt(rs.getString("max_id").substring(2)) + 1;
            	String new_id = "nm" + new_idnum;
	            
            	// Generate second SQL query
            	String query = "";
            	if (birthyear == 0) {
            		query = String.format("INSERT INTO stars (id, name) "
	            		+ "VALUES ('%s',?)", new_id);
            	}
            	else {
            		query = String.format("INSERT INTO stars (id, name, birthYear) "
    	            		+ "VALUES ('%s', ?, %s)", new_id, birthyear);
            	}
            	
            	// Create and perform the query as a PreparedStatement
            	PreparedStatement pStatement1 = dbCon.prepareStatement(query);
                pStatement1.setString(1, name);
            	
	            pStatement1.executeUpdate();
            	
	            
	            // test check (can delete after)
	            // Generate a SQL query
            	String test = String.format("SELECT * FROM stars WHERE id = '%s'", new_id); 
            	// Perform the query
	            ResultSet rs2 = statement3.executeQuery(test);
	            out.println("<table>");
	            while (rs2.next()) {
	            	String m_id = rs2.getString("id");
	            	String m_name = rs2.getString("name");
	                String m_year = rs2.getString("birthYear");
	                out.println("<tr><td>ID</td><td>Name</td><td>Birth Year</td>");
	                
	                out.printf("<tr><td>%s</td><td>%s</td><td>%s</td>", m_id, m_name, m_year);
	            }
	            out.println("</table>");
	            
	            
	            // Close all structures
	            rs.close();
	            rs2.close();
	            pStatement1.close();
            }
            
            // Close all structures
            statement2.close();
            statement3.close();
            dbCon.close();

        } catch (NumberFormatException ex) {

            // Output Error Massage to html
            out.println(String.format("<html><head><title>MovieDB: Error</title></head>\n<body><p>Invalid Inputs</p></body></html>"));
            out.println("<a href=\"newstar.html\">Back to Add New Star</a>");
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