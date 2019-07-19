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
import java.util.List;

/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedb,
 * generates output as a html <table>
 */

// Declaring a WebServlet called SearchServlet, which maps to url "/browse"
@WebServlet(name = "CartServlet", urlPatterns = "/cart")
public class CartServlet extends HttpServlet {

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
        out.println("<html><head>");
        out.println("<title>Shopping Cart</title>");
        
        out.println("<style>");
        out.println("table {border-collapse: collapse;}");
        out.println("th, td {border: 1px solid #dddddd; text-align: left; padding: 5px;}");
        out.println("</style>");
        
        
        out.println("</head>");

        // Building page body
        out.println("<a href='index.html'>Home</a>");
        out.println("<body><h1>Shopping Cart</h1>");


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
            
            // Obtain user and sort shoppingcart alphabetically
            User myUser = (User) request.getSession(false).getAttribute("user");
            //myUser.sortCart();
                       
            // Updating quantities
            try {
            	if (request.getParameter("change").equals("Update") && myUser.getCart().contains(request.getParameter("id"))) {
            		int newQuantity = Integer.parseInt(request.getParameter("quantity"));
            		if(newQuantity >= 1) {
            			myUser.updateQuantities(myUser.posCart(request.getParameter("id")), newQuantity);
            		}
            		if (newQuantity == 0) {
            			myUser.delFromQuantities(myUser.posCart(request.getParameter("id")));
	            		myUser.delFromCart(request.getParameter("id"));
            		}
            	}
            	if (request.getParameter("change").equals("Remove") && myUser.getCart().contains(request.getParameter("id"))) {
            		myUser.delFromQuantities(myUser.posCart(request.getParameter("id")));
            		myUser.delFromCart(request.getParameter("id"));
            	}
            } catch (Exception ex) {
	        	
	        }
            
            // Gather movie ids added in Users shopping cart
            List<String> shoppingCart = myUser.getCart();
            //System.out.println(shoppingCart);
            
            // Generate a substring to use for query
            String subString = "";
            int count = 0;
            for (String mId : shoppingCart) {
            	count++;
            	subString += "\"";
            	subString += mId;
            	subString += "\"";
            	if (count != shoppingCart.size())
            		subString += " OR id = ";
            }
            
            if (shoppingCart.size() > 0) {
	            // Generate a SQL query
	            String query = String.format("SELECT * FROM movies WHERE id = " + subString + " ORDER BY id");   
	                         
	            // Perform the query
	            ResultSet rs = statement.executeQuery(query);
	            
	            // Create a html <table>
	            out.println("<table border>");
	
	            // Iterate through each row of rs and create a table row <tr>
	            		
	            out.println("<tr><td>Title</td><td>Year</td><td>Quantity</td><td></td></tr>");
	            while (rs.next()) {
	            	String m_id = rs.getString("id");
	            	String m_title = rs.getString("title");
	            	String m_year = rs.getString("year");
	            	int m_quantity = myUser.getQuantities().get(myUser.posCart(m_id));
	            	
	            	out.println(String.format("<tr>"
	            			+ "<td>%s</td><td>%s</td><td>%s</td>"
	            			+ "<td><form ACTION=\"cart\" METHOD=\"GET\">"
							+ "Quantity:<br><input TYPE=\"TEXT\" name=\"quantity\"><br>"
							+ "<input TYPE='HIDDEN' name=id value='%s'>"
							+ "<input TYPE=\"SUBMIT\" name=change VALUE=\"Update\">"
							+ "<input TYPE=\"SUBMIT\" name=change VALUE=\"Remove\">"
							+ "</form></td>"
	            			+ "</tr>", m_title, m_year, m_quantity, m_id));
	            }
	            out.println("</table>");
	            
	            
	            // Add checkout link
	            out.println("<br>");
	            out.println("<a href='checkout'>Checkout</a>");
           
	            
	            
	            // Close all structures
	            rs.close();
            }
            else {
            	out.println("<th>Shopping Cart is Empty</th>");
            }
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
