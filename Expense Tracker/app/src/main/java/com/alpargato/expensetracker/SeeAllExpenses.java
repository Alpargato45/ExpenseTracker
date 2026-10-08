package com.alpargato.expensetracker;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static java.security.AccessController.getContext;

import android.app.DatePickerDialog;
import android.os.Bundle;

import com.alpargato.expensetracker.CustomAdapters.Expense;
import com.alpargato.expensetracker.CustomAdapters.ExpensesAdapter;
import com.alpargato.expensetracker.ExpenseType.ExpenseType;
import com.alpargato.expensetracker.ExpenseType.ExpenseTypeManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alpargato.expensetracker.databinding.ActivitySeeAllExpensesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SeeAllExpenses extends AppCompatActivity {

    private ImageButton btnGoBack;
    private ImageButton btnSearch;
    private ProgressBar progressBar;
    private ListView listado;
    private TextView txtNoList;
    private List<String> selectedCategories = new ArrayList<>();
    private List<Expense> listaGastos = new ArrayList<>();
    private String dateFrom = "";
    private String dateTo = "";
    private Button btnAll;
    private Button btnDateFrom;
    private Button btnDateTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_all_expenses);

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));

        btnGoBack = findViewById(R.id.btnGoBack);
        btnSearch = findViewById(R.id.btnSearch);
        progressBar = findViewById(R.id.progressBar);
        listado = findViewById(R.id.listExpenses);
        txtNoList = findViewById(R.id.txtNoExistRecent);
        btnDateFrom = findViewById(R.id.btnDateFrom);
        btnDateTo = findViewById(R.id.btnDateTo);


        readRecentsExpensesFromDatabase();
        createCategoryButtons();

        btnSearch.setOnClickListener(view -> {
            filterExpenses();
        });

        btnGoBack.setOnClickListener(view -> {
            finish();
        });

        btnDateFrom.setOnClickListener(view -> {
            selectDate(btnDateFrom,true);
        });
        btnDateTo.setOnClickListener(view -> {
            selectDate(btnDateTo,false);
        });
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

        dbRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
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
                            SeeAllExpenses.this,
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
        ExpenseTypeManager manager = new ExpenseTypeManager(SeeAllExpenses.this);
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

    private void createCategoryButtons() {
        LinearLayout filterContainer = findViewById(R.id.filterContainer);

        ExpenseTypeManager manager = new ExpenseTypeManager(this);
        List<ExpenseType> categories = manager.getCategories();

        // BOTÓN ALL
        btnAll = new Button(this);
        btnAll.setText("All");
        btnAll.setTextColor(ContextCompat.getColor(this, R.color.basicWhite));
        btnAll.setBackgroundTintList(
                ContextCompat.getColorStateList(this, R.color.basicGreen)
        );

        btnAll.setOnClickListener(v -> {

            selectedCategories.clear();

            for (int i = 1; i < filterContainer.getChildCount(); i++) {
                Button button = (Button) filterContainer.getChildAt(i);

                button.setBackgroundTintList(
                        ContextCompat.getColorStateList(this, R.color.basicBrown)
                );
            }

            btnAll.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.basicGreen)
            );
        });

        filterContainer.addView(btnAll);

        // CATEGORÍAS
        for (ExpenseType category : categories) {

            Button button = new Button(this);
            button.setText(category.getEmoji() + " " + category.getName());
            button.setTextColor(ContextCompat.getColor(this, R.color.basicWhite));
            button.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.basicBrown)
            );

            button.setOnClickListener(v -> {

                String categoryName = category.getName();

                if (selectedCategories.contains(categoryName)) {

                    // QUITAR CATEGORÍA
                    selectedCategories.remove(categoryName);

                    button.setBackgroundTintList(
                            ContextCompat.getColorStateList(this, R.color.basicBrown)
                    );

                    // Si no queda ninguna → ALL
                    if (selectedCategories.isEmpty()) {
                        btnAll.setBackgroundTintList(
                                ContextCompat.getColorStateList(this, R.color.basicGreen)
                        );
                    }
                } else {
                    // AÑADIR CATEGORÍA
                    selectedCategories.add(categoryName);
                    button.setBackgroundTintList(
                            ContextCompat.getColorStateList(this, R.color.basicGreen)
                    );
                    // Al seleccionar una categoría → quitar ALL
                    btnAll.setBackgroundTintList(
                            ContextCompat.getColorStateList(this, R.color.basicBrown)
                    );
                }
            });

            filterContainer.addView(button);
        }
    }

    private void filterExpenses() {

        List<Expense> filteredExpenses = new ArrayList<>();

        LocalDate from = dateFrom.isEmpty() ? null : parseDate(dateFrom);
        LocalDate to = dateTo.isEmpty() ? null : parseDate(dateTo);

        for (Expense expense : listaGastos) {

            boolean matchesCategory = selectedCategories.isEmpty();
            boolean matchesDate = true;

            for (String category : selectedCategories) {
                if (expense.getExpenseType().contains(category)) {
                    matchesCategory = true;
                }
            }

            LocalDate expenseDate = parseDate(expense.getDate());

            if (expenseDate == null) {
                matchesDate = false;
            }

            if (from != null && expenseDate != null && expenseDate.isBefore(from)) {
                matchesDate = false;
            }

            if (to != null && expenseDate != null && expenseDate.isAfter(to)) {
                matchesDate = false;
            }

            if (matchesCategory && matchesDate) {
                filteredExpenses.add(expense);
            }
        }

        ExpensesAdapter miAdaptador = new ExpensesAdapter(
                this,
                filteredExpenses.toArray(new Expense[0])
        );

        listado.setAdapter(miAdaptador);
    }

    private void selectDate(Button button, boolean isFrom) {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (datePicker, year, month, day) -> {

                    String date = String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            day
                    );

                    button.setText(date);

                    if (isFrom) {
                        dateFrom = date;
                    } else {
                        dateTo = date;
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private LocalDate parseDate(String date) {

        String[] parts = date.split("-");

        if (parts.length != 3) {
            return null;
        }

        try {

            int year;
            int month;
            int day;

            if (parts[0].length() == 4) {
                year = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]);
                day = Integer.parseInt(parts[2]);
            } else {
                day = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]);
                year = Integer.parseInt(parts[2]);
            }

            return LocalDate.of(year, month, day);

        } catch (Exception e) {
            return null;
        }
    }
}