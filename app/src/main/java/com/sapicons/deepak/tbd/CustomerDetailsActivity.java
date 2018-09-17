package com.sapicons.deepak.tbd;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sapicons.deepak.tbd.Fragments.CustomerFragment;
import com.sapicons.deepak.tbd.Objects.AccountItem;
import com.sapicons.deepak.tbd.Objects.CustomerItem;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

public class CustomerDetailsActivity extends AppCompatActivity {

    EditText firstNameET, lastNameEt, phoneEt,addLine1Et, addLine2Et,townEt,pincodeEt;
    Button saveChangesBtn, deleteCustomerBtn;
    FancyButton contactCustomerBtn;
    CustomerItem selectedCustomer;

    CircleImageView customerPicIv;
    Uri customerPicUri;
    String customerPicUrl;
    StorageReference storageReference;

    TextView mTv,cTv,dTv,fullNameTv;
    int count = 0;
    int dCount =0,cCount=0,mCount=0;

    InputMethodManager inputManager;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_details);
        setTitle("Details");

        Log.d("ACTIVITY","CustomerDetailsActivity");

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();

        selectedCustomer = (CustomerItem)bundle.getSerializable("selected_customer");

        initialiseUI();
        getNoOfAccounts();

    }
    private void initialiseUI(){

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait ...");
        progressDialog.show();

        firstNameET = findViewById(R.id.display_customer_first_name);
        lastNameEt = findViewById(R.id.display_customer_last_name);
        phoneEt = findViewById(R.id.display_customer_phone);
        addLine1Et = findViewById(R.id.display_customer_add_line1);
        addLine2Et = findViewById(R.id.display_customer_add_line2);
        townEt = findViewById(R.id.display_customer_town_city);
        pincodeEt = findViewById(R.id.display_customer_pincode);
        customerPicIv = findViewById(R.id.display_customer_pic);

        mTv= findViewById(R.id.acd_m_acc_tv);
        cTv = findViewById(R.id.acd_c_acc_tv);
        dTv= findViewById(R.id.acd_d_acc_tv);
        fullNameTv = findViewById(R.id.acd_display_full_name);

        saveChangesBtn  = findViewById(R.id.save_changes_btn);
        deleteCustomerBtn = findViewById(R.id.remove_customer_btn);
        contactCustomerBtn = findViewById(R.id.acd_contact_customer_btn);


        //set the edit text fields
        firstNameET.setText(selectedCustomer.getFirstName());
        lastNameEt.setText(selectedCustomer.getLastName());
        phoneEt.setText(selectedCustomer.getPhone());
        addLine1Et.setText(selectedCustomer.getAddressLine1());
        addLine2Et.setText(selectedCustomer.getAddressLine2());
        townEt.setText(selectedCustomer.getTownCity());
        pincodeEt.setText(selectedCustomer.getPincode());
        fullNameTv.setText(selectedCustomer.getFirstName()+" "+selectedCustomer.getLastName());

        //add text watcher
        firstNameET.addTextChangedListener(watcher);
        lastNameEt.addTextChangedListener(watcher);
        phoneEt.addTextChangedListener(watcher);
        addLine2Et.addTextChangedListener(watcher);
        addLine1Et.addTextChangedListener(watcher);
        townEt.addTextChangedListener(watcher);

        // set customer's display pic
        if(selectedCustomer.getPhotoUrl().length()>0)
            Glide.with(this).load(selectedCustomer.getPhotoUrl()).into(customerPicIv);

        setFocusOfET(false);


        //display no of accounts this customer has
        //noOfAccTv.setText("No. Of Accounts: "+getNoOfAccounts());

        setOnClickListeners();

        //hideKeyboard(this);



    }

    private final TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(firstNameET.getText().toString().length() == 0 ||
                    lastNameEt.getText().toString().length() == 0 ||
                    phoneEt.getText().toString().length() == 0 ||
                    addLine1Et.getText().toString().length() == 0 ||
                    addLine2Et.getText().toString().length() == 0 ||
                    townEt.getText().toString().length() == 0 )

                saveChangesBtn.setVisibility(View.GONE);
            else
                saveChangesBtn.setVisibility(View.VISIBLE);
        }
    };

    private void setOnClickListeners(){
        // add onClick to buttons
        saveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // user switches to edit mode , hide the contact button
                if(saveChangesBtn.getText().toString().toLowerCase().equals("edit")){
                    setFocusOfET(true);
                    contactCustomerBtn.setVisibility(View.GONE);
                }
                // user is in display mode, show the contact button
                else {
                    AlertDialog.Builder builder;

                    builder = new AlertDialog.Builder(CustomerDetailsActivity.this);

                    builder.setTitle("Save Changes?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //update customer

                                    Log.d("URI","Customer URI: "+customerPicUri);
                                    if(customerPicUri!=null)
                                        uploadToDBwithPic();
                                    else
                                        updateCustomer("");
                                    progressDialog.show();
                                    contactCustomerBtn.setVisibility(View.VISIBLE);

                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();
                }
            }
        });


        deleteCustomerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(CustomerDetailsActivity.this);



                if(count>0){
                    builder.setTitle("Cannot Delete Customer!")
                            .setMessage("This customer cannot be deleted as he/she has some open accounts. Consider closing the accounts first.")
                    .show();
                }
                else {
                    builder.setTitle("Remove Customer?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {


                                    //remove customer
                                    deleteCustomer(false);
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();
                }
            }
        });


        customerPicIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(saveChangesBtn.getText().toString().toLowerCase().equals("save")) {
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(CustomerDetailsActivity.this);
                }
            }
        });

        contactCustomerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openIntentChooser();

            }
        });



    }

    private void updateCustomer(String picUrl){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // create firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String firstName = firstNameET.getText().toString();
        String lastName = lastNameEt.getText().toString();
        String phone = phoneEt.getText().toString();
        String addLine1 = addLine1Et.getText().toString();
        String addLine2 = addLine2Et.getText().toString();
        String townCity = townEt.getText().toString();
        String pincode = pincodeEt.getText().toString();
        if(picUrl.length() == 0)
            picUrl = selectedCustomer.getPhotoUrl();

        //check if the new phone number is different from old phone number
        //if(!phone.equals(selectedCustomer.getPhone()))
          //  deleteCustomer(true);

        //create an auto-generated id
        final DocumentReference newCustomerRef = db.collection("users").document(user.getEmail())
                .collection("customers").document(selectedCustomer.getCustomerId());
        //create a customer item
        CustomerItem item =  new CustomerItem(selectedCustomer.getCustomerId(),firstName,lastName,phone,
                addLine1,addLine2,townCity,pincode,picUrl);

        //set the new customer item to firestore db
        newCustomerRef.set(item)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG","Data successfully written. ID: "+newCustomerRef.getId());


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "Error writing document. "+e);
            }
        });

        Toasty.success(this, "Customer successfully updated!").show();

        //update the name of the customer in their accounts
        //updateCustomersNameInAccounts(firstName,lastName,phone);

        if (!firstName.equals(selectedCustomer.getFirstName()) ||
                !lastName.equals(selectedCustomer.getLastName()) ||
                !phone.equals(selectedCustomer.getPhone())){
            updateCustomersExistingAccounts(firstName,lastName,phone);
        }


        progressDialog.dismiss();
        finish();

    }

    private void uploadToDBwithPic(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        storageReference= FirebaseStorage.getInstance().getReference().child("users")
                .child(user.getEmail()).child("customers_pic");
        if(customerPicUri!=null) {
            storageReference = storageReference.child(customerPicUri.getLastPathSegment());
            UploadTask task = storageReference.putFile(customerPicUri);

            Task<Uri> urlTask = task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Log.d("PIC_URL","url: "+downloadUri);
                        updateCustomer(downloadUri.toString());
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
        }


    }


    private void deleteCustomer(boolean wait){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // create firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        final DocumentReference newCustomerRef = db.collection("users").document(user.getEmail())
                .collection("customers").document(selectedCustomer.getPhone());
        newCustomerRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toasty.info(CustomerDetailsActivity.this, "Customer removed!").show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d("TAG","Deletion failed! "+e);
            }
        });

        if(!wait) {
            finish();
        }
    }

    public void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void getNoOfAccounts(){


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // create firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //create ref to accounts
        CollectionReference ref = db.collection("users").document(user.getEmail())
                .collection("accounts");


        // 8-09-2018
        //create a query to search for user's accounts
        /*Query getNoOfAcc = ref.whereEqualTo("firstName",selectedCustomer.getFirstName())
                .whereEqualTo("lastName",selectedCustomer.getLastName())
                .whereEqualTo("phoneNumber",selectedCustomer.getPhone());*/

        Query getNoOfAcc = ref.whereEqualTo("customerId",selectedCustomer.getCustomerId());

        getNoOfAcc.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {


                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                AccountItem item = document.toObject(AccountItem.class);
                                if(item.getAccoutType().contains("D"))
                                    dCount++;
                                else if(item.getAccoutType().contains("M"))
                                    mCount++;
                                else if(item.getAccoutType().contains("C"))
                                    cCount++;
                                count++;
                                Log.d("TAG","Count: "+count);
                            }
                        }else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }



                        progressDialog.dismiss();
                        setNoOfAccounts(count);
                    }
                });

    }

    public void setNoOfAccounts(int count){
        cTv.setText(cCount+"");
        mTv.setText(mCount+"");
        dTv.setText(dCount+"");
    }

    public void setFocusOfET(boolean set){
        if(set){
            //firstNameET.requestFocus();
            firstNameET.setFocusableInTouchMode(true);
            pincodeEt.setFocusableInTouchMode(true);
            firstNameET.setFocusableInTouchMode(true);
            lastNameEt.setFocusableInTouchMode(true);
            phoneEt.setFocusableInTouchMode(true);
            addLine2Et.setFocusableInTouchMode(true);
            addLine1Et.setFocusableInTouchMode(true);
            townEt.setFocusableInTouchMode(true);

            Log.d("TAG","SAVE");

            saveChangesBtn.setText("Save");

        }
        else{

            pincodeEt.setFocusable(false);
            firstNameET.setFocusable(false);
            lastNameEt.setFocusable(false);
            phoneEt.setFocusable(false);
            addLine2Et.setFocusable(false);
            addLine1Et.setFocusable(false);
            townEt.setFocusable(false);


            Log.d("TAG","EDIT");

            saveChangesBtn.setText("Edit");
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                customerPicUri = result.getUri();
                Glide.with(this).load(customerPicUri).into(customerPicIv);
                Log.d("URI","Result URI: "+customerPicUri.toString());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void updateCustomersExistingAccounts(final String fName, final String lName, final String phone){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // create firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final CollectionReference accRef = db.collection("users").document(user.getEmail())
                .collection("accounts");

        Query getAcc = accRef.whereEqualTo("customerId",selectedCustomer.getCustomerId());

        getAcc.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("TAG", document.getId() + " => " + document.getData());
                        AccountItem item = document.toObject(AccountItem.class);

                        item.setFirstName(fName);
                        item.setLastName(lName);
                        item.setPhoneNumber(phone);

                        updateAccounts(item);

                    }
                    Toasty.success(CustomerDetailsActivity.this,"Customer Updated!").show();
                }else {
                    Log.d("TAG", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void updateAccounts(AccountItem accountItem){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final  DocumentReference accRef = db.collection("users").document(firebaseUser.getEmail())
                .collection("accounts").document(accountItem.getAccountNumber());

        accRef.update("firstName",accountItem.getFirstName(),
                "lastName",accountItem.getLastName(),
                "phoneNumber",accountItem.getPhoneNumber())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Toasty.success(CustomerDetailsActivity.this,"Customer Updated!").show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG","Error updating documents: "+e);
            }
        });


    }

    public void openIntentChooser(){
        AlertDialog.Builder builder = new AlertDialog.Builder(CustomerDetailsActivity.this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView=inflater.inflate(R.layout.custom_contact_app_list,null);

        final CircleImageView choosePhone = customView.findViewById(R.id.ccal_open_phone),
                chooseMessage = customView.findViewById(R.id.ccal_open_message),
                chooseWhatsapp = customView.findViewById(R.id.ccal_open_whatsapp);
        builder.setMessage("Choose");
        builder.setView(customView);

        builder.create();
        builder.show();

        choosePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialer(selectedCustomer.getPhone());
            }
        });

        chooseMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMessages(selectedCustomer.getPhone(),"Enter message: ");
            }
        });

        chooseWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWhatsapp(selectedCustomer.getPhone(),"Enter Message: ");
            }
        });

    }

    public void openDialer(String phoneNumber){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:"+phoneNumber));
        startActivity(intent);
    }

    public void openWhatsapp(String phoneNumber,String message){
        Intent sendIntent = new Intent("android.intent.action.MAIN");
        sendIntent.setComponent(new ComponentName("com.whatsapp","com.whatsapp.Conversation"));
        sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(phoneNumber)+"@s.whatsapp.net");
        startActivity(sendIntent);
    }

    public void openMessages(String phoneNumber,String message){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phoneNumber));
        intent.putExtra("sms_body", message);
        startActivity(intent);
    }
}
