import java.io.Serializable;
import java.util.Date;

public class Note implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String title;
    private String content;
    private Date timestamp;
    private String owner;
    
    public Note(int id, String title, String content, String owner) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.owner = owner;
        this.timestamp = new Date();
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    
    @Override
    public String toString() {
        return "ID: " + id + " | Title: " + title + " | Owner: " + owner;
    }
    
    public String getFullDetails() {
        return "=== Note " + id + " ===\n" +
               "Title: " + title + "\n" +
               "Owner: " + owner + "\n" +
               "Created: " + timestamp + "\n" +
               "Content: " + content;
    }
}