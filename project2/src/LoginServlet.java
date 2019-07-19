import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.jasypt.util.password.StrongPasswordEncryptor;

import java.io.IOException;
import java.io.PrintWriter;
//import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

//
@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static boolean reCHECK = false;
    
    
    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	
    	//added:
        response.setContentType("text/html");    // Response mime type
    	
    	String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
//        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        // Verify reCAPTCHA
    	if (!reCHECK) {
	        try {
	            RecaptchaVerifyUtils.verify(gRecaptchaResponse, RecaptchaConstants.WEB_SECRET_KEY);
	            reCHECK = true;
	        } catch (Exception e) {
	        	// reCAPTCHA fail
	    		JsonObject responseJsonObject = new JsonObject();
	            responseJsonObject.addProperty("status", "fail");
	            responseJsonObject.addProperty("message", "reCAPTCHA failure");
	            response.getWriter().write(responseJsonObject.toString());
	            return;
	        }
    	}
    	
    	
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        /* This example only allows username/password to be test/test
        /  in the real project, you should talk to the database to verify username/password
        */

        List<String> emails = new ArrayList<String>();
        List<String> passwords = new ArrayList<String>();
        
        // Get previous url before the submit was clicked;
        String requestURI = request.getHeader("referer");
        
        try {
        	Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            
            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/moviedb");

            // Create a new connection to database
            Connection dbCon = ds.getConnection();

            // Declare a new statement
            Statement statement = dbCon.createStatement();

            
            // Generate a SQL query
            String query = "";
            
            if (requestURI.endsWith("login.html")) {
            	query = String.format("SELECT email, password from customers");
            }
            if (requestURI.endsWith("dashboard.html")) {
            	query = String.format("SELECT email, password from employees");
            }
            

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            while (rs.next()) {
                String m_email = rs.getString("email");
                //String m_password = rs.getString("password");
                String encryptedPassword = rs.getString("password");
                emails.add(m_email);
                passwords.add(encryptedPassword);
            }
            // Close all structures
            rs.close();
            statement.close();
            dbCon.close();

        } catch (Exception ex) {
        		return;
        }
        
        
        //if (username.equals("anteater") && password.equals("123456")) {
        if (emails.contains(username)) {
        	boolean success = new StrongPasswordEncryptor().checkPassword(password, passwords.get(emails.indexOf(username)));
        	if (success) {
	            // Login success:
	
	            // set this user into the session
	            request.getSession().setAttribute("user", new User(username));
	
	            JsonObject responseJsonObject = new JsonObject();
	            if (requestURI.endsWith("login.html")) {
	            	responseJsonObject.addProperty("status", "success");
	            }
	            if (requestURI.endsWith("dashboard.html")) {
	            	responseJsonObject.addProperty("status", "success2");
	            }
	            responseJsonObject.addProperty("message", "success");
	
	            response.getWriter().write(responseJsonObject.toString());
        	}
        	else {
        		// Login fail
        		JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "incorrect password");
                response.getWriter().write(responseJsonObject.toString());
        	}
        } else {
            // Login fail
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "fail");
            if (!emails.contains(username)) {
                responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
            } else if (!passwords.contains(password)) {
                responseJsonObject.addProperty("message", "incorrect password");
            }
            response.getWriter().write(responseJsonObject.toString());
        }
    }
}
