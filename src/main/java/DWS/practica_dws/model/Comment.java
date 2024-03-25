package DWS.practica_dws.model;

import jakarta.persistence.*;

@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long CID;

    private String userName;
    private int score;
    private String opinion;

    public Comment() {
    }

    public Comment(String userName, Integer score, String opinion) {
        this.userName = userName;
        this.score = score;
        this.opinion = opinion;
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
