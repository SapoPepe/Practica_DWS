package DWS.practica_dws.model;

public class Comment {
    private String userName;
    private int score;
    private String comment;

    public Comment(String userName, int score, String comment) {
        this.userName = userName;
        this.score = score;
        this.comment = comment;
    }

    public String getUser(){
        return this.userName;
    }
}
