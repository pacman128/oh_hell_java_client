package ohhell.game;

public final class PlayerState {

    private final String name;

    private int score;

    private int bid = -1;

    private int numTricks = 0;

    public PlayerState(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public PlayerState(String name) {
        this(name, 0);
    }

    public void clear() {
        bid = -1;
        numTricks = 0;
    }

    public void updateScore(int delta) {
        score += delta;
    }

    public void wonTrick() {
        numTricks++;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

    public int getTricksMade() {
        return numTricks;
    }
}
