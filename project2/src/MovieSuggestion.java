
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// server endpoint URL
@WebServlet("/movie-suggestion")
public class MovieSuggestion extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	// Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    
    public MovieSuggestion() {
        super();
    }

    /*
     * 
     * Match the query against Marvel and DC heros and return a JSON response.
     * 
     * For example, if the query is "super":
     * The JSON response look like this:
     * [
     * 	{ "value": "Superman", "data": { "category": "dc", "heroID": 101 } },
     * 	{ "value": "Supergirl", "data": { "category": "dc", "heroID": 113 } }
     * ]
     * 
     * The format is like this because it can be directly used by the 
     *   JSON auto complete library this example is using. So that you don't have to convert the format.
     *   
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information.
     * 
     * 
     */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			// setup the response json arrray
			JsonArray jsonArray = new JsonArray();
			
			// Create a new connection to database
            Connection dbCon = dataSource.getConnection();
			
			// get the query string from parameter
			String query = request.getParameter("query");
			
			
			
			// Build title query
			String title_query = "";
            String[] queries = query.split(" ");
            title_query += "+" + queries[0] + "*";
            if (queries.length > 1) { 
            	for (int i = 1; i < queries.length; i++){
            		title_query += " +" + queries[i] + "*";		
                }
            }
            
			String results_query = String.format("SELECT * FROM movies "
	 				   + "WHERE MATCH (title) AGAINST ('" + title_query
	 				   + "' IN BOOLEAN MODE) "
	 				   + "LIMIT 10");
			
			PreparedStatement pStatement = dbCon.prepareStatement(results_query);
            ResultSet rs = pStatement.executeQuery();
            
            //System.out.println("query getting called");
            
            if (query == "") {
				response.getWriter().write(jsonArray.toString());
				return;
			}	
            
			// return the empty json array if query is null or empty
			if (query == null || query.trim().isEmpty()) {
				response.getWriter().write(jsonArray.toString());
				return;
			}	
			
			// search on marvel heros and DC heros and add the results to JSON Array
			// this example only does a substring match
			// TODO: in project 4, you should do full text search with MySQL to find the matches on movies and stars
			
			while (rs.next()) {
            	String m_ID = rs.getString("id");
                String m_Title = rs.getString("title");
                //String m_Year = rs.getString("year");
                //String m_Dir = rs.getString("director");
                jsonArray.add(generateJsonObject(m_ID, m_Title, "movies"));
			}
			
			// Close all structures
            rs.close();
            pStatement.close();
            dbCon.close();
			
			response.getWriter().write(jsonArray.toString());
			return;
		} catch (Exception e) {
			System.out.println(e);
			response.sendError(500, e.getMessage());
		}
	}
	
	/*
	 * Generate the JSON Object from hero and category to be like this format:
	 * {
	 *   "value": "Iron Man",
	 *   "data": { "category": "marvel", "heroID": 11 }
	 * }
	 * 
	 */
	private static JsonObject generateJsonObject(String movieID, String movieTitle, String categoryName) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("value", movieTitle);
		
		JsonObject additionalDataJsonObject = new JsonObject();
		additionalDataJsonObject.addProperty("category", categoryName);
		additionalDataJsonObject.addProperty("movieID", movieID);
		
		jsonObject.add("data", additionalDataJsonObject);
		return jsonObject;
	}


}
