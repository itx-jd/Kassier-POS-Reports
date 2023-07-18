package com.coderium.pos.preferences;

import static com.coderium.pos.Constant.downloadFileFromURL;
import static com.coderium.pos.Constant.vibrator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.coderium.pos.BuildConfig;
import com.coderium.pos.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {


    public static double service_charges = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().hide();

        // Set status bar background to white
        setStatusBarColor();

        initSwitch();

    }

    public void rl_service_charges(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modify Service Charges");

        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_moify_service_charges, null);

        builder.setView(dialogView);

        TextInputEditText chargesInputField = dialogView.findViewById(R.id.charges_edit_text);

        chargesInputField.setText(String.valueOf(service_charges));

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // Get input field value and process when OK button clicked
                        String enteredCharges = chargesInputField.getText().toString();
                        service_charges = Double.parseDouble(enteredCharges);

                    }
                })
                .setNegativeButton("Cancel", null);  // Dismisses the dialog when Cancel button clicked

        AlertDialog alertDialog  = builder.create();
        alertDialog.show();

    }

    public void rl_business_info(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modify Business Info");

        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_modify_business, null);

        builder.setView(dialogView);

        TextInputEditText name_edit_text = dialogView.findViewById(R.id.name_edit_text);
        TextInputEditText phone_edit_text = dialogView.findViewById(R.id.phone_edit_text);
        TextInputEditText address_edit_text = dialogView.findViewById(R.id.address_edit_text);

        // Fetch the business info from Firebase
        DatabaseReference businessRef = FirebaseDatabase.getInstance().getReference("business_info");

        businessRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                BusinessInfo businessInfo = snapshot.getValue(BusinessInfo.class);

                if (businessInfo != null) {
                    name_edit_text.setText(businessInfo.getName());
                    phone_edit_text.setText(businessInfo.getContact());
                    address_edit_text.setText(businessInfo.getAddress());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        BusinessInfo businessInfo = new BusinessInfo();
                        businessInfo.setName(name_edit_text.getText().toString());
                        businessInfo.setContact(phone_edit_text.getText().toString());
                        businessInfo.setAddress(address_edit_text.getText().toString());

                        // Update the business info in Firebase
                        businessRef.setValue(businessInfo);

                    }
                })
                .setNegativeButton("Cancel", null);  // Dismisses the dialog when Cancel button clicked

        AlertDialog alertDialog  = builder.create();
        alertDialog.show();

    }

    public void rl_contact_developer(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_contact_dev, null);

        TextView tv_number = dialogView.findViewById(R.id.tv_number);
        TextView tv_mail = dialogView.findViewById(R.id.tv_mail);

        tv_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = tv_number.getText().toString();
                copyToClipboard(phoneNumber);
                Toast.makeText(getApplicationContext(), "Phone Number Copied", Toast.LENGTH_SHORT).show();
            }
        });

        tv_mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailAddress = tv_mail.getText().toString();
                copyToClipboard(emailAddress);
                Toast.makeText(getApplicationContext(), "Email Address Copied", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }

    public void rl_report_bug(View view) {

        Intent intent = new Intent(Intent.ACTION_SENDTO);

        intent.setData(Uri.parse("mailto:")); // only email apps should handle this

        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.email_address)});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));

        try {
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }catch (Exception e){
            Toast.makeText(this, "Failed To Open Email !", Toast.LENGTH_SHORT).show();
        }
    }

    public void rl_check_update(View view) {

        // Get the current app version name
        String currentVersionName = BuildConfig.VERSION_NAME;

        // Get the latest app version name and direct link from Firebase Realtime Database
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("app_info");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String latestVersionName = snapshot.child("version_name").getValue(String.class);
                String directLink = snapshot.child("direct_link").getValue(String.class);

                // Check if the current version is lower than the latest version
                if (currentVersionName.compareTo(latestVersionName) < 0) {
                    // Show the update dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);

                    // Inflate the dialog layout
                    View dialogView = LayoutInflater.from(SettingsActivity.this).inflate(R.layout.dialog_update, null);
                    builder.setView(dialogView);

                    AlertDialog alertDialog  = builder.create();

                    Button btn_update =  dialogView.findViewById(R.id.btn_update);
                    Button btn_no =  dialogView.findViewById(R.id.btn_no);

                    btn_update.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            vibrator(SettingsActivity.this);
                            downloadFileFromURL(SettingsActivity.this,directLink,"Kassier",latestVersionName);
                        }
                    });

                    btn_no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            vibrator(SettingsActivity.this);
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.show();
                }else{
                    Toast.makeText(SettingsActivity.this, "You have Latest Version ! \uD83D\uDC4D", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Do nothing
            }
        });
    }

    private void initSwitch() {
        // Get the switch from the layout
        Switch switchReceipt = findViewById(R.id.switch_receipt);

        // Get the old state of the switch from preference storage
        SharedPreferences preferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        boolean isReceiptSwitchOn = preferences.getBoolean("is_receipt_switch_on", false);

        // Set the switch to the old state
        switchReceipt.setChecked(isReceiptSwitchOn);

        // Register a listener to listen for changes to the switch
        switchReceipt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                vibrator(SettingsActivity.this);

                // Save the new state of the switch to preference storage
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("is_receipt_switch_on", isChecked);
                editor.apply();
            }
        });
    }

    private void setStatusBarColor() {

        int currentapiVersion = Build.VERSION.SDK_INT;

        // Check if the device supports setting the status bar text color
        if (currentapiVersion >= Build.VERSION_CODES.R) {
            // Set status bar text color to black
            WindowInsetsControllerCompat insetsController =
                    new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
            ((WindowInsetsControllerCompat) insetsController).setAppearanceLightStatusBars(true);
        } else {
            // Set status bar text color to black
            View decorView = getWindow().getDecorView();
            int flags = decorView.getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(flags);
        }

        // Set status bar background color to white
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.WHITE);
    }


}