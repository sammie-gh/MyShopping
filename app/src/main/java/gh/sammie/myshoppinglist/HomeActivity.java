package gh.sammie.myshoppinglist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

import gh.sammie.myshoppinglist.Holder.MyViewHolder;
import gh.sammie.myshoppinglist.Interface.ItemClickListener;
import gh.sammie.myshoppinglist.Model.Data;

import static gh.sammie.myshoppinglist.Contansts.Common.Toasty;

public class HomeActivity extends AppCompatActivity {


    private Toolbar toolbar;
    private FloatingActionButton fab_btn;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    //Globar variable..
    private TextView totalsumResult;
    private String type;
    private int amount;
    private String note;
    private String post_key;
    private String uId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();

        fab_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        uId = mUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Shopping List").child(uId);
        mDatabase.keepSynced(true);


        loadData();

        //Total sum number
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int totalammount = 0;

                for (DataSnapshot snap : dataSnapshot.getChildren()) {

                    Data data = snap.getValue(Data.class);

                    totalammount += data.getAmount();

                    String sttotal = String.valueOf(totalammount + ".00");

                    totalsumResult.setText("GH¢ "+sttotal);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void initViews() {
        fab_btn = findViewById(R.id.fab);
        totalsumResult = findViewById(R.id.total_ammount);
        recyclerView = findViewById(R.id.recycler_home);
        toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Shopping List");
    }


    private void customDialog() {

        AlertDialog.Builder myDialog = new AlertDialog.Builder(HomeActivity.this);

        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        View myView = inflater.inflate(R.layout.input_data, null);

        final AlertDialog dialog = myDialog.create();

        dialog.setView(myView);

        final EditText type = myView.findViewById(R.id.edt_type);
        final EditText amount = myView.findViewById(R.id.edt_ammount);
        final EditText note = myView.findViewById(R.id.edt_note);
        Button btnSave = myView.findViewById(R.id.btn_save);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String mType = type.getText().toString().trim();
                String mAmount = amount.getText().toString().trim();
                String mNote = note.getText().toString().trim();

                int ammint = Integer.parseInt(mAmount);

                if (TextUtils.isEmpty(mType)) {
                    type.setError("Required Field..");
                    return;
                }
                if (TextUtils.isEmpty(mAmount)) {
                    amount.setError("Required Field..");
                    return;
                }
                if (TextUtils.isEmpty(mNote)) {
                    note.setError("Required Field..");
                    return;
                }


                String id = mDatabase.push().getKey();

                String date = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(mType, ammint, mNote, date, id);

                mDatabase.child(id).setValue(data);

                Toasty.Show_Toast(getApplicationContext(),view,"Data Added");

                dialog.dismiss();
            }
        });


        dialog.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.log_out:
                showLogOutDialog();
                break;

            case R.id.refresh:
                loadData();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void loadData() {
        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(FirebaseDatabase.getInstance()
                                .getReference()
                                .child("Shopping List").child(uId)
                        , Data.class)
                .build();
        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_data, parent, false);
                return new MyViewHolder(itemView);
            }

            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder viewHolder, final int position, @NonNull final Data model) {

                viewHolder.setDate(model.getDate());
                viewHolder.setType(model.getType());
                viewHolder.setNote(model.getNote());
                viewHolder.setAmmount(model.getAmount());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
//                        Toast.makeText(HomeActivity.this, ""+position, Toast.LENGTH_SHORT).show();
                        post_key = getRef(position).getKey();
                        type = model.getType();
                        note = model.getNote();
                        amount = model.getAmount();
                        updateData();
                    }
                });

            }

        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void showLogOutDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(getApplicationContext(), android.R.style.Theme_Material_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(getApplicationContext());


        builder = new AlertDialog.Builder(HomeActivity.this)
                .setTitle("You are about to Log out")
                .setMessage("Are you sure on Action ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mAuth.signOut();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });


        builder.show();


    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    public void updateData() {

        AlertDialog.Builder mydialog = new AlertDialog.Builder(HomeActivity.this);

        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);

        View mView = inflater.inflate(R.layout.update_inputfield, null);

        final AlertDialog dialog = mydialog.create();

        dialog.setView(mView);

        final EditText edt_Type = mView.findViewById(R.id.edt_type_upd);
        final EditText edt_Ammoun = mView.findViewById(R.id.edt_ammount_upd);
        final EditText edt_Note = mView.findViewById(R.id.edt_note_upd);

        edt_Type.setText(type);
        edt_Type.setSelection(type.length());

        edt_Ammoun.setText(String.valueOf(amount));
        edt_Ammoun.setSelection(String.valueOf(amount).length());

        edt_Note.setText(note);
        edt_Note.setSelection(note.length());


        Button btnUpdate = mView.findViewById(R.id.btn_SAVE_upd);
        Button btnDelete = mView.findViewById(R.id.btn_delete_upd);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                type = edt_Type.getText().toString().trim();

                String mAmmount = String.valueOf(amount);

                mAmmount = edt_Ammoun.getText().toString().trim();

                note = edt_Note.getText().toString().trim();

                int intammount = Integer.parseInt(mAmmount);

                String date = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(type, intammount, note, date, post_key);

                mDatabase.child(post_key).setValue(data);

                Toasty.Show_Toast(getApplicationContext(),view,"Data edited");
                dialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mDatabase.child(post_key).removeValue();
                Toasty.Show_Toast(getApplicationContext(),view,"Data remove successful");

                dialog.dismiss();

            }
        });

        dialog.show();

    }


}
