package com.abhishek.pal.locationsharer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SignInActivity extends AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 007;
    private static int flag=0;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;
    public String PhotoUrl;
    public static File direc;
    private SignInButton btnSignIn;
    private Button btnSignOut, btnRevokeAccess;
    //private Button continu;
    public String save;
    private LinearLayout llProfileLayout;
    private ImageView imgProfilePic;
    private TextView txtName, txtEmail;
    //private ImageView imgProfilePic2;
    private String name;
    private int fl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        fl=0;
        btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
        btnSignOut = (Button) findViewById(R.id.btn_sign_out);
        btnRevokeAccess = (Button) findViewById(R.id.btn_revoke_access);
        llProfileLayout = (LinearLayout) findViewById(R.id.llProfile);
        imgProfilePic = (ImageView) findViewById(R.id.imgProfilePic);
        txtName = (TextView) findViewById(R.id.txtName);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        //imgProfilePic2=(ImageView) findViewById(R.id.roundedImage);
        //continu=(Button)findViewById(R.id.cont);

        btnSignIn.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);
        btnRevokeAccess.setOnClickListener(this);
        //continu.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        Log.e(TAG,"In OnCreate()");
        // Customizing G+ button
        btnSignIn.setSize(SignInButton.SIZE_STANDARD);
        //btnSignIn.setScopes(gso.getScopeArray());
    }

    private void signIn() {
        Log.e(TAG,"In signIn()");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void signOut() {
        Log.e(TAG,"In signOut()");
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                    }
                });
    }

    private void revokeAccess() {
        Log.e(TAG,"In revokeAccess()");
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                    }
                });
    }

//    private void conti(){
//        //setVisible(true);
////        if(flag==1)
////        {
////            Intent intent = new Intent(SignInActivity.this, MapsActivity.class);
////            //String message = PhotoUrl;
////            //String message=direc.toString();
////            File message=direc;
////            intent.putExtra("id", message);
////            startActivity(intent);
////        }
//        Log.d(TAG,"In conti()");
//    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            Log.e(TAG, "display name: " + acct.getDisplayName());

            String personPhotoUrl;
            String personName = acct.getDisplayName();
            name=personName;
            String email = acct.getEmail();
            //String gender=acct.g

            try {
                personPhotoUrl = acct.getPhotoUrl().toString();
//                Glide.with(getApplicationContext()).load(personPhotoUrl)
//                        .thumbnail(0.5f)
//                        .crossFade()
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .into(imgProfilePic);
            }
            catch(Exception e){
                Log.d(TAG,"We Catched It.",e);
                imgProfilePic.setImageResource(R.mipmap.ic_launcher);
                //imgProfilePic2.setImageResource(R.mipmap.ic_launcher_round);
                personPhotoUrl="";
            }

            PhotoUrl=personPhotoUrl;
            Log.e(TAG, "Name: " + personName + ", email: " + email + ", Image: " + personPhotoUrl);

            txtName.setText(personName);
            txtEmail.setText(email);
            if(!personPhotoUrl.equals("")) {
                Log.e(TAG,"In if");
                Glide.with(getApplicationContext()).load(personPhotoUrl)
                        .asBitmap()
                        .thumbnail(0.5f)
                        //.into(imgProfilePic);
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new BitmapImageViewTarget(imgProfilePic){
                @Override
                protected void setResource(Bitmap resource) {
                    //String issaved=saveToInternalStorage(resource);
                    String savedir=saveToInternalStorage(resource);
                    save=savedir;
                    Log.d(TAG,"File "+savedir);

                }
                });
                //I want to round up the image hence this
//                Glide.with(getApplicationContext()).load(personPhotoUrl).asBitmap().centerCrop().into(new BitmapImageViewTarget(imgProfilePic2) {
//                    @Override
//                    protected void setResource(Bitmap resource) {
//                        RoundedBitmapDrawable circularBitmapDrawable =
//                                RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
//                        circularBitmapDrawable.setCircular(true);
//                        imgProfilePic2.setImageDrawable(circularBitmapDrawable);
//                        String issaved=saveToSdCard(resource,"myself");
//                        Log.d(TAG,"File "+issaved);
//
//                }
//                });
            }
            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(this,"Connection Cannot be established",Toast.LENGTH_SHORT).show();
            updateUI(false);
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 60, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }


//    public static String saveToSdCard(Bitmap bitmap, String filename) {
//        Log.e(TAG,"In save to SDCARD");
//        String stored = null;
//
//        File sdcard = Environment.getExternalStorageDirectory() ;
//
//        File folder = new File(sdcard.getAbsoluteFile(), "Marker");//the dot makes this directory hidden to the user
//        folder.mkdir();
//        if(direc==null)
//        {
//            direc=folder;
//        }
//        File file = new File(folder.getAbsoluteFile(), filename + ".jpg") ;
//        if (file.exists())
//            return stored ;
//
//        try {
//            FileOutputStream out = new FileOutputStream(file);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
//            out.flush();
//            out.close();
//            stored = "success";
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return stored;
//    }


//    public static void saveToSdCard(Context context, RoundedBitmapDrawable b, String picName){
//        FileOutputStream fos;
//        try {
//            fos = context.openFileOutput(picName, Context.MODE_PRIVATE);
//            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
//            fos.close();
//        }
//        catch (FileNotFoundException e) {
//            Log.d(TAG, "file not found");
//            e.printStackTrace();
//        }
//        catch (IOException e) {
//            Log.d(TAG, "io exception");
//            e.printStackTrace();
//        }
//
//    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {
            case R.id.btn_sign_in:
                signIn();
                break;

            case R.id.btn_sign_out:
                signOut();
                break;

            case R.id.btn_revoke_access:
                revokeAccess();
                break;
            //case R.id.cont:conti();break;
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG,"In onActivityResult()");
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG,"In onStart()");
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

    }

    private void showProgressDialog() {
        Log.e(TAG,"In showProgressDialog()");
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        Log.e(TAG,"In hideProgressDialog()");
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void updateUI(boolean isSignedIn) {
        Log.d(TAG,"In updateUI() with value "+ isSignedIn);
        if(isSignedIn)
        {
            flag=1;
        }
        else{
            flag=0;
        }
        if (isSignedIn) {
            btnSignIn.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.VISIBLE);
            btnRevokeAccess.setVisibility(View.VISIBLE);
            llProfileLayout.setVisibility(View.VISIBLE);
            //imgProfilePic2.setVisibility(View.VISIBLE);
            //continu.setVisibility(View.VISIBLE);
        } else {
            btnSignIn.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
            btnRevokeAccess.setVisibility(View.GONE);
            llProfileLayout.setVisibility(View.GONE);
            //imgProfilePic2.setVisibility(View.GONE);
            //continu.setVisibility(View.GONE);
        }
//        if(PhotoUrl.equals("")&& isSignedIn==true)
//        {
//            onResume();
//        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG,"In onResume()");
        //Intent i=getIntent();
       //getIntent if(Intent.FilterComparison(MapsActivity) == i){

        //}
//&& (direc!=null || personphotourl)
        Log.e(String.valueOf(this), String.valueOf(fl));
        Intent i=getIntent();

            if(flag==1 )
        {
            Intent intent = new Intent(SignInActivity.this, MapsActivity.class);
            //String message = PhotoUrl;
//            //String message=direc.toString();
              //File message=direc;
            //intent.putExtra("id", message);
            intent.putExtra("name",name);
            intent.putExtra("url",PhotoUrl);
            if(!PhotoUrl.equals(""))
            {
                intent.putExtra("dir",save);
            }
            fl++;
            Log.e(String.valueOf(this), String.valueOf(fl));
            startActivity(intent);
        }
        /*EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);*/

    }


    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true);
    }

}


