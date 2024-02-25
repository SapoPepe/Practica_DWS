package DWS.practica_dws.model;

public class Comment {
    private long CID;
    private String userName;
    private int score;
    private String opinion;

    public Comment(String userName, int score, String opinion) {
        this.userName = userName;
        this.score = score;
        this.opinion = opinion;
    }

    public String getUser(){
        return this.userName;
    }
}
