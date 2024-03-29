package com.example.ihuntwithjavalins.Player;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ihuntwithjavalins.QRCode.QRCode;
import com.example.ihuntwithjavalins.QRCode.QRCodeDB;
import com.example.ihuntwithjavalins.common.DBConnection;
import com.example.ihuntwithjavalins.common.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * PlayerDB is a class which handles all database operations for Player objects.
 * Much functionality is derived from Well Fed project example given by TA
 * Design Patterns:
 * observer pattern - onCompleteListener
 *
 * @version 1.0
 */
public class PlayerDB {
    /**
     * Holds the tag for logging
     */
    private final static String myTAG = "PlayerDB";
    /**
     * Holds the instance of the Firebase Firestore database
     */
    private FirebaseFirestore db;
    /**
     * Holds the instance of the QRCodeDB
     */
    private QRCodeDB codeDB;
    /**
     * Holds the CollectionReference for the user collection
     */
    private CollectionReference collection;
    /**
     * Holds the string of user's username
     */
    private String userUsername;

    /**
     * Constructor for the PlayerDB class, initializes declared fields
     *
     * @param connection the DBConnection object used to access the database
     */
    public PlayerDB(DBConnection connection) {
        // Gets the database and user collection based on current connection
        db = connection.getDB();
        collection = connection.getUserCollection();

        // Create new instance of QRCodeDB based on current connection
        if (connection.getUsername() != null) {
            codeDB = new QRCodeDB(connection);
        }
        userUsername = connection.getUsername();
    }


    /**
     * Adds a player to the database(Use lambda to retrieve)
     * Citation: Batch Update Info https://cloud.google.com/firestore/docs/samples/firestore-data-batch-writes
     *
     * @param player   the Player object being added to the database
     * @param listener the listener to call when the player is added
     */
    public void addPlayer(@NonNull Player player, OnCompleteListener<Player> listener) {


        WriteBatch batch = db.batch();

        // add player info to batch
        String playerUsername = player.getUsername();
        DocumentReference playerRef = collection.document(playerUsername);
        Map<String, Object> item = new HashMap<>();
        item.put("Email", player.getEmail());
        item.put("Region", player.getRegion());
        item.put("Date Joined", player.getDateJoined());
        batch.set(playerRef, item);

        // commits batch writes to firebase
        batch.commit().addOnCompleteListener(task -> {
            Log.d(myTAG, "addPlayer:onComplete");
            if (task.isSuccessful()) {
                Log.d(myTAG, ":isSuccessful:" + playerUsername);
                listener.onComplete(player, true);
            } else {
                Log.d(myTAG, ":isFailure:" + playerUsername);
                listener.onComplete(player, false);
            }
        });
    }

    /**
     * Gets the player from the database(Use lambda to retrieve)
     *
     * @param selectedPlayer the player who's document is being accessed
     * @param listener       the listener to call when the player is found
     */
    public void getPlayer(Player selectedPlayer, OnCompleteListener<Player> listener) {
        String playerUsername = selectedPlayer.getUsername();
        DocumentReference playerRef = collection.document(playerUsername);
        playerRef.get().addOnCompleteListener(task -> {
            Log.d(myTAG, "getPlayer:onComplete");
            if (task.isSuccessful()) {
                Log.d(myTAG, ":isSuccessful:" + playerUsername);
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(myTAG, ":exists:" + playerUsername);
                    Player player = new Player();
                    player.setUsername(document.getId());
                    player.setEmail(document.getString("Email"));
                    player.setRegion(document.getString("Region"));
                    player.setDateJoined(document.getString("Date Joined"));
                    listener.onComplete(player, true);
                } else {
                    Log.d(myTAG, ":notExists:" + playerUsername);
                    listener.onComplete(null, false);
                }
            } else {
                Log.d(myTAG, ":isFailure:" + playerUsername);
                listener.onComplete(null, false);
            }

        });
    }

    /**
     * Gets all player data from database
     *
     * @param listener the listener to call after data retrieval
     */
    public void getAllPlayers(OnCompleteListener<ArrayList<Player>> listener) {
        collection.get().addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<Player> playerList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Log.d(myTAG, doc.getId() + " => " + doc.getData());

                        Player player = new Player();
                        player.setUsername(doc.getId());
                        player.setEmail(doc.getString("Email"));
                        player.setRegion(doc.getString("Region"));
                        player.setDateJoined(doc.getString("Date Joined"));
                        getPlayerCodes(player, (foundCodes, foundSuccess) -> {
                            if (foundSuccess) {
                                player.setCodes(foundCodes);
                                playerList.add(player);
                            } else {
                                Log.d(myTAG, "Getting codes failed");
                            }
                        });
                    }
                    listener.onComplete(playerList, true);
                } else {
                    Log.d(myTAG, "Error getting documents: ", task.getException());
                    listener.onComplete(null, false);
                }
            }
        });
    }

    /**
     * Deletes the player from the database(Use lambda to retrieve)
     *
     * @param player   the Player object representing the player to delete from database
     * @param listener the listener to call when the player is deleted
     */
    public void deletePlayer(@NonNull Player player, OnCompleteListener<Player> listener) {
        WriteBatch batch = db.batch();

        String playerUsername = player.getUsername();
        DocumentReference userDocument = collection.document(playerUsername);
        batch.delete(userDocument);

        batch.commit().addOnCompleteListener(task -> {
            Log.d(myTAG, "deletePlayer:onComplete");
            if (task.isSuccessful()) {
                Log.d(myTAG, ":isSuccessful:" + playerUsername);
                listener.onComplete(player, true);
            } else {
                Log.d(myTAG, ":isFailure:" + playerUsername);
                listener.onComplete(player, false);
            }
        });
    }

    /**
     * Updates the given player's email in the database
     *
     * @param player   the given player to update
     * @param newEmail the new email
     * @param listener the listener to call when the player is updated
     */
    public void updatePlayerEmail(Player player, String newEmail, OnCompleteListener<Player> listener) {
        String playerUsername = player.getUsername();
        DocumentReference playerRef = collection.document(playerUsername);
        playerRef
                .update("Email", newEmail)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        player.setEmail(newEmail);
                        listener.onComplete(player, true);
                        Log.d(myTAG, "Email successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onComplete(player, false);
                        Log.w(myTAG, "Error updating document", e);
                    }
                });
    }

    /**
     * Updates the given player's phone number in the database
     *
     * @param player    the given player
     * @param newRegion the new region
     * @param listener  the listener to call when the player is updated
     */
    public void updatePlayerRegion(Player player, String newRegion, OnCompleteListener<Player> listener) {
        String playerUsername = player.getUsername();
        DocumentReference playerRef = collection.document(playerUsername);
        playerRef
                .update("Region", newRegion)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        player.setRegion(newRegion);
                        listener.onComplete(player, true);
                        Log.d(myTAG, "Region successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onComplete(player, false);
                        Log.w(myTAG, "Error updating document", e);
                    }
                });
    }

    /**
     * Gets reference to player document
     *
     * @param player the Player object's document to be retrieved
     * @return reference to given player document
     */
    public DocumentReference getDocumentReference(Player player) {
        return collection.document(player.getUsername());
    }

    /**
     * Gets query of sorted player documents(Use .get to retrieve data)
     *
     * @param field     the field to sort player documents by
     * @param ascending true if sorting in ascending order, false if sorting in descending order
     * @return the query of sorted player documents
     */
    public Query getSortedPlayers(String field, Boolean ascending) {
        return collection.orderBy(field, ascending ? Query.Direction.ASCENDING : Query.Direction.DESCENDING);    // Running .get on the Query should give you the sorted data
    }

    /**
     * Adds given code to player code collection in database
     *
     * @param code     given code to add
     * @param listener the listener to call when code is added
     */
    public void playerAddQRCode(QRCode code, OnCompleteListener<QRCode> listener) {
        codeDB.getCode(code, (foundCode, success) -> {
            if (foundCode == null) {
                codeDB.addQRCode(code, (addedCode, success2) -> {
                    if (addedCode != null) {
                        listener.onComplete(addedCode, true);
                    } else {
                        listener.onComplete(null, false);
                    }
                });
            }
        });
    }

    /**
     * Deletes given code from player code collection in database
     *
     * @param code     given code to delete
     * @param listener the listener to call when the code is deleted
     */
    public void playerDelQRCode(QRCode code, OnCompleteListener<QRCode> listener) {
        codeDB.getCode(code, (foundCode, success) -> {
            if (foundCode != null) {
                codeDB.deleteCode(code, (deletedCode, success2) -> {
                    if (deletedCode != null) {
                        listener.onComplete(deletedCode, true);
                    } else {
                        listener.onComplete(null, false);
                    }
                });
            }
        });
    }

    /**
     * Returns a list of QRCodes the user owns
     *
     * @param listener an on complete listener for a QRCOde array
     */
    public void getUserCodes(OnCompleteListener<ArrayList<QRCode>> listener) {
        codeDB.getCodes((codeList, success) -> {
            if (success) {
                listener.onComplete(codeList, true);
            } else {
                listener.onComplete(null, false);
            }
        });
    }

    /**
     * Returns a list of QRCodes the given player owns
     *
     * @param player   the given player who owns the codes
     * @param listener the on complete listener for a list of QRCodes
     */
    public void getPlayerCodes(Player player, OnCompleteListener<ArrayList<QRCode>> listener) {
        String playerUsername = player.getUsername();
        codeDB.switchFromPlayerToPlayerCodes(playerUsername);
        codeDB.getCodes((codeList, success) -> {
            if (success) {
                listener.onComplete(codeList, true);
            } else {
                listener.onComplete(null, false);
            }
        });
        codeDB.switchFromPlayerToPlayerCodes(userUsername);
    }

}
