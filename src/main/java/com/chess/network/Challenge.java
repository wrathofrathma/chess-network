package com.chess.network;

import Packets.ChallengePacket;

import java.util.Random;

/**
 * Small container for player vs player challenges
 */
public class Challenge {
    public Challenge(ChallengePacket challengePacket, ChessNetwork network)
    {
        this.network=network;
        this.challengedID = challengePacket.challengedID;
        this.challengerID = challengePacket.challengerID;

        this.challenger = getPlayer(challengerID);
        this.challenged = getPlayer(challengedID);

        this.challengeID=challengePacket.challengeID;

    }
    public Challenge(){}

    public Player getPlayer(int id)
    {
        for(int i=0; i<network.connectedPlayers.size(); i++)
        {
            if(network.connectedPlayers.get(i).connection.getID()==id) {
                return network.connectedPlayers.get(i);
            }
        }
        System.err.println("We /should/ never encounter this. But we didn't find the player.");
        throw new NullPointerException();
    }
    public void accept()
    {
        challengedAccepted = true;
    }

    //Can probably get rid of the IDs if I just do it this way.
    public ChessNetwork network;
    public int challengeID;
    public int challengerID;
    public int challengedID;
    public boolean challengedAccepted;
    public Player challenger;
    public Player challenged;
}
