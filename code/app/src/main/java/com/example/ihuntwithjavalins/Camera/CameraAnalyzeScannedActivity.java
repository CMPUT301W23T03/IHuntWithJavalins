package com.example.ihuntwithjavalins.Camera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


/**
 * This activity analyzes a scanned camera and determines whether it has already been caught by the user or if it's a new camera to be added to the user's collection. It accesses a
 * Cloud Firestore database to retrieve user-specific data and uses Intent objects to switch between activities accordingly.
 * TODO: implement this with the app (right now the camera cannot distinguish new codes from old QRcodes, and this is intended to be the activity that decides new or old.)
 */
public class CameraAnalyzeScannedActivity extends AppCompatActivity {

    /** Flag to indicate if the QR code has already been caught by the user. */
    private int alreadycaught_flag = 0;
    /** The hash value of the QR code that was scanned and caught by the user. */
    String savedHash;
    /** The name of the QR code that was scanned and caught by the user. */
    String savedName;
    /** The points awarded for catching the QR code that was scanned and caught by the user. */
    String savedPoints;
    /** The reference to the image associated with the QR code that was scanned and caught by the user. */
    String savedImgRef;

    /**
     * Called when the activity is starting. This is where most initialization should go:
     * calling setContentView(int) to inflate the activity's UI, using findViewById(int) to programmatically interact with widgets in the UI, and using savedInstanceState to restore the activity's previously saved state.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Access a Cloud Firestore instance from your Activity
        // my database name is ...  on the ... location
        FirebaseFirestore db; // firestore database object (need to import library dependency)
        db = FirebaseFirestore.getInstance(); // pull instance of database from firestore

        // grabbed any store username variables within app local date storage
        SharedPreferences mPrefs = getSharedPreferences("Login", 0);
        String mStringU = mPrefs.getString("UsernameTag", "default_username_not_found");
        //        String mStringU = "YaBroNahSon"; // used with setting up other usernames quickly

        // setup library based on presence of Username tag (either preferences or global var (global var needs to have an open static var that doesnt die))
        final CollectionReference collectionRef_Username = db.collection(mStringU); // pull instance of specific collection in firestore

        Bundle extras = getIntent().getExtras();
        savedHash = extras.getString("cameraHash");//The key argument here must match that used in the other activity cameraName
        savedName = extras.getString("cameraName");//The key argument here must match that used in the other activity cameraName
        savedPoints = extras.getString("cameraPoints");//The key argument here must match that used in the other activity cameraName
        savedImgRef = extras.getString("cameraGenImgRef");//The key argument here must match that used in the other activity cameraName

//        ArrayList<QRCode> codeList = new ArrayList<>();// list of objects

        // This listener will pull the firestore data into your android app (if you reopen the app)
        collectionRef_Username.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable
            FirebaseFirestoreException error) {
//                codeList.clear(); // Clear the old list
                String myCamTag = "myCamTag"; // used as starter string for debug-log messaging
                Log.d(myCamTag, savedHash);
                alreadycaught_flag = 0;
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) { // Re-add firestore collection sub-documents and sub-sub-collection items)
                    String codeHash = doc.getId();
                    if (codeHash.equals("RegionDocument")) {
                        continue;
                    }
                    Log.d(myCamTag, codeHash);
                        if (savedHash.equals(codeHash)) {
                            alreadycaught_flag++;
                            Log.d(myCamTag, "already caught");
                            break;
                        }
                }
                if (alreadycaught_flag > 0) {
                    Intent intent = new Intent(CameraAnalyzeScannedActivity.this, CameraAlreadyCaughtActivity.class);
//                                                intent.putExtra("cameraSavedCodeText",barcodeText.getText().toString());
                    intent.putExtra("cameraSavedCodeHash", savedHash);
                    intent.putExtra("cameraSavedCodeName", savedName);
                    intent.putExtra("cameraSavedCodePoints", savedPoints);
                    intent.putExtra("cameraSavedCodeImageRef", savedImgRef);
//                    cameraFlag = 1;
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(CameraAnalyzeScannedActivity.this, CameraCaughtNewActivity.class);
//                                                intent.putExtra("cameraSavedCodeText",barcodeText.getText().toString());
                    intent.putExtra("cameraSavedCodeHash", savedHash);
                    intent.putExtra("cameraSavedCodeName", savedName);
                    intent.putExtra("cameraSavedCodePoints", savedPoints);
                    intent.putExtra("cameraSavedCodeImageRef", savedImgRef);
//                    cameraFlag = 1;
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

            }
        });

    }
}
