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
@WebServlet(name = "BrowseByServlet", urlPatterns = "/movie-browse")
public class BrowseByServlet extends HttpServlet {

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
        out.println("<html><head><title>Movie Browse</title>");
        
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
        out.println("<h1>Browse</h1>");
        
        out.println("<h2>Browse by Genre</h2>");
        
        
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
            String query = String.format("SELECT name FROM genres");       
            
            //<tr><th><a href="browse?id=Action&offset=0&limit=5&order=&ad=">Action</a></th></tr>
            
            // Perform the query
            ResultSet rs = statement.executeQuery(query);
            
            // use results
         // Print browse by Genre
            out.println("<table><tr>");
            int count = 0;
            while (rs.next()) {
            	String m_name = rs.getString("name");
            	out.printf("<th><a href=\"browse?id=%s&offset=0&limit=5&order=&ad=\">%s</a></th>", m_name, m_name);
            	count++;
            	if (count%4 == 0) {
            		out.printf("</tr><tr>");
            	}
            }
            out.println("</tr></table>");
            
            // Print browse by Title
            out.println("<h2>Browse by Title</h2>");
            //out.println("<table><tr>");
            out.println("<th><a href=browse?id=0&offset=0&limit=5&order=&ad=>0</a></th> | <th><a href=browse?id=1&offset=0&limit=5&order=&ad=>1</a></th> | <th><a href=browse?id=2&offset=0&limit=5&order=&ad=>2</a></th> | "
    	            + "<th><a href=browse?id=3&offset=0&limit=5&order=&ad=>3</a></th> | <th><a href=browse?id=4&offset=0&limit=5&order=&ad=>4</a></th> | <th><a href=browse?id=5&offset=0&limit=5&order=&ad=>5</a></th> | "
    	    		+ "<th><a href=browse?id=6&offset=0&limit=5&order=&ad=>6</a></th> | <th><a href=browse?id=7&offset=0&limit=5&order=&ad=>7</a></th> | <th><a href=browse?id=8&offset=0&limit=5&order=&ad=>8</a></th> | "
    	    		+ "<th><a href=browse?id=9&offset=0&limit=5&order=&ad=>9</a></th></tr><tr> | <th><a href=browse?id=A&offset=0&limit=5&order=&ad=>A</a></th> | <th><a href=browse?id=B&offset=0&limit=5&order=&ad=>B</a></th> | "
    	    		+ "<th><a href=browse?id=C&offset=0&limit=5&order=&ad=>C</a></th> | <th><a href=browse?id=D&offset=0&limit=5&order=&ad=>D</a></th> | <th><a href=browse?id=E&offset=0&limit=5&order=&ad=>E</a></th> | "
    	    		+ "<th><a href=browse?id=F&offset=0&limit=5&order=&ad=>F</a></th> | <th><a href=browse?id=G&offset=0&limit=5&order=&ad=>G</a></th> | <th><a href=browse?id=H&offset=0&limit=5&order=&ad=>H</a></th> | "
    	    		+ "<th><a href=browse?id=I&offset=0&limit=5&order=&ad=>I</a></th> | <th><a href=browse?id=J&offset=0&limit=5&order=&ad=>J</a></th> | <th><a href=browse?id=K&offset=0&limit=5&order=&ad=>K</a></th> | "
    	    		+ "<th><a href=browse?id=L&offset=0&limit=5&order=&ad=>L</a></th> | <th><a href=browse?id=M&offset=0&limit=5&order=&ad=>M</a></th></tr><tr> | <th><a href=browse?id=N&offset=0&limit=5&order=&ad=>N</a></th> | "
    	    		+ "<th><a href=browse?id=O&offset=0&limit=5&order=&ad=>O</a></th> | <th><a href=browse?id=P&offset=0&limit=5&order=&ad=>P</a></th> | <th><a href=browse?id=Q&offset=0&limit=5&order=&ad=>Q</a></th> | "
    	    		+ "<th><a href=browse?id=R&offset=0&limit=5&order=&ad=>R</a></th> | <th><a href=browse?id=S&offset=0&limit=5&order=&ad=>S</a></th> | <th><a href=browse?id=T&offset=0&limit=5&order=&ad=>T</a></th> | "
    	    		+ "<th><a href=browse?id=U&offset=0&limit=5&order=&ad=>U</a></th> | <th><a href=browse?id=V&offset=0&limit=5&order=&ad=>V</a></th> | <th><a href=browse?id=W&offset=0&limit=5&order=&ad=>W</a></th> | "
    	    		+ "<th><a href=browse?id=X&offset=0&limit=5&order=&ad=>X</a></th> | <th><a href=browse?id=Y&offset=0&limit=5&order=&ad=>Y</a></th> | <th><a href=browse?id=Z&offset=0&limit=5&order=&ad=>Z</a></th>"
    	    		);
            //out.println("</tr></table>");
            
            
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