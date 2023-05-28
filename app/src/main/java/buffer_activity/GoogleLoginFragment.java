package buffer_activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.zyterfitlayout.MainActivity;
import com.example.zyterfitlayout.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;


public class GoogleLoginFragment extends Fragment {

    private GoogleSignInClient mGoogleSignInClient;
    private String personName, personGivenName, personFamilyName, personEmail, personID;
    private Uri personPhoto;
    private TextView TprofileName, TprofileEmail;
    private ImageView IprofileImage;
    private Button Bcontinue;
    private BufferActivity bufferActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_google_login, container, false);

        TprofileName = v.findViewById(R.id.G_profile_name);
        TprofileEmail = v.findViewById(R.id.G_profile_email);
        IprofileImage = v.findViewById(R.id.G_profile_pic);
        Bcontinue = v.findViewById(R.id.Gcontinue);
        bufferActivity = new BufferActivity();

        String serverClientId = "138992375306-as2mhs74vo2lkb38klnlr9b3bpn6lfd4.apps.googleusercontent.com";
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestId()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        signIn();

//        googlesignin();



        return v;
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 12);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 12) {
            Log.i("hskahdhalksda", "jahskgdbjakghkjshdkajhlkgsjdbakjsld");
//            googlesignin();
            setAccount();
        } else {
            signIn();
        }
    }
    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(getContext()) != null;
    }

    private void setAccount(){

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (acct != null) {
            Log.i("acct", "" + acct.getPhotoUrl());
            personName = acct.getDisplayName();
            personGivenName = acct.getGivenName();
            personFamilyName = acct.getFamilyName();
            personEmail = acct.getEmail();
            personID = acct.getId();
            personPhoto = acct.getPhotoUrl();

            Log.i("Account details", personName+ personGivenName+ personFamilyName);
//            Log.i("Account picture", );

            TprofileName.setText(personName);
            TprofileEmail.setText(personEmail);
            Bcontinue.setText("Continue as " + personName);
            if (personName!=null) {
                editor.putString("personName", personName);
            }
            if (personGivenName!=null) {
                editor.putString("personGivenName", personGivenName);
            }
            if (personEmail!=null) {
            editor.putString("personEmail", personEmail);
            }
            if (personPhoto!=null) {
                editor.putString("personImage", personPhoto.toString());
                Glide.with(this)
                        .load(personPhoto)
                        .into(IprofileImage);
                Log.i("personImage", ""+ personPhoto);
            }


            editor.commit();
            Bcontinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    if(bufferActivity.checkGooglePermissions(getActivity())==false){
                        GooglePermisionsFragment googlePermisionsFragment = new GooglePermisionsFragment();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.bufferact, googlePermisionsFragment).commit();
                    } else if (mPreferences.getInt("FirstStart",0)==0){
                        WeightHeightFragment weightHeightFragment = new WeightHeightFragment();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.bufferact, weightHeightFragment).commit();
                    } else{
                        startActivity(new Intent(getActivity(), MainActivity.class));
                        getActivity().finish();
                    }
                }
            });

        } else {
            signIn();

        }

    }

}