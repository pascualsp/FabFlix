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
@WebServlet(name = "ConfirmationServlet", urlPatterns = "/confirmation")
public class ConfirmationServlet extends HttpServlet {

	// Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/masterdb")
    private DataSource dataSource;

    
    // Use http GET
    public void doPost(HttpServletRequest request, HttpServletResponse response)
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

        
        try {
        	Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
                out.println("envCtx is NULL");
            
            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/masterdb");

            // Create a new connection to database
            Connection dbCon = ds.getConnection();
            
            // Retrieve parameters from the http request
            String num = request.getParameter("num");
            String fn = request.getParameter("fn");
            String ln = request.getParameter("ln");
            String exp = request.getParameter("exp");
            
            // Declare a new statement
            Statement statement = dbCon.createStatement();
            Statement statement_0 = dbCon.createStatement();
            Statement statement_1 = dbCon.createStatement();
            Statement statement_2 = dbCon.createStatement();
            Statement statement_3 = dbCon.createStatement();
            
            // Obtain user and sort shoppingcart alphabetically
            User myUser = (User) request.getSession(false).getAttribute("user");
                       
            // Gather movie ids added in Users shopping cart
            List<String> shoppingCart = myUser.getCart();
            
           
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
	            
	            // Generate a SQL query
	            String query_customerid = String.format("SELECT * FROM customers WHERE email = \"%s\"", myUser.getUsername());
	            
	            // Perform the query
	            ResultSet rs1 = statement_1.executeQuery(query_customerid);
	            String c_id = "";
	            while (rs1.next()) {
                	c_id = rs1.getString("id");
                }
	            
	            // Perform the query
	            String query_last_sale_id = String.format("SELECT MAX(id) as max_id FROM sales"); 
	            ResultSet rs0 = statement_0.executeQuery(query_last_sale_id);
	            rs0.next();
	            int s_id = Integer.parseInt(rs0.getString("max_id"));
	            
            	
	            
	            // Check if customer info matched credit card
	            if (rs.next() == true) {
	            	out.println("<a href='index.html'>Return to Home</a>");
	            	out.println("<body><h1>Confirmation</h1>");
	            	out.println(String.format("<p>Transaction Succeeded</p><br>"));
	            	
	            	
	            	for (String mId : shoppingCart) {
	            		for (int i = 1; i <= myUser.getQuantities().get(myUser.posCart(mId)); i++){
	            			String query_record_sale = String.format("INSERT INTO sales "
	    		            		+ "VALUES (NULL, %s, '%s', CURDATE())", c_id, mId);
	    	            	statement_2.executeUpdate(query_record_sale);
	    	            }
	            	}
	            	
	            	String query_display_sales = String.format("SELECT * FROM sales as s "
	            			+ "JOIN movies as m ON m.id = s.movieId "
	            			+ "WHERE s.id > %d", s_id);
		            ResultSet rs3 = statement_3.executeQuery(query_display_sales);
		            // Create a html <table>
		            out.println("<table border>");
		            // Iterate through each row of rs3 and create a table row <tr>
		            out.println("<tr><td>Sale ID</td><td>Movie Title</td><td>Quantity</td></tr>");
		            while (rs3.next()) {
		            	String movie_id = rs3.getString("movieId");
		            	String sale_id = rs3.getString("id");
		            	String m_title = rs3.getString("title");
		            	
		            	out.println(String.format("<tr><td>%s</td><td>%s</td><td>%s</td></tr>", sale_id, m_title, myUser.getQuantities().get(myUser.posCart(movie_id))));
		            }
		            out.println("</table>");
	            	
	            	
	            	myUser.resetCart();
	            }
	            else {
	            	out.println("<a href='cart'>Return to Cart</a>");
	            	out.println("<body><h1>Confirmation</h1>");
	            	out.println(String.format("<tr><th>Transaction Unsuccessful</th></tr>"));
	            	out.println(String.format("Invalid Input: %s, %s, %s, %s", 
	            			request.getParameter("exp"), request.getParameter("num"), request.getParameter("fn"), request.getParameter("ln")));
	            	out.println("<tr><th><a href='checkout'>Edit Customer Information</a></th></tr>");
	            }
	            
	            
	            
	            // Close all structures
	            rs.close();
	            rs1.close();
	            pStatement1.close();
            }
            else {
            	out.println("<th>Shopping Cart is Empty</th>");
            }
            
            statement_1.close();
            dbCon.close();

        } catch (Exception ex) {

            // Output Error Massage to html
            out.println(String.format("<html><head><title>MovieDB: Error</title></head>\n<body><p>SQL error in doGet: %s</p></body></html>", ex.getMessage()));
            return;
        }
        out.close();
    }
}

