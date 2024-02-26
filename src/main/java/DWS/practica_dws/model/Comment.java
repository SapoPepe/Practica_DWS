package DWS.practica_dws.model;

public class Comment {
    private int CID;
    private String userName;
    private int score;
    private String opinion;

    public Comment(String userName, int score, String opinion) {
        this.userName = userName;
        this.score = score;
        this.opinion = opinion;
    }

    public void setID(int CID){
        this.CID = CID;
    }
}
