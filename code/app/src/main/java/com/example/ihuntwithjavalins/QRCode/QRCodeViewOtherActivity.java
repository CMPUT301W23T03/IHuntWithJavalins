package com.example.ihuntwithjavalins.QRCode;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ihuntwithjavalins.Comment.Comment;
import com.example.ihuntwithjavalins.Comment.CommentListForCommentAdapter;
import com.example.ihuntwithjavalins.MonsterID;
import com.example.ihuntwithjavalins.QuickNavActivity;
import com.example.ihuntwithjavalins.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * QRCodeViewActivity is an Activity class that displays details of a QRCode and provides options to delete,
 * view image, go back to the library, and navigate to the home screen.
 */
public class QRCodeViewOtherActivity extends AppCompatActivity {
    private Button backButton;
    private ImageButton imageButton;
    private Button quickNavButton;
    private Button deleteButton;
    private Button commentButton;
    private TextView codeName;
    private TextView codeHash;
    private TextView codePoints;
    private TextView codeDateCaught;
    private QRCode thisCode;
    private String TAG = "Sample"; // used as starter string for debug-log messaging

    private ArrayList<Comment> commentsForThisCode = new ArrayList<>();

    private ListView commentList; // xml's object for holding the UI-datalist (within content.xml)

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.code_view_individ_owned);

        backButton = findViewById(R.id.btn_cvi_back);
        quickNavButton = findViewById(R.id.btn_cvi_quicknav);
        imageButton = findViewById(R.id.btn_cvi_image);
        deleteButton = findViewById(R.id.btn_remove_code);
        commentButton = findViewById(R.id.comment_btn);

        codeName = findViewById(R.id.player_name);
        codeHash = findViewById(R.id.player_hash);
        codePoints = findViewById(R.id.player_points);
        codeDateCaught = findViewById(R.id.cvi_code_date_caught);

        // Get the intent from the previous activity
        Intent myIntent = getIntent();
        thisCode = (QRCode) myIntent.getSerializableExtra("focusedCode");

        // new for this variant (different from QRCodeViewActivity)
        deleteButton.setVisibility(View.INVISIBLE);
        commentButton.setVisibility(View.INVISIBLE);
        quickNavButton.setVisibility(View.INVISIBLE);


        codeName.setText(thisCode.getCodeName());
        codeHash.setText(thisCode.getCodeHash());
        codePoints.setText(thisCode.getCodePoints());

        String date_caught = getNiceDateFormat(thisCode.getCodeDate());
        codeDateCaught.setText(date_caught);


        /** Portion reponsble for generating an image of the monster from the hashcode */
        MonsterID monsterID = new MonsterID();
        // Get the AssetManager object
        AssetManager assetManager = getAssets();
        monsterID.generateAndSetImage(imageButton, thisCode.getCodeHash());

        // grabbed any store username variables within app local date storage
        SharedPreferences mPrefs = getSharedPreferences("Login", 0);
        String mStringU = mPrefs.getString("UsernameTag", "default_username_not_found");
        // Access a Firestore instance
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final CollectionReference collectionRef_Users = db.collection("Users");
        final DocumentReference docRef_thisPlayer = collectionRef_Users.document(mStringU);
        final CollectionReference subColRef_myCodes = docRef_thisPlayer.collection("QRCodesSubCollection");
        CollectionReference subColRef_myComments = docRef_thisPlayer.collection("CommentsSubCollection");
        final DocumentReference docRef_hashcode = subColRef_myCodes.document(thisCode.getCodeHash());

        // grab all comments, from whole database, for this code hash
        collectionRef_Users
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                DocumentReference docRef_thisPlayer = collectionRef_Users.document(doc.getId());
                                CollectionReference subColRef_Comments = docRef_thisPlayer.collection("CommentsSubCollection");
                                subColRef_Comments
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot doc : task.getResult()) {
                                                        String unixMillis_DateTime = (String) doc.getId();
                                                        String hashcode = (String) doc.getData().get("Hashcode");
                                                        String username = (String) doc.getData().get("Username");
                                                        String codeComment = (String) doc.getData().get("Comment");
//                                                        String unixMillis_DateTime = (String) doc.getData().get("UnixMillis_DateTime");
                                                        if (hashcode.equals(thisCode.getCodeHash())) {
                                                            commentsForThisCode.add(new Comment(hashcode, username, unixMillis_DateTime, codeComment));
                                                        }
                                                    }
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        // Setup/link list to new adapter for linking data and UI
        commentList = findViewById(R.id.cmt_listview); // grab UI-datalist var
        ArrayAdapter<Comment> customCommentAdapter = new CommentListForCommentAdapter(this, commentsForThisCode); // create adapter (custom child class of Adapter) to link/use on backend-datalist
        commentList.setAdapter(customCommentAdapter);// Set the adapter for backend-datalist to be used with UI-datalist
        customCommentAdapter.notifyDataSetChanged(); // Notifying the adapter to render any new data fetched from the cloud
        Collections.sort(commentsForThisCode, new Comparator<Comment>() {
            @Override
            public int compare(Comment o1, Comment o2) {
                return o1.getUnixMillis_DateTime().compareTo(o2.getUnixMillis_DateTime());
            }
        });
        customCommentAdapter.notifyDataSetChanged();

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(QRCodeViewOtherActivity.this);
                builder.setTitle("Confirmation")
                        .setMessage("Are you sure you want to delete this item?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                docRef_hashcode
                                        .delete() // delete document from firebase database
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // These are a method which gets executed when the task is succeeded
                                                Log.d(TAG, "Data has been deleted successfully!");


                                                // https://firebase.google.com/docs/storage/android/delete-files
                                                // Get a non-default Storage bucket (https://console.firebase.google.com/u/1/project/ihuntwithjavalins-22de3/storage/ihuntwithjavalins-22de3.appspot.com/files/~2F)
                                                FirebaseStorage storage = FirebaseStorage.getInstance("gs://ihuntwithjavalins-22de3.appspot.com/");
                                                // Create a storage reference from our app
                                                StorageReference storageRef = storage.getReference();
                                                // Create a reference with an initial file path and name // use QRcode-object's imgRef string to ref storage
                                                String codePicRef = "UserPhotos/" + thisCode.getCodePhotoRef();
                                                StorageReference pathReference_pic = storageRef.child(codePicRef);
                                                // Delete the file
                                                pathReference_pic.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // File deleted successfully
                                                        Log.d(TAG, "Photo has been deleted successfully!");
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception exception) {
                                                        // Uh-oh, an error occurred!
                                                        Log.d(TAG, "Photo could not be deleted!" + exception.toString());
                                                    }
                                                });


                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // These are a method which gets executed if there’s any problem
                                                Log.d(TAG, "Data could not be deleted!" + e.toString());
                                            }
                                        });
                                BackActionToLibrary();// Finish the activity
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(QRCodeViewOtherActivity.this);
                final EditText edittext = new EditText(QRCodeViewOtherActivity.this);
                builder.setTitle("Comment")
                        .setMessage("Enter comment:")
                        .setView(edittext)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                HashMap<String, String> dataMap = new HashMap<>(); //setup temp key-value mapping (to throw list items at firestore)
                                dataMap.put("Hashcode", thisCode.getCodeHash());
                                dataMap.put("Username", mStringU);
                                dataMap.put("Comment", edittext.getText().toString());
                                String unixMillis_DateTime = String.valueOf(System.currentTimeMillis());
                                commentsForThisCode.add(new Comment(thisCode.getCodeHash(), mStringU, unixMillis_DateTime, edittext.getText().toString()));
                                subColRef_myComments
                                        .document(unixMillis_DateTime)// point to at document (hashcode) then...
                                        .set(dataMap) // add key-value-pairs (to fields of document)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() { // log the success on your console (this helps you verify that the firestore sending-action worked)
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // These are a method which gets executed when the task is succeeded
                                                Log.d(TAG, "Comment has been added successfully!");
//                                                customCommentAdapter.notifyDataSetChanged();
                                                Collections.sort(commentsForThisCode, new Comparator<Comment>() {
                                                    @Override
                                                    public int compare(Comment o1, Comment o2) {
                                                        return o1.getUnixMillis_DateTime().compareTo(o2.getUnixMillis_DateTime());
                                                    }
                                                });
                                                customCommentAdapter.notifyDataSetChanged();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() { // log the failure on your console (if sending-action failed)
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // These are a method which gets executed if there’s any problem
                                                Log.d(TAG, "Comment could not be added!" + e.toString());
                                            }
                                        });
                                customCommentAdapter.notifyDataSetChanged();
                            }

                        })
                        .setNegativeButton("Cancel", null)
                        .show();

            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QRCodeViewOtherActivity.this, QRCodeImageViewOtherActivity.class);
                intent.putExtra("savedItemObjectForImage", (Serializable) thisCode);
                startActivity(intent);

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackActionToLibrary();
            }
        });

        quickNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QRCodeViewOtherActivity.this, QuickNavActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        customCommentAdapter.notifyDataSetChanged(); // Notifying the adapter to render any new data fetched from the cloud

        //delay timer to force update (in addition to click  )
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                customCommentAdapter.notifyDataSetChanged(); // Notifying the adapter to render any new data fetched from the cloud
                Collections.sort(commentsForThisCode, new Comparator<Comment>() {
                    @Override
                    public int compare(Comment o1, Comment o2) {
                        return o1.getUnixMillis_DateTime().compareTo(o2.getUnixMillis_DateTime());
                    }
                });
                customCommentAdapter.notifyDataSetChanged();
            }
        }, 1000);

    }

    @Override
    public void onBackPressed() {
        BackActionToLibrary();
    }

    void BackActionToLibrary() {
        finish();
    }

    String getNiceDateFormat(String joinedDate) {
        String date = joinedDate;
        String date_joined = "";
        if (date != null) {
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
            String year = date.substring(0, 4);
            String month = date.substring(4, 6);
            String day = date.substring(6, 8);
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
        } else {
            date_joined = "No date";
        }
        return date_joined;
    }

}
