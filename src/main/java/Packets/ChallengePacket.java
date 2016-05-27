package Packets;

/**
 * Sent out when issuing a challenge.
 */
public class ChallengePacket{
    public int challengerID;
    public int challengedID;

    public int challengeID;
    public boolean cancel=false;

    public ChallengePacket(){
        challengeID=-1;
    }
    public ChallengePacket(int userID)
    {
        this.challengedID = userID;
        challengeID=-1;
    }
    public ChallengePacket(int userID, int challengerID, int challengeID)
    {
        this.challengeID = challengeID;
        this.challengedID = userID;
        this.challengerID = challengerID;
    }
    public ChallengePacket(int userID, int challengeID)
    {
        this.challengeID = challengeID;
        this.challengedID = userID;
    }
}
