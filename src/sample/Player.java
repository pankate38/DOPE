package sample;

public class Player {
    private String name;
    private int lives;
    private int powerUpUses;
    private int playerPoints;

    public Player(String n, int l, int u, int p){
        name = n;
        lives = l;
        powerUpUses = u;
    }

    public String getName(){
        return name;
    }

    public int getLives(){
        return lives;
    }

    public void setLives(int l){
        lives = l;
    }

    public int getPlayerPoints(){
        return playerPoints;
    }

    public void setPlayerPoints(int p){
        playerPoints = p;
    }

    public int getPowerUpUses(){
        return powerUpUses;
    }

    public void setPowerUpUse(int p){
        powerUpUses = p;
    }

}
