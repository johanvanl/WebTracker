package za.ac.sun.cs.webtracker.database;

public class Website {

	private int ID = -1;
	private String url = null;
	private String title = null;

	public Website() {
		this.url = "";
		this.title = "";
	}

	public Website(int ID, String url, String title) {
		this.ID = ID;
		this.url = url;
		this.title = title;
	}

	public void setId(int ID) {
		this.ID = ID;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getID() {
		return this.ID;
	}

	public String getUrl() {
		return this.url;
	}

	public String getTitle() {
		return this.title;
	}
	
	@Override
	public String toString() {
		return Integer.toString(ID) + " " + url + " " + title;
	}

}
