

public class Cast {

	private String movie;
	private String actor;
    
	public Cast(){
		
	}
	
	public Cast(String movie, String actor) {
        this.movie = movie;
        this.actor = actor;
		
	}
    

	public String getMovie() {
		return movie;
	}

	public void setMovie(String movie) {
		this.movie = movie;
	}

    
	public String getActor() {
		return actor;
	}

	public void setActor(String actor) {
		this.actor = actor;
	}	
	
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Cast Details - ");
		sb.append("Movie:" + getMovie());
		sb.append(", ");
		sb.append("Actor:" + getActor());
		sb.append(".");
		
		return sb.toString();
	}
}
