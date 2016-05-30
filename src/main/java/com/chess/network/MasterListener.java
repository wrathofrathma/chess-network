package com.chess.network;

import Packets.*;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.Random;
import java.util.Vector;


public class MasterListener extends Listener {
    public ChessNetwork chessNetwork;
    public MasterListener(ChessNetwork chessNetwork)
    {
        this.chessNetwork = chessNetwork;
    }

    public void connected(Connection connection)
    {
        //We need to handle incoming connections. This is where we can handle the initial handshake and player queue.
        /* At a later date, we'll add a handshake & authentication */
        chessNetwork.connectedPlayers.add(new Player(connection));
        chessNetwork.server.sendToAllTCP(new PlayerListPacket(chessNetwork.connectedPlayers));
    }
    public void disconnected(Connection connection)
    {
        /* Removing from the connection list */
        for(int i=0; i<chessNetwork.connectedPlayers.size(); i++)
        {
            if(chessNetwork.connectedPlayers.get(i).connection.getID()==connection.getID()) {
                chessNetwork.connectedPlayers.get(i).connection.close();
                chessNetwork.connectedPlayers.remove(i);
            }
        }

        Vector<Challenge> removal = new Vector<Challenge>();
        /* Removing from the challenge queue */
        synchronized (chessNetwork.openChallenges) {
            for (Challenge challenge : chessNetwork.openChallenges) {
                if (challenge.challengedID == connection.getID() || challenge.challengerID == connection.getID()) {
                    removal.add(challenge);
                }
            }
            for (Challenge challenge : removal) {
                chessNetwork.openChallenges.remove(challenge);
            }
        }
        //Notifying other clients of the change in connected users
        chessNetwork.server.sendToAllTCP(new PlayerListPacket(chessNetwork.connectedPlayers));
    }
    public void received(Connection connection, Object object)
    {
        if(object instanceof MessagePacket)
        {
            MessagePacket messagePacket = (MessagePacket) object;
            System.out.println(messagePacket.message);
            for(Player player : chessNetwork.connectedPlayers) {
                if (player.connection.getID() != connection.getID()) {
                    player.connection.sendTCP(messagePacket);
                }
            }

        }
        else if(object instanceof IdentPacket)
        {
            IdentPacket packet = (IdentPacket) object;
            boolean identFound = false;
            for(Player player : chessNetwork.connectedPlayers)
            {
                //Can't have two players of the same name.
                if(player.nick.equals(packet.username))
                {
                    identFound = true;
                }
            }
            if(!identFound){
            for(Player player : chessNetwork.connectedPlayers) {
                if (connection.getID() == player.connection.getID()) {
                    player.nick = packet.username;
                    connection.sendTCP(new IdentPacket(true));
                    chessNetwork.server.sendToAllTCP(new PlayerListPacket(chessNetwork.connectedPlayers));
                }

            }   }
        }
        else if(object instanceof String)
        {
            String message = (String) object;
            System.out.println(message);
            connection.sendTCP(new String("Server received String from client ID: " + connection.getID()));
        }
        else if(object instanceof RequestPacket)
        {
            RequestPacket rp = (RequestPacket) object;
            Player p;
            int index=chessNetwork.checkPlayerExist(connection.getID());
            if(index!=-1)
            {
                p = chessNetwork.connectedPlayers.get(index);
            }
            else
            {
                p = new Player(connection);
            }
            switch(rp.request){
                case 0: //OH okay.
                    connection.sendTCP(new PlayerListPacket(chessNetwork.connectedPlayers));
                    break;
                case 1:
                    connection.sendTCP(new PlayerInfoPacket(p.nick, p.connection.getID(),true));
                    break;
                case 2:
                    //TODO make a loop of sending these or better yet, let's just send an array of challenges.
                    for(Challenge challenge : chessNetwork.openChallenges)
                    {
                        if(challenge.challengerID==connection.getID() || challenge.challengedID==connection.getID())
                            connection.sendTCP(new ChallengePacket(challenge.challengedID,challenge.challengerID,challenge.challengeID));
                    }
                    break;
            }
        }
        else if(object instanceof ChallengePacket) {
            /* Good god this is ugly. Let's actually fix this
            *
            * */
            ChallengePacket packet = (ChallengePacket) object;
            synchronized (chessNetwork.openChallenges) {
                if (packet.challengeID == -1) {
                    //TODO create challenge
                    Random rng = new Random();

                /* To find a unique ID that isn't based on an index of an array we use RNG
                 * This is meant to generate a random number 0-100000.
                 * If the ID is taken, then the loop tries again.
                 * If it makes it through the loop without matching another ID, then challenge is created
                 */
                    boolean uniqueID = false;
                    int challengeID = -1;

                    while (!uniqueID) {
                        uniqueID = true;
                        challengeID = rng.nextInt(100000);
                        for (Challenge challenge : chessNetwork.openChallenges) {
                            if (challenge.challengeID == challengeID)
                                uniqueID = false;
                        }
                    }
                    System.out.println("Created a unique challenge ID of : " + challengeID);
                /* To save time, we'll construct the ChallengePacket here then generate the Challenge off of that */
                    ChallengePacket challengePacket = new ChallengePacket(packet.challengedID, packet.challengerID, challengeID);
                    //Creating the challenge serverside
                    chessNetwork.openChallenges.add(new Challenge(challengePacket, chessNetwork));
                    chessNetwork.server.sendToTCP(challengePacket.challengedID, challengePacket);
                } else {
                    for (Challenge challenge : chessNetwork.openChallenges) {
                        if (challenge.challengeID == packet.challengeID) {
                            if (packet.cancel == true) {
                                //TODO Watch me for ConcurrentAccessExceptions!!!!
                                chessNetwork.openChallenges.remove(challenge);
                            } else {
                                //TODO matched the challenge ID. treat as a challenge.accept()!

                            }
                        }

                    }
                }
            }
        }
        else if(object instanceof ChallengeAcceptPacket)
        {
            /* Remove the challenge from the list
             * Create new game instance
             */
            ChallengeAcceptPacket packet = (ChallengeAcceptPacket) object;
            System.out.println("User ID that sent the ChallengeAccept: " + connection.getID() + " with challenge ID: " + packet.challengeID);

            int index=0;
            Challenge removal=null;
            synchronized (chessNetwork.openChallenges) {
                for (Challenge challenge : chessNetwork.openChallenges) {
                    if (challenge.challengeID == packet.challengeID && packet.challengedID == challenge.challengedID) {
                        challenge.challenger.inGame = true;
                        challenge.challenged.inGame = true;
                        chessNetwork.gameRooms.add(new GameRoom(challenge.challenger, challenge.challenged, chessNetwork));
                        removal=challenge; //Shouldn't break things....? Not sure since it's synced now
                    } else
                        index++;
                }
               //TODO remove challenge
                if(removal!=null)
                    chessNetwork.openChallenges.remove(removal);
                //chessNetwork.openChallenges.remove(index);
            }
         }
        //TODO We need to check for the boardID, which currently won't ever be assigned.
        else if(object instanceof BoardPosition)
        {
            BoardPosition packet = (BoardPosition)object;
            if(packet.request)
            {
                /* Find game ID then send current position fully */
                for(GameRoom room : chessNetwork.gameRooms)
                {
                    if(room.gameID==packet.gameID)
                    {
                        if(room.board!=null)
                        {
                            chessNetwork.server.sendToTCP(connection.getID(),room.board);
                        }
                        else
                            chessNetwork.server.sendToTCP(connection.getID(),new BoardPosition(room.gameID));
                    }
                }
            }
            else
            {
                //Why would they send me a BoardPosition packet if I didn't request it?
                //Do nothing
            }
        }
    }


}
