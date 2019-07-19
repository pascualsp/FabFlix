

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class MovieServlet
 */
@WebServlet("/page")
public class MovieServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MovieServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// change this to your own mysql username and password
        String loginUser = "root";
        String loginPasswd = "9209548jp";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";
        
        // set response mime type
        response.setContentType("text/html"); 

        // get the printwriter for writing response
        PrintWriter out = response.getWriter();

        out.println("<html>");
        out.println("<head><title>Movie List</title></head>");
        
        try {
    		Class.forName("com.mysql.jdbc.Driver").newInstance();
    		// create database connection
    		Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
    		// declare statement
    		Statement statement = connection.createStatement();
    		// prepare query
    		String query = "SELECT * from movies "
    					 + "JOIN ratings on movies.id = ratings.movieId "
    					 + "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(name SEPARATOR ', ') as genres FROM (genres_in_movies JOIN genres on genres_in_movies.genreId = genres.id) GROUP BY movieId) as g on movies.id = g.movieId "
    					 + "JOIN (SELECT DISTINCT movieId, GROUP_CONCAT(name SEPARATOR ', ') as stars FROM (stars_in_movies JOIN stars on stars_in_movies.starId = stars.id) GROUP BY movieId) as s on movies.id = s.movieId "
    					 + "ORDER BY ratings.rating DESC limit 20 ";
    		// execute query
    		ResultSet resultSet = statement.executeQuery(query);

    		out.println("<body>");
    		out.println("<h1>Movie List</h1>");
    		
    		out.println("<table border>");
    		
    		// add table header row
    		out.println("<tr>");
    		out.println("<td>title</td>");
    		out.println("<td>year</td>");
    		out.println("<td>director</td>");
    		out.println("<td>rating</td>");
    		out.println("<td>list of genres</td>");
    		out.println("<td>list of stars</td>");
    		out.println("</tr>");
    		
    		// add a row for every movie result
    		while (resultSet.next()) {
    			// get a star from result set
    			String title = resultSet.getString("title");
    			String year = resultSet.getString("year");
    			String director = resultSet.getString("director");
    			String rating = resultSet.getString("rating");
    			String genres = resultSet.getString("genres");
    			String stars = resultSet.getString("stars");
    			
    			out.println("<tr>");
    			out.println("<td>" + title + "</td>");
    			out.println("<td>" + year + "</td>");
    			out.println("<td>" + director + "</td>");
    			out.println("<td>" + rating + "</td>");
    			out.println("<td>" + genres + "</td>");
    			out.println("<td>" + stars + "</td>");
    			out.println("</tr>");
    		}
    		
    		out.println("</table>");
    		
    		out.println("</body>");
    		
    		resultSet.close();
    		statement.close();
    		connection.close();
    		
        } catch (Exception e) {
    		/*
    		 * After you deploy the WAR file through tomcat manager webpage,
    		 *   there's no console to see the print messages.
    		 * Tomcat append all the print messages to the file: tomcat_directory/logs/catalina.out
    		 * 
    		 * To view the last n lines (for example, 100 lines) of messages you can use:
    		 *   tail -100 catalina.out
    		 * This can help you debug your program after deploying it on AWS.
    		 */
    		e.printStackTrace();
    		
    		out.println("<body>");
    		out.println("<p>");
    		out.println("Exception in doGet: " + e.getMessage());
    		out.println("</p>");
    		out.print("</body>");
    }
    
    out.println("</html>");
    out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
