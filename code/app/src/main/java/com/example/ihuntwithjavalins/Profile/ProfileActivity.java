package com.example.ihuntwithjavalins.Profile;

import static android.content.ContentValues.TAG;

import static java.util.concurrent.TimeUnit.SECONDS;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ihuntwithjavalins.Player.Player;
import com.example.ihuntwithjavalins.Player.PlayerController;
import com.example.ihuntwithjavalins.QRCode.QRCode;
import com.example.ihuntwithjavalins.QuickNavActivity;
import com.example.ihuntwithjavalins.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class represents the activity where the user can view their profile information.
 * It extends the AppCompatActivity class.
 * TODO: add algorithms for rankings
 */
public class ProfileActivity extends AppCompatActivity {

    private Button quickNavButton;
    private Button logoutButton;
    private TextView username;
    private TextView userdateJoined;
    private TextView userregion;
    private TextView userEmail;
    private TextView totalPoints;
    private TextView totalPointsPlacing;
    private TextView totalCodes;
    private TextView totalCodesPlacing;
    private TextView highestCodeValue;
    private TextView highestCodeValuePlacing;
    private PlayerController playerController;

    private String TAG = "Sample"; // used as starter string for debug-log messaging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        playerController = new PlayerController(this);
        setContentView(R.layout.my_profile_page);

        quickNavButton = findViewById(R.id.button_prf_quicknav);
        logoutButton = findViewById(R.id.button_prf_logout);

        username = findViewById(R.id.prf_username_data);
        userEmail = findViewById(R.id.prf_email_data);
        userdateJoined = findViewById(R.id.prf_datejoined_data);
        userregion = findViewById(R.id.prf_region_data);
        totalPoints = findViewById(R.id.prf_totalpoints_data);
        totalPointsPlacing = findViewById(R.id.prf_totalpointsplacing_data);
        totalCodes = findViewById(R.id.prf_totalcodes_data);
        totalCodesPlacing = findViewById(R.id.prf_totalcodesplacing_data);
        highestCodeValue = findViewById(R.id.prf_highestcodevalue_data);
        highestCodeValuePlacing = findViewById(R.id.prf_highestcodevalueplacing_data);

        // Get the intent from the previous activity
        Intent myIntent = getIntent();
        Player myPlayer = (Player) myIntent.getSerializableExtra("myPlayer");
        List<Player> players = (List<Player>) myIntent.getSerializableExtra("playerList");

        username.setText(myPlayer.getUsername());
        userEmail.setText(myPlayer.getEmail());
        userregion.setText(myPlayer.getRegion());
        userdateJoined.setText(myPlayer.getDateJoined());
        totalPoints.setText(String.valueOf(playerController.calculateTotalPoints(myPlayer)));
        totalCodes.setText(String.valueOf(playerController.getTotalCodes(myPlayer)));
        highestCodeValue.setText(String.valueOf(playerController.calculateHighestValue(myPlayer)));

        List<Player> regionalPlayers = playerController.getRegionalPlayers(myPlayer, players);
        String rankString = playerController.getRanking(myPlayer, players, "Everywhere: #", "points");
        rankString = playerController.getRanking(myPlayer, regionalPlayers, rankString + "\n" + "Regional: #", "points");
        totalPointsPlacing.setText(rankString);
        rankString = playerController.getRanking(myPlayer, players, "Everywhere: #", "sum");
        rankString = playerController.getRanking(myPlayer, regionalPlayers, rankString + "\n" + "Regional: #", "sum");
        totalCodesPlacing.setText(rankString);
        rankString = playerController.getRanking(myPlayer, players, "Everywhere: #", "high");
        rankString = playerController.getRanking(myPlayer, regionalPlayers, rankString + "\n" + "Regional: #", "high");
        highestCodeValuePlacing.setText(rankString);

//        // Access a Firestore instance
//        final FirebaseFirestore db = FirebaseFirestore.getInstance(); // pull instance of database from firestore
//        final CollectionReference collectionRef_Users = db.collection("Users"); // pull instance of specific collection in firestore
//        final DocumentReference docRef_thisPlayer = collectionRef_Users.document(myPlayer.getUsername()); // pull instance of specific collection in firestore
//
//        //https://firebase.google.com/docs/firestore/query-data/get-data
//        docRef_thisPlayer.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        Log.d(TAG, "DocumentSnapshot data" + document.getData());
//                        myPlayer.setEmail(document.getString("Email"));
//                        myPlayer.setRegion(document.getString("Region"));
//                        myPlayer.setDateJoined(document.getString("Date Joined"));
//                        username.setText(myPlayer.getUsername());
//                        userEmail.setText(myPlayer.getEmail());
//                        userregion.setText(myPlayer.getRegion());
//                        userdateJoined.setText(myPlayer.getDateJoined());
//                    } else {
//                        Log.d(TAG, "No such document");
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });
//
//        final CollectionReference subColRef_Codes = docRef_thisPlayer.collection("QRCodesSubCollection");
//        // This listener will pull the firestore data into your android app (if you reopen the app)
//        subColRef_Codes.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable
//            FirebaseFirestoreException error) {
//                codeList.clear(); // Clear the old list
//                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) { // Re-add firestore collection sub-documents and sub-sub-collection items)
//                    String codeHash = doc.getId();
//                    String codeName = (String) doc.getData().get("Code Name");
//                    String codePoints = (String) doc.getData().get("Point Value");
//                    String codeImgRef = (String) doc.getData().get("Img Ref");
//                    String codeLatValue = (String) doc.getData().get("Lat Value");
//                    String codeLonValue = (String) doc.getData().get("Lon Value");
//                    String codePhotoRef = (String) doc.getData().get("Photo Ref");
//                    String codeDate = (String) doc.getData().get("Date:");
//                    codeList.add(new QRCode(codeHash, codeName, codePoints, codeImgRef, codeLatValue, codeLonValue, codePhotoRef, codeDate));
//                }
//                // calc most codes and points
//                int totalPointsInt = 0;
//                int highestcodeval = 0;
//                for (QRCode code: codeList ) {
//                    totalPointsInt = totalPointsInt + Integer.parseInt(code.getCodePoints());
//                    if (Integer.parseInt(code.getCodePoints()) > highestcodeval) {
//                        highestcodeval = Integer.parseInt(code.getCodePoints());
//                    }
//                }
//                totalPoints.setText(String.valueOf(totalPointsInt));
//                totalCodes.setText(String.valueOf(codeList.size()));
//                highestCodeValue.setText(String.valueOf(highestcodeval));
//            }
//        });

        quickNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, QuickNavActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // https://stackoverflow.com/questions/3687315/how-to-delete-shared-preferences-data-from-app-in-android
                SharedPreferences mPrefs = getApplicationContext().getSharedPreferences("Login", Context.MODE_PRIVATE);
                mPrefs.edit().clear().commit();    // uncomment to easily clear the shared preferences for login testing

                Intent intent = new Intent(ProfileActivity.this, QuickNavActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

    }

}