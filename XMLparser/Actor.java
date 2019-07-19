

public class Actor {

	private String name;
	private String birthYear;
    
	public Actor(){
		
	}
	
	public Actor(String name, String birthYear) {
        this.name = name;
        this.birthYear = birthYear;
		
	}
    

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    
	public String getDob() {
		return birthYear;
	}

	public void setDob(String birthYear) {
		this.birthYear = birthYear;
	}	
	
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Actor Details - ");
		sb.append("Name:" + getName());
		sb.append(", ");
		sb.append("Birth Year:" + getDob());
		sb.append(".");
		
		return sb.toString();
	}
}
