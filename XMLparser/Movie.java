

public class Movie {

	private String title;
	private String year;
	private String director;
    private String genres;
    
	public Movie(){
        genres = "";
		
	}
	
	public Movie(String title, String year, String director, String genres) {
        this.title = title;
        this.year = year;
        this.director = director;
		this.genres = genres;
	}
    

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

    
	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}
    
    
    public String getDir() {
		return director;
	}

	public void setDir(String director) {
		this.director = director;
	}
    
    
    public String getGenres() {
		return genres;
	}

	public void setGenres(String genre) {
		this.genres += genre + ";";
	}	
	
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Movie Details - ");
		sb.append("Title:" + getTitle());
		sb.append(", ");
		sb.append("Year:" + getYear());
        sb.append(", ");
		sb.append("Director:" + getDir());
        sb.append(", ");
		sb.append("Genres:" + getGenres());
		sb.append(".");
		
		return sb.toString();
	}
}
