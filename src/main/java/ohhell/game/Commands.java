package ohhell.game;

public enum Commands {
    LOGIN("LOGIN"),
    LOGOUT("LOGOUT"),
    NEW_PLAYER("NEW_PLAYER"),
    START_GAME("START_GAME"),
    NEW_HAND("NEW_HAND"),
    BID("BID"),
    BID_ANNOUNCE("BIN_ANNOUNCE"),
    BADBID("BADBID"),
    DRAW("DRAW"),
    DEAL_OVER("DEAL_OVER"),
    GET_CARD("GET_CARD"),
    PLAY_CARD("PLAY_CARD"),
    CARD_PLAYED("CARD_PLAYED"),
    TRICK_WINNER("TRICK_WINNER"),
    END_HAND("END_HAND"),
    GAME_OVER("GAME_OVER"),
    OK("OK"),
    ERROR("ERROR");

    private final String text;

    Commands(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
