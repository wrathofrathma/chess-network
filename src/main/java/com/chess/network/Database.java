package com.chess.network;


import com.mongodb.*;

import java.net.UnknownHostException;

/**
 * This will be our primary interface to our database.
 * The database for this project uses MongoDB, not because it was the best tool for the job, I believe SQL would be best.
 * However MonogoDB seems like a good choice to learn for future projects when we don't know of the relations and structure of our DB.
 *
 */
public class Database {
    /* Our database specific variables */
    private static String dbAddr = "localhost";
    //private static int dbPort = 12121; //Palindromic prime ^_^
    private static int dbPort = 27017;
    private static String dbName = "chess-network";
    private static DB database;
    private static MongoClient mongoClient;

    public Database(){
        /* We should connect to our DB immediately */
        try {
            /* This sets up the connection to our server */
            mongoClient = new MongoClient(dbAddr,dbPort);
            /* This actually connects to our database - If it doesn't exist, it'll be created */
            database = mongoClient.getDB(dbName);
            System.out.println("Connected to database successfully!");

        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
    }
    //TODO create methods for connecting to custom database ports and names.
    /* Collections are essentially the first ID of which data is stored
     * For example, Users would be a collection, holding small tables.
     * Areas / Area levels could be another collection
     * */

    /* We'll probably be managing players by username as a key identifier - This method should recreate a player item based on the database info
    * Returns null if the player doesn't exist.
    * */
    public User getUser(String username)
    {
        DBCollection users = database.getCollection("USERS");
        /* We need to create a temporary object of what we're looking for.
         * This searches the username field for the given username */
        BasicDBObject query = new BasicDBObject("username",username);
        BasicDBObject projection = new BasicDBObject().append("username",1).append("password",1);
        /* We use find() instead of findOne() due to findOne reading the full object wasting time */
        DBCursor cursor = users.find(query,projection).limit(1);
        /* Check if the user exists or the cursor has a value */

        if(cursor.size()<=0)
            return null;
        BasicDBObject userObject = (BasicDBObject)cursor.next();
        User user = new User();
        user.setUsername((String)userObject.get("username"));
        user.setPassword((String)userObject.get("password"));
        return user;
    }

    /* Adding a user - Returns false if the user exists or if it failed to create. Returns true if the add was completed.*/
    public boolean addUser(String username, String password)
    {
        /* We should probably check if the player exists */
        if(getUser(username)!=null)
        {
            return false;
        }
        DBCollection users = database.getCollection("USERS");
        BasicDBObject newUser = new BasicDBObject();
        newUser.put("username",username);
        newUser.put("password",password);
        users.insert(newUser);
        return true;
    }
}
