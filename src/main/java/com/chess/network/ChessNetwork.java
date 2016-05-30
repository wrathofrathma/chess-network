package com.chess.network;

import Packets.*;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

/**
 * com.chess.network.Main Server Class
 *
 * Flow of the server
 * * -------------------
 * * INC Connection
 * * * Create new instance of com.chess.network.Player
 * * * Handshake
 * * * Add to player queue
 * * *
 *
 *
 *
 */
public class ChessNetwork {
    public Server server;
    int portno=7667;
    Vector<GameRoom> gameRooms; //May as well keep track of all of these.
    private boolean headless=false;
    private boolean isRunning=true;
    public Vector<Player> connectedPlayers;
    public Vector<Challenge> openChallenges;


    public int checkPlayerExist(int id)
    {
        for(int i=0; i<connectedPlayers.size(); i++)
        {
            if(connectedPlayers.get(i).connection.getID()==id)
                return i;
        }
        return -1;
    }

    public ChessNetwork()
    {
        server = new Server();
        connectedPlayers = new Vector<Player>();
        openChallenges = new Vector<Challenge>();
        gameRooms = new Vector<GameRoom>();
    }

    public void run()
    {
        server.start(); //Starts a separate thread. Contains an I/O loop.
        try {
            server.bind(portno);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
        server.getKryo().register(MessagePacket.class);
        server.getKryo().register(MovePacket.class);
        server.getKryo().register(RequestPacket.class);
        server.getKryo().register(PlayerListPacket.class);
        server.getKryo().register(Vector.class);
        server.getKryo().register(ChallengePacket.class);
        server.getKryo().register(ChallengeAcceptPacket.class);
        server.getKryo().register(boolean[].class);
        server.getKryo().register(CreateGamePacket.class);
        server.getKryo().register(PlayerInfoPacket.class);
        server.getKryo().register(int[].class);
        server.getKryo().register(int[][].class);
        server.getKryo().register(BoardPosition.class);
        server.getKryo().register(GameEndPacket.class);
        server.getKryo().register(ServerShutdownPacket.class);
        server.getKryo().register(PromotionPacket.class);
        server.getKryo().register(PromotionAccept.class);
        server.getKryo().register(IdentPacket.class);
        server.getKryo().register(SurrenderPacket.class);
        server.addListener(new MasterListener(this));


        if(!headless)
        {
            Scanner sc = new Scanner(System.in);
            String buffer;
            while(isRunning)
            {
                buffer = sc.nextLine();
                if(buffer.equals("stop")) {
                    isRunning = false;
                    server.sendToAllTCP(new ServerShutdownPacket("Server shutting down!\nHave a spiffy day! :D"));
                    server.stop();
                }
                server.sendToAllTCP(buffer);
            }
        }
    }

}
