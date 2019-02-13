package centennial.ca.helpstack.Model;

public class Post {
    private String postid;
    private String post_title;
    private String description;
    private String publisher;
    private String department;

    public Post(String postid, String post_title, String description, String publisher, String department) {
        this.postid = postid;
        this.post_title = post_title;
        this.description = description;
        this.publisher = publisher;
        this.department = department;
    }

    public Post() {
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPost_title() {
        return post_title;
    }

    public void setPost_title(String post_title) {
        this.post_title = post_title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
