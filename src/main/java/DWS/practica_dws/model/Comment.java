package DWS.practica_dws.model;

import jakarta.persistence.*;

import java.security.Principal;

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

    public Comment(Principal principal, Integer score, String opinion) {
        this.userName = principal.getName();
        this.score = score;
        this.opinion = opinion;
    }

    public long getCID() {
        return CID;
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

    public boolean hasPerson(String name){
        return this.userName.equals(name);
    }
}
