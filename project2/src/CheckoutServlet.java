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
import java.util.List;

/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedb,
 * generates output as a html <table>
 */

// Declaring a WebServlet called SearchServlet, which maps to url "/browse"
@WebServlet(name = "CheckoutServlet", urlPatterns = "/checkout")
public class CheckoutServlet extends HttpServlet {

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
        out.println("<html><head><title>Checkout</title>");
        
        out.println("<style>");
        out.println("table {border-collapse: collapse;}");
        out.println("th, td {border: 1px solid #dddddd; text-align: left; padding: 5px;}");
        out.println("</style>");
        
        out.println("</head>");

        // Building page body
        out.println("<a href='cart'>Return to Cart</a>");
        out.println("<body><h1>Customer Information</h1>");


        try {
        	Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
                out.println("envCtx is NULL");
            
            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/moviedb");

            // Create a new connection to database
            Connection dbCon = ds.getConnection();
            
            // Retrieve parameters from the http request
            String num = request.getParameter("num");
            String fn = request.getParameter("fn");
            String ln = request.getParameter("ln");
            String exp = request.getParameter("exp");
            
            // Declare a new statement
            Statement statement = dbCon.createStatement();
            
            // Obtain user and sort shoppingcart alphabetically
            User myUser = (User) request.getSession(false).getAttribute("user");
                       
            // Gather movie ids added in Users shopping cart
            List<String> shoppingCart = myUser.getCart();
            
            // Ask for customer info
            out.println(String.format("<tr>"
        			+ "<td><form ACTION=\"confirmation\" METHOD=\"POST\">"
					+ "Credit Number:<br><input TYPE=\"TEXT\" name=\"num\"><br>"
					+ "Expiration Date:<br><input TYPE=\"TEXT\" name=\"exp\"><br>"
					+ "First Name:<br><input TYPE=\"TEXT\" name=\"fn\"><br>"
					+ "Last Name:<br><input TYPE=\"TEXT\" name=\"ln\"><br><br>"
					+ "<input TYPE=\"SUBMIT\" name=submit VALUE=\"Submit\">"
					+ "</form></td>"
        			+ "</tr>"));
            
           
            if (shoppingCart.size() > 0) {
	            // Generate a SQL query
	            String query = String.format("SELECT * FROM creditcards "
	            		+ "WHERE id = ? AND firstName = ? AND lastName = ? AND expiration = ?");
	            
	            // Create and perform the query as a PreparedStatement
	            PreparedStatement pStatement1 = dbCon.prepareStatement(query);
	            pStatement1.setString(1, num);
	            pStatement1.setString(2, fn);
	            pStatement1.setString(3, ln);
	            pStatement1.setString(4, exp);
	                         
	            // Perform the query
	            ResultSet rs = pStatement1.executeQuery();
	            
	            // Check if customer info matched credit card
	            if (rs.next() == true) {
	            	out.println(String.format("<tr><td>transaction succeeded</td></tr>"));
	            	out.println(String.format("<tr>"
	            			+ "<td><form ACTION=\"confirmation\" METHOD=\"POST\">"
	    					+ "<input TYPE='HIDDEN' name=success value='0'>"
	    					+ "</form></td>"
	            			+ "</tr>"));
	            }
	            
	            /*
	            // Create a html <table>
	            out.println("<table border>");
	            
	            // Iterate through each row of rs and create a table row <tr>
	            out.println("<tr><td>Number</td><td>Number</td><td>Number</td><td>Number</td></tr>");
	            while (rs.next()) {
	            	String m_num = rs.getString("id");
	            	String m_exp = rs.getString("firstName");
	            	String m_fn = rs.getString("lastName");
	            	String m_ln = rs.getString("expiration");
	            	System.out.println("HERE");
		            System.out.println("went through");
	            	out.println(String.format("<tr>"
	            			+ "<td>%s</td><td>%s</td><td>%s</td><td>%s</td>"
	            			+ "</tr>", m_num, m_exp, m_fn, m_ln));
	            }
	            out.println("</table>");
	            */
	            
	            // Close all structures
	            rs.close();
	            pStatement1.close();
            }
            else {
            	out.println("<th>Shopping Cart is Empty</th>");
            }
            
            
            dbCon.close();

        } catch (Exception ex) {

            // Output Error Massage to html
            out.println(String.format("<html><head><title>MovieDB: Error</title></head>\n<body><p>SQL error in doGet: %s</p></body></html>", ex.getMessage()));
            return;
        }
        out.close();
    }
}
