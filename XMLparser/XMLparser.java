import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class XMLparser extends DefaultHandler {
    
    //variable indicating XML type
    private String type;
    
    List<Actor> myActors;
    List<Movie> myMovies;
    List<Cast>  myCasts;
    private String tempVal;

    //to maintain context
    private Actor tempActor;
    private Movie tempMovie;
    private Cast  tempCast;

    public XMLparser(String Xtype) {
        type = Xtype;
        
        if (type.equals("actor")) {
            myActors = new ArrayList<Actor>();
        }
        else if (type.equals("movie")) {
            myMovies = new ArrayList<Movie>();
        }
        else if (type.equals("cast")) {
            myCasts = new ArrayList<Cast>();
        }
    }

    public void runExample() {
        parseDocument();
        printData();
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        
        String fileName = "";
        if (type.equals("actor")) {
            fileName = "actors63.xml";
        }
        else if (type.equals("movie")) {
            fileName = "mains243.xml";
        }
        else if (type.equals("cast")) {
            fileName = "casts124.xml";
        }
        
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse(fileName, this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Iterate through the list and print
     * the contents
     */
    private void printData() {
        
        String loginUser = "root";
        String loginPasswd = "9209548jp";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            
            if (type.equals("actor")) {

                String existsQuery = "SELECT 1 FROM stars WHERE name = ?";
                PreparedStatement checkStatement = connection.prepareStatement(existsQuery);
                
                String insertQuery = "INSERT INTO stars VALUES (?, ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                
                String maxQuery = "SELECT MAX(id) as max_id FROM stars";
                PreparedStatement maxStatement = connection.prepareStatement(maxQuery);
                ResultSet maxId = maxStatement.executeQuery();
                maxId.next();
                int max = Integer.parseInt(maxId.getString("max_id").substring(2,9));

                
                for (Actor a : myActors) {
                    
                    String actor = a.getName();
                    String y = a.getDob();
                    checkStatement.setString(1, actor);
                    ResultSet exists = checkStatement.executeQuery();
                    
                    if (!exists.next()) {
                        max++;
                        String newId = "nm" + String.valueOf(max);
                        
                        insertStatement.setString(1, newId);
                        insertStatement.setString(2, actor);
                        if (y == null || y.equals("") || !y.matches("^[0-9]+$")) {
                            insertStatement.setNull(3, java.sql.Types.INTEGER);
                            System.out.println(actor + " not in db; adding with ID=" + newId + " birthYear=null");
                        }
                        else {
                            insertStatement.setInt(3, Integer.parseInt(y));
                            System.out.println(actor + " not in db; adding with ID=" + newId + " birthYear=" + y);
                        }
                        insertStatement.executeUpdate();
                        
                    }
                    else {
                        System.out.println("Actor already exists in DB");
                    }
                }
            }
            else if (type.equals("movie")) {
                
                String existsQuery = "SELECT 1 FROM movies WHERE title = ?";
                PreparedStatement checkStatement = connection.prepareStatement(existsQuery);
                
                String existsQueryG = "SELECT 1 FROM genres WHERE name = ?";
                PreparedStatement checkStatementG = connection.prepareStatement(existsQueryG);
                
                String genreQuery = "SELECT id FROM genres WHERE name = ?";
                PreparedStatement genreStatement = connection.prepareStatement(genreQuery);
                
                String insertQuery = "INSERT INTO movies VALUES (?, ?, ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                
                String insertQueryG = "INSERT INTO genres VALUES (?, ?)";
                PreparedStatement insertStatementG = connection.prepareStatement(insertQueryG);
                
                String insertQueryGM = "INSERT INTO genres_in_movies VALUES (?, ?)";
                PreparedStatement insertStatementGM = connection.prepareStatement(insertQueryGM);
                
                String maxQuery = "SELECT MAX(id) as max_id FROM movies";
                PreparedStatement maxStatement = connection.prepareStatement(maxQuery);
                ResultSet maxId = maxStatement.executeQuery();
                maxId.next();
                int max = Integer.parseInt(maxId.getString("max_id").substring(2,9));
                
                String maxQueryG = "SELECT MAX(id) as max_id FROM genres";
                PreparedStatement maxStatementG = connection.prepareStatement(maxQueryG);
                ResultSet maxIdG = maxStatementG.executeQuery();
                maxIdG.next();
                int maxG = maxIdG.getInt("max_id");

                
                for (Movie m : myMovies) {
                    
                    String t = m.getTitle();
                    String y = m.getYear();
                    String d = m.getDir();
                    String g = m.getGenres();
                    
                    if (t != null && y != null && d != null && y.matches("^[0-9]+$")) {
                        checkStatement.setString(1, t);
                        ResultSet exists = checkStatement.executeQuery();
                        
                        if (!exists.next()) {
                            max++;
                            String newId = "tt" + String.valueOf(max);
                            System.out.println(t + " not in db; adding with ID=" + newId + " Year=" + y + " Director=" + d);
                            
                            insertStatement.setString(1, newId);
                            insertStatement.setString(2, t);
                            insertStatement.setInt(3, Integer.parseInt(y));
                            insertStatement.setString(4, d);
                            insertStatement.executeUpdate();
                            
                            String genresList[] = g.split(";");
                            for (String ge : genresList) {
                                
                                int gId;
                                
                                checkStatementG.setString(1, ge);
                                ResultSet existsG = checkStatementG.executeQuery();
                                if (!existsG.next()) {
                                    maxG++;
                                    System.out.println("Genre " + ge + " not in db; adding with ID=" + maxG);
                                    
                                    insertStatementG.setInt(1, maxG);
                                    insertStatementG.setString(2, ge);
                                    insertStatementG.executeUpdate();
                                    
                                    gId = maxG;
                                    
                                }
                                else {
                                    genreStatement.setString(1, ge);
                                    ResultSet existsGenre = genreStatement.executeQuery();
                                    existsGenre.next();
                                    gId = existsGenre.getInt("id");
                                }
                                
                                System.out.println("Adding " + ge + " with movie " + newId);
                                    
                                insertStatementGM.setInt(1, gId);
                                insertStatementGM.setString(2, newId);
                                insertStatementGM.executeUpdate();
                                
                            }
                            
                            
                        }
                    }
                    else {
                        System.out.println("Invalid movie entry");
                    }
                }
                
                
            }
            else if (type.equals("cast")) {
                
                String existsQueryM = "SELECT id FROM movies WHERE title = ?";
                PreparedStatement checkStatementM = connection.prepareStatement(existsQueryM);
                
                String existsQueryA = "SELECT id FROM stars WHERE name = ?";
                PreparedStatement checkStatementA = connection.prepareStatement(existsQueryA);
                
                String insertQuery = "INSERT INTO stars_in_movies VALUES (?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);

                
                for (Cast c : myCasts) {
                    
                    String m = c.getMovie();
                    String a = c.getActor();
                    
                    checkStatementM.setString(1, m);
                    ResultSet existsM = checkStatementM.executeQuery();
                    
                    checkStatementA.setString(1, a);
                    ResultSet existsA = checkStatementA.executeQuery();
                    
                    if (existsM.next() && existsA.next()) {
                        
                        String mId = existsM.getString("id");
                        String aId = existsA.getString("id");
                        
                        System.out.println("Adding starId " + aId + " with movieId " + mId);
                        
                        insertStatement.setString(1, aId);
                        insertStatement.setString(2, mId);
                        insertStatement.executeUpdate();
                        
                        
                    }
                    else {
                        System.out.println("Invalid entry; Star or Movie does not exist");
                    }
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        	System.out.println("Exception in doGet: " + e.getMessage());
        }
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        
        if (type.equals("actor")) {
            if (qName.equalsIgnoreCase("actor")) {
                //create a new instance of Actor
                tempActor = new Actor();
            }
        }
        else if (type.equals("movie")) {
            if (qName.equalsIgnoreCase("film")) {
                //create a new instance of Movie
                tempMovie = new Movie();
            }
        }
        else if (type.equals("cast")) {
            if (qName.equalsIgnoreCase("m")) {
                //create a new instance of Cast
                tempCast = new Cast();
            }
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (type.equals("actor")) {
            if (qName.equalsIgnoreCase("actor")) {
                //add it to the list
                myActors.add(tempActor);
            }
            else if (qName.equalsIgnoreCase("stagename")) {
                tempActor.setName(tempVal);
            }
            else if (qName.equalsIgnoreCase("dob")) {
                tempActor.setDob(tempVal);
            }
        }
        else if (type.equals("movie")) {
            if (qName.equalsIgnoreCase("film")) {
                //add it to the list
                myMovies.add(tempMovie);
            }
            else if (qName.equalsIgnoreCase("t")) {
                tempMovie.setTitle(tempVal);
            }
            else if (qName.equalsIgnoreCase("year")) {
                tempMovie.setYear(tempVal);
            }
            else if (qName.equalsIgnoreCase("dirn")) {
                tempMovie.setDir(tempVal);
            }
            else if (qName.equalsIgnoreCase("cat")) {
                tempMovie.setGenres(tempVal);
            }
        }
        else if (type.equals("cast")) {
            if (qName.equalsIgnoreCase("m")) {
                //add it to the list
                myCasts.add(tempCast);
            }
            else if (qName.equalsIgnoreCase("t")) {
                tempCast.setMovie(tempVal);
            }
            else if (qName.equalsIgnoreCase("a")) {
                tempCast.setActor(tempVal);
            }
        }
        
    }
    
    public static void main(String[] args) {
        XMLparser xd = new XMLparser(args[0]);
        xd.runExample();
    }
    
    
}