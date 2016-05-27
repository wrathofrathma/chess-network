package Packets;

import java.util.Vector;
import com.chess.network.Player;

/**
 * Created by rathma on 5/22/16.
 */
public class PlayerListPacket {
    /* What we want to include here or send out
     **** Username
      *  UserID
     */
    public Vector<String> usernames;
    public boolean[] gameState;

    public PlayerListPacket(){}
    public int[] userID;
    public PlayerListPacket(Vector<Player> players)
    {
        usernames = new Vector<String>();
        gameState = new boolean[players.size()];
        userID = new int[players.size()];
        for(int i=0; i<players.size(); i++){
            usernames.add(players.get(i).nick);
            gameState[i]=players.get(i).inGame;
            userID[i]=players.get(i).connection.getID();
        }
    }
}
