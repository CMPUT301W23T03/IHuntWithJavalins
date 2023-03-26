package com.example.ihuntwithjavalins.Scoreboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ihuntwithjavalins.Player.Player;
import com.example.ihuntwithjavalins.QRCode.QRCode;
import com.example.ihuntwithjavalins.R;
import com.google.firebase.firestore.auth.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ShowIndividualCodes is responsible for showing the details of each QRCode
 */
public class ShowIndividualCodes extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scoreboard_main_individual);

        TextView user_name = findViewById(R.id.userName);
        String total_points_placing="";


//        String UserName = getIntent().getStringExtra("USER");
        // Get the intent from the previous activity
        Intent myIntent = getIntent();
        Player thisPlayer = (Player) myIntent.getSerializableExtra("focusedPlayer");

//        Toast.makeText(this, thisPlayer.getUsername(), Toast.LENGTH_SHORT).show();
        Button see_user_profile = findViewById(R.id.player_profile_btn);
        String btn_txt = "See "+ thisPlayer.getUsername()+"'s Profile";
        see_user_profile.setText(btn_txt);

        ArrayList<StoreNamePoints> storageList = (ArrayList<StoreNamePoints>) getIntent().getSerializableExtra("codes");
        CustomListForQRCodeCustomAdapter storageAdapter = new CustomListForQRCodeCustomAdapter(this, storageList);

        ListView listView = findViewById(R.id.code_list);
        listView.setAdapter(storageAdapter);

        user_name.setText(thisPlayer.getUsername());

        ImageButton go_back = findViewById(R.id.go_back_btn);
        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        see_user_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Player> player_details = new ArrayList<Player>();
                Intent start_view_user_activity = new Intent(ShowIndividualCodes.this,ShowUserProfile.class);
                String date = thisPlayer.getDateJoined();
                String date_joined = "";
                if (date!=null){
                    String[] months = {
                            "January",
                            "February",
                            "March",
                            "April",
                            "May",
                            "June",
                            "July",
                            "August",
                            "September",
                            "October",
                            "November",
                            "December"
                    };


                    String year = date.substring(0,4);
                    String month = date.substring(4,6);
                    String day = date.substring(6,8);

                    // Convert the day from a string to an integer
                    int dayInt = Integer.parseInt(day);

                    // Get the day suffix
                    String daySuffix;
                    if (dayInt % 10 == 1 && dayInt != 11) {
                        daySuffix = "st";
                    } else if (dayInt % 10 == 2 && dayInt != 12) {
                        daySuffix = "nd";
                    } else if (dayInt % 10 == 3 && dayInt != 13) {
                        daySuffix = "rd";
                    } else {
                        daySuffix = "th";
                    }

                    // Get the month name from the array
                    int monthInt = Integer.parseInt(month);
                    String monthName = months[monthInt - 1];

                    // Build the final date string
                    date_joined = dayInt + daySuffix + " " + monthName + ", " + year;
                }
                else{
                    date_joined="No date";
                }

                List<QRCode> allCodes = thisPlayer.getCodes();
                int highestCode = 0;

                for (QRCode code : allCodes) {
                    int value = Integer.parseInt(code.getCodePoints());
                    if (value > highestCode) {
                        highestCode = value;
                    }
                }

                UserProfileDetails user = new UserProfileDetails(thisPlayer.getUsername(), date_joined, thisPlayer.getEmail(),thisPlayer.getRegion(),Integer.toString(thisPlayer.getSumOfCodePoints()),getTotalPointsPlacing(),Integer.toString((thisPlayer.getCodes().size())),getTotalCodesPlacing(),Integer.toString(highestCode),getHighestCodePlacing());
                start_view_user_activity.putExtra("PlayerDetails",(Serializable)user);
                startActivity(start_view_user_activity);
            }
        });

    }
    public String getTotalPointsPlacing(){
        String total_points_placing="";
        Intent myIntent = getIntent();
        Player thisPlayer = (Player) myIntent.getSerializableExtra("focusedPlayer");

        ArrayList<Player> players = (ArrayList<Player>) getIntent().getSerializableExtra("playerList");
        // Find the top 5%, 10%, and 15% of the list
        int top5Percent = (int) Math.round(players.size() * 0.05);
        int top10Percent = (int) Math.round(players.size() * 0.1);
        int top15Percent = (int) Math.round(players.size() * 0.15);
        Collections.sort(players, new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player p2) {
                return Integer.compare(p2.getSumOfCodePoints(), p1.getSumOfCodePoints());
            }
        });


        // Search for a player's name and find which percentage their score falls in
        String playerName = thisPlayer.getUsername(); // Replace with the name you want to search for
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getUsername().equals(playerName)) {
                int playerScore = players.get(i).getSumOfCodes();
//                Toast.makeText(this, Integer.toString(i), Toast.LENGTH_SHORT).show();
                if (i < top5Percent) {
                    if (i == 0) {
                        total_points_placing = "Leader(Position:" + (i+1) + ")";
                    } else {
//                        Toast.makeText(this, playerName + " is in the top 5% with a score of " + playerScore, Toast.LENGTH_SHORT).show();
                        total_points_placing = "Gold(Position:" + (i+1) + ")";
                    }
                } else if (i+1 <= top10Percent && i+1 >= top5Percent) {
//                    Toast.makeText(this, playerName + " is in the top 10% with a score of " + playerScore, Toast.LENGTH_SHORT).show();
                    total_points_placing = "Silver(Position:" + (i+1) + ")";
                } else if (i+1 <= top15Percent && i+1 >= top10Percent) {
//                    Toast.makeText(this, playerName + " is in the top 15% with a score of " + playerScore, Toast.LENGTH_SHORT).show();
                    total_points_placing = "Bronze(Position:" + (i+1) + ")";
                } else {
//                    Toast.makeText(this, playerName + " is outside of the top 15% with a score of " + playerScore, Toast.LENGTH_SHORT).show();
                    total_points_placing = "Position:"+Integer.toString(i+1);
                }

                break;
            }
        }
        return total_points_placing;
    }

    public String getTotalCodesPlacing() {
        String total_points_placing = "";
        Intent myIntent = getIntent();
        Player thisPlayer = (Player) myIntent.getSerializableExtra("focusedPlayer");

        ArrayList<Player> players = (ArrayList<Player>) getIntent().getSerializableExtra("playerList");
        // Find the top 5%, 10%, and 15% of the list
        int top5Percent = (int) Math.round(players.size() * 0.05);
        int top10Percent = (int) Math.round(players.size() * 0.1);
        int top15Percent = (int) Math.round(players.size() * 0.15);
        Collections.sort(players, new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player p2) {
                return Integer.compare(p2.getSumOfCodes(), p1.getSumOfCodes());
            }
        });


        // Search for a player's name and find which percentage their score falls in
        String playerName = thisPlayer.getUsername(); // Replace with the name you want to search for
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getUsername().equals(playerName)) {
                int playerScore = players.get(i).getSumOfCodes();
//                Toast.makeText(this, Integer.toString(i), Toast.LENGTH_SHORT).show();
                if (i < top5Percent) {
                    if (i == 0) {
                        total_points_placing = "Leader(Position:" + (i + 1) + ")";
                    } else {
//                        Toast.makeText(this, playerName + " is in the top 5% with a score of " + playerScore, Toast.LENGTH_SHORT).show();
                        total_points_placing = "Gold(Position:" + (i + 1) + ")";
                    }
                } else if (i + 1 <= top10Percent && i + 1 >= top5Percent) {
//                    Toast.makeText(this, playerName + " is in the top 10% with a score of " + playerScore, Toast.LENGTH_SHORT).show();
                    total_points_placing = "Silver(Position:" + (i + 1) + ")";
                } else if (i + 1 <= top15Percent && i + 1 >= top10Percent) {
//                    Toast.makeText(this, playerName + " is in the top 15% with a score of " + playerScore, Toast.LENGTH_SHORT).show();
                    total_points_placing = "Bronze(Position:" + (i + 1) + ")";
                } else {
//                    Toast.makeText(this, playerName + " is outside of the top 15% with a score of " + playerScore, Toast.LENGTH_SHORT).show();
                    total_points_placing = "Position:" + Integer.toString(i + 1);
                }

                break;
            }
        }
        return total_points_placing;
    }


    public String getHighestCodePlacing(){
        String total_points_placing="";
        Intent myIntent = getIntent();
        Player thisPlayer = (Player) myIntent.getSerializableExtra("focusedPlayer");

        ArrayList<Player> players = (ArrayList<Player>) getIntent().getSerializableExtra("playerList");
        // Find the top 5%, 10%, and 15% of the list
        int top5Percent = (int) Math.round(players.size() * 0.05);
        int top10Percent = (int) Math.round(players.size() * 0.1);
        int top15Percent = (int) Math.round(players.size() * 0.15);
        Collections.sort(players, new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player p2) {
                return Integer.compare(p2.getHighestCode(), p1.getHighestCode());
            }
        });


        // Search for a player's name and find which percentage their score falls in
        String playerName = thisPlayer.getUsername(); // Replace with the name you want to search for
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getUsername().equals(playerName)) {
                int playerScore = players.get(i).getSumOfCodes();
//                Toast.makeText(this, Integer.toString(i), Toast.LENGTH_SHORT).show();
                if (i < top5Percent) {
                    if (i == 0) {
                        total_points_placing = "Leader(Position:" + (i+1) + ")";
                    } else {
//                        Toast.makeText(this, playerName + " is in the top 5% with a score of " + playerScore, Toast.LENGTH_SHORT).show();
                        total_points_placing = "Gold(Position:" + (i+1) + ")";
                    }
                } else if (i+1 <= top10Percent && i+1 >= top5Percent) {
//                    Toast.makeText(this, playerName + " is in the top 10% with a score of " + playerScore, Toast.LENGTH_SHORT).show();
                    total_points_placing = "Silver(Position:" + (i+1) + ")";
                } else if (i+1 <= top15Percent && i+1 >= top10Percent) {
//                    Toast.makeText(this, playerName + " is in the top 15% with a score of " + playerScore, Toast.LENGTH_SHORT).show();
                    total_points_placing = "Bronze(Position:" + (i+1) + ")";
                } else {
//                    Toast.makeText(this, playerName + " is outside of the top 15% with a score of " + playerScore, Toast.LENGTH_SHORT).show();
                    total_points_placing = "Position:"+Integer.toString(i+1);
                }

                break;
            }
        }
        return total_points_placing;
    }
}
















