package com.alpargato.expensetracker.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.alpargato.expensetracker.CustomAdapters.Expense;
import com.alpargato.expensetracker.CustomAdapters.ExpensesAdapter;
import com.alpargato.expensetracker.ExpenseType.ExpenseType;
import com.alpargato.expensetracker.ExpenseType.ExpenseTypeManager;
import com.alpargato.expensetracker.Home_acces_classes.AddExpense;
import com.alpargato.expensetracker.R;
import com.alpargato.expensetracker.Home_acces_classes.SavingPlan;
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
import java.util.List;

public class HomeFragment extends Fragment {

    private ImageView imgUser;

    private TextView txtUser;
    private FirebaseAuth auth;

    private ListView listado;

    private ProgressBar progressBar;

    private TextView txtNoList;

    private TextView txtExpensePlan;

    private Button btnAddExpense;


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
        editExpenseType = view.findViewById(R.id.editTextExpenseType);*/
        btnAddExpense = view.findViewById(R.id.btnAddExpense);

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

        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (editExpense.getText() != null && editExpenseType.getText() != null) {
                    addExpenseToDatabase(editExpense.getText().toString(),editExpenseType.getText().toString());
                }else {
                    Toast.makeText(view.getContext(), "Error", Toast.LENGTH_SHORT).show();
                }*/
                openDialogAddExpense();
            }
        });

        txtExpensePlan.setOnClickListener(view1 -> {
            goToSavingPlan();
        });

        readRecentsExpensesFromDatabase();


        return view;
    }

    private void readRecentsExpensesFromDatabase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            progressBar.setVisibility(View.GONE);
            txtNoList.setVisibility(View.VISIBLE);
            return;
        }

        String UID = user.getUid();

        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("expenses")
                .child(UID)
                .child("userExpenses");

        List<Expense> listaGastos = new ArrayList<>();

        dbRef.orderByKey().limitToLast(5).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Object expenseValue = snapshot.child("expense").getValue();

                    double expense = 0.0;

                    if (expenseValue instanceof Number) {
                        expense = ((Number) expenseValue).doubleValue();
                    } else if (expenseValue != null) {
                        expense = Double.parseDouble(expenseValue.toString());
                    }

                    String date = snapshot.child("date").getValue(String.class);
                    String expenseType = snapshot.child("expenseType").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String emoji = getEmojiExpenseType(snapshot);

                    if (date == null) {
                        date = "";
                    }

                    if (expenseType == null) {
                        expenseType = "";
                    }

                    if (description == null) {
                        description = "";
                    }

                    listaGastos.add(new Expense(expense, date, expenseType, description,emoji));
                }

                Collections.reverse(listaGastos);

                progressBar.setVisibility(View.GONE);

                if (!listaGastos.isEmpty()) {
                    listado.setVisibility(View.VISIBLE);

                    ExpensesAdapter miAdaptador = new ExpensesAdapter(
                            getContext(),
                            listaGastos.toArray(new Expense[0])
                    );

                    listado.setAdapter(miAdaptador);
                } else {
                    txtNoList.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                txtNoList.setVisibility(View.VISIBLE);

                Log.e("Firebase", "Error al leer datos: " + error.getMessage());
            }
        });
    }

    private String getEmojiExpenseType(DataSnapshot snapshot) {
        ExpenseTypeManager manager = new ExpenseTypeManager(requireContext());
        List<ExpenseType> categories = manager.getCategories();
        String category = snapshot.child("expenseType").getValue(String.class);
        if (category == null) {
            return "";
        }
        String name = category.substring(category.indexOf(" ") + 1).trim();
        for (ExpenseType ex : categories) {
            if (ex.getName().equals(name)) {
                return ex.getEmoji();
            }
        }
        return "";
    }



    private void goToSavingPlan() {
        Intent intent = new Intent(HomeFragment.this.getContext(), SavingPlan.class);
        startActivity(intent);
    }

    private void openDialogAddExpense() {
        AddExpense dialog = new AddExpense();

        dialog.setOnExpenseAddedListener(this::readRecentsExpensesFromDatabase);
        dialog.show(
                getParentFragmentManager(),
                "AddExpense"
        );
    }
}