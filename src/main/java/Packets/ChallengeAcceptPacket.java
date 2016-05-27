package Packets;

/**
 * Created by rathma on 5/22/16.
 */
public class ChallengeAcceptPacket {
    public ChallengeAcceptPacket(){}

    public ChallengeAcceptPacket(int challengeID, int challengedID)
    {
        this.challengedID = challengedID;
        this.challengeID = challengeID;
    }
    public int challengedID;
    public int challengeID;
}
