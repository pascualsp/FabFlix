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
@WebServlet(name = "MetadataServlet", urlPatterns = "/metadata")
public class MetadataServlet extends HttpServlet {

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
        out.println("<html><head><title>Metadata</title>");
        
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
        out.println("<h1>Metadata</h1>");

        
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
            
            // Generate a SQL query
            String query = String.format("SELECT t.table_name, c.column_name, c.data_type "
            		+ "FROM information_schema.tables as t "
            		+ "INNER JOIN information_schema.columns as c "
            		+ "ON t.table_name = c.table_name "
            		+ "WHERE t.table_schema = 'moviedb' AND c.table_schema = 'moviedb'");
            
            out.println("<table>");
            out.println("<tr><td><b>Table Name<b></td><td><b>Attribute<b></td><td><b>Attribute's Type<b></td>");
            // Perform the query
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
            	String message1 = rs.getString("table_name");
            	String message2 = rs.getString("column_name");
            	String message3 = rs.getString("data_type");
            	out.printf("<tr><td>%s</td><td>%s</td><td>%s</td></tr>", message1, message2, message3);
            } 
            out.println("</table>");
            
            
            
            // Close all structures
            rs.close();
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