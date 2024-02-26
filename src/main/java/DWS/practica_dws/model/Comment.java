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

    public int getCID() {
        return CID;
    }

    public void setCID(int CID) {
        this.CID = CID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }
}
