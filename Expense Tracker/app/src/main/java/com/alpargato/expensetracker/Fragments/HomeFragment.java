package com.alpargato.expensetracker.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.alpargato.expensetracker.CustomAdapters.Expense;
import com.alpargato.expensetracker.CustomAdapters.ExpensesAdapter;
import com.alpargato.expensetracker.LoginGoogle;
import com.alpargato.expensetracker.MainActivity;
import com.alpargato.expensetracker.R;
import com.alpargato.expensetracker.SavingPlan;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private ImageView imgUser;

    private TextView txtUser;
    private FirebaseAuth auth;

    private ListView listado;

    private ProgressBar progressBar;

    private TextView txtNoList;

    private TextView txtExpensePlan;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Infla el diseño y obtiene la vista raíz
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Ahora obtenemos las vistas correctamente
        imgUser = view.findViewById(R.id.imagePhotoUser);
        txtUser = view.findViewById(R.id.txtUserName);
        listado = view.findViewById(R.id.ListRecentExpenses);
        progressBar = view.findViewById(R.id.progressBar);
        txtNoList = view.findViewById(R.id.txtNoExistRecent);
        txtExpensePlan = view.findViewById(R.id.txtViewMoreExpensePlan);


        /*editExpense = view.findViewById(R.id.editTextExpenses);
        editExpenseType = view.findViewById(R.id.editTextExpenseType);
        btnAddDatabase = view.findViewById(R.id.btnAddDatabase);*/

        // Inicializa Firebase
        FirebaseApp.initializeApp(requireContext());
        auth = FirebaseAuth.getInstance();

        // Verifica que el usuario no sea null antes de acceder a sus datos
        if (auth.getCurrentUser() != null) {
            Glide.with(this).load(auth.getCurrentUser().getPhotoUrl()).transform(new RoundedCorners(20)).into(imgUser);
            txtUser.setText(auth.getCurrentUser().getDisplayName());
        } else {
            txtUser.setText("Usuario no autenticado");
        }

        /*btnAddDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editExpense.getText() != null && editExpenseType.getText() != null) {
                    addExpenseToDatabase(editExpense.getText().toString(),editExpenseType.getText().toString());
                }else {
                    Toast.makeText(view.getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });*/

        txtExpensePlan.setOnClickListener(view1 -> {
            goToSavingPlan();
        });

        readRecentsExpensesFromDatabase();


        return view;
    }

    private void readRecentsExpensesFromDatabase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String UID = user.getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("expenses").child(UID).child("userExpenses");
        List<Expense> listaGastos = new ArrayList<>();
        dbRef.orderByKey().limitToLast(5).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String amountStr = snapshot.child("amount").getValue(String.class);
                    String category = snapshot.child("category").getValue(String.class);

                    double amount = 0.0;

                    amount = Double.parseDouble(amountStr);

                    listaGastos.add(new Expense(amount,R.drawable.google_logo_small,"2025-02-05",category));
                }

                Collections.reverse(listaGastos);

                if (listaGastos != null) {
                    progressBar.setVisibility(View.GONE);
                    listado.setVisibility(View.VISIBLE);

                    ExpensesAdapter miAdaptador = new ExpensesAdapter(getContext(),listaGastos.toArray(new Expense[0]));
                    listado.setAdapter(miAdaptador);
                }else {
                    progressBar.setVisibility(View.GONE);
                    txtNoList.setVisibility(View.VISIBLE);
                }


//                listado.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                    }
//                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                listado.setVisibility(View.VISIBLE);
                Log.e("Firebase", "Error al leer datos: " + error.getMessage());
            }
        });
    }

    private void addExpenseToDatabase(String expense, String expenseType) {
        HashMap<String, Object> userData = new HashMap<>();
        FirebaseUser user = auth.getCurrentUser();
        String UID = user.getUid();
        DatabaseReference expenseRef = FirebaseDatabase.getInstance().getReference("expenses").child(UID).child("userExpenses");
        //DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("expenses");

        //userData.put("userName",auth.getCurrentUser().getDisplayName());

        HashMap<String, Object> Addexpenses = new HashMap<>();

        Addexpenses.put("amount",expense);
        Addexpenses.put("category",expenseType);

        //databaseRef.setValue(userData);
        expenseRef.push().setValue(Addexpenses);

        Toast.makeText(getView().getContext(), "Added Succsesfully", Toast.LENGTH_SHORT).show();

    }



    private void goToSavingPlan() {
        Intent intent = new Intent(HomeFragment.this.getContext(), SavingPlan.class);
        startActivity(intent);
    }
}