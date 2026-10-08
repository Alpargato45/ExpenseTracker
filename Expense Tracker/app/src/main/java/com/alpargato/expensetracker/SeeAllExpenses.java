package com.alpargato.expensetracker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.alpargato.expensetracker.CustomAdapters.Expense;
import com.alpargato.expensetracker.CustomAdapters.ExpensesAdapter;
import com.alpargato.expensetracker.ExpenseType.ExpenseType;
import com.alpargato.expensetracker.ExpenseType.ExpenseTypeManager;
import com.alpargato.expensetracker.Home_acces_classes.AddExpense;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SeeAllExpenses extends AppCompatActivity {

    private ImageButton btnGoBack, btnSearch, btnDelete;
    private ProgressBar progressBar;
    private ListView listado;
    private TextView txtNoList;
    private Button btnAll, btnDateFrom, btnDateTo;
    private TextInputEditText edtDescriptionSearch, edtMinAmount, edtMaxAmount;
    private TextInputLayout minAmountLayout, maxAmountLayout;
    private AutoCompleteTextView spinnerSort;

    private List<String> selectedCategories = new ArrayList<>();
    private List<Expense> listaGastos = new ArrayList<>();

    private String dateFrom = "";
    private String dateTo = "";

    private final String[] sortOptions = {
            "Newest first",
            "Oldest first",
            "Highest amount",
            "Lowest amount"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_all_expenses);

        getWindow().setNavigationBarColor(
                ContextCompat.getColor(this, R.color.colorPrimary)
        );

        btnGoBack = findViewById(R.id.btnGoBack);
        btnSearch = findViewById(R.id.btnSearch);
        btnDelete = findViewById(R.id.btnDelete);
        btnDateFrom = findViewById(R.id.btnDateFrom);
        btnDateTo = findViewById(R.id.btnDateTo);

        progressBar = findViewById(R.id.progressBar);
        listado = findViewById(R.id.listExpenses);
        txtNoList = findViewById(R.id.txtNoExistRecent);

        edtDescriptionSearch = findViewById(R.id.edtDescriptionSearch);
        edtMinAmount = findViewById(R.id.edtMinAmount);
        edtMaxAmount = findViewById(R.id.edtMaxAmount);

        minAmountLayout = findViewById(R.id.minAmountLayout);
        maxAmountLayout = findViewById(R.id.maxAmountLayout);

        spinnerSort = findViewById(R.id.spinnerSort);

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                sortOptions
        );

        spinnerSort.setAdapter(sortAdapter);
        spinnerSort.setText(sortOptions[0], false);

        btnGoBack.setOnClickListener(v -> finish());
        btnSearch.setOnClickListener(v -> filterExpenses());
        btnDelete.setOnClickListener(v -> clearFilters());

        btnDateFrom.setOnClickListener(v -> selectDate(btnDateFrom, true));
        btnDateTo.setOnClickListener(v -> selectDate(btnDateTo, false));

        createCategoryButtons();
        readRecentsExpensesFromDatabase();

        listado.setOnItemClickListener((parent, itemView, position, id) -> {
            Expense selectedExpense =
                    (Expense) parent.getItemAtPosition(position);

            showExpenseOptions(selectedExpense);
        });
    }

    private void readRecentsExpensesFromDatabase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            progressBar.setVisibility(View.GONE);
            showExpenses(new ArrayList<>());
            return;
        }

        String uid = user.getUid();

        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("expenses")
                .child(uid)
                .child("userExpenses");

        progressBar.setVisibility(View.VISIBLE);

        dbRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                listaGastos.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Object expenseValue = snapshot.child("expense").getValue();

                    double amount = 0.0;

                    if (expenseValue instanceof Number) {
                        amount = ((Number) expenseValue).doubleValue();
                    } else if (expenseValue != null) {
                        try {
                            amount = Double.parseDouble(expenseValue.toString());
                        } catch (NumberFormatException e) {
                            Log.e("Firebase", "Invalid expense amount", e);
                        }
                    }

                    String date = snapshot.child("date").getValue(String.class);
                    String expenseType = snapshot.child("expenseType").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String emoji = getEmojiExpenseType(expenseType);

                    if (date == null) date = "";
                    if (expenseType == null) expenseType = "";
                    if (description == null) description = "";

                    listaGastos.add(
                            new Expense(amount, date, expenseType, description, emoji, snapshot.getKey())
                    );
                }

                Collections.reverse(listaGastos);
                progressBar.setVisibility(View.GONE);
                filterExpenses();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Log.e("Firebase", "Error reading expenses: " + error.getMessage());
                Toast.makeText(
                        SeeAllExpenses.this,
                        "Error loading expenses",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private String getEmojiExpenseType(String category) {
        if (category == null) return "";

        ExpenseTypeManager manager = new ExpenseTypeManager(this);

        for (ExpenseType type : manager.getCategories()) {
            if (category.contains(type.getName())) {
                return type.getEmoji();
            }
        }

        return "";
    }

    private void createCategoryButtons() {
        LinearLayout filterContainer = findViewById(R.id.filterContainer);

        ExpenseTypeManager manager = new ExpenseTypeManager(this);

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

        for (ExpenseType category : manager.getCategories()) {

            Button button = new Button(this);
            button.setText(category.getEmoji() + " " + category.getName());
            button.setTextColor(ContextCompat.getColor(this, R.color.basicWhite));
            button.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.basicBrown)
            );

            button.setOnClickListener(v -> {
                String categoryName = category.getName();

                if (selectedCategories.contains(categoryName)) {
                    selectedCategories.remove(categoryName);

                    button.setBackgroundTintList(
                            ContextCompat.getColorStateList(this, R.color.basicBrown)
                    );

                    if (selectedCategories.isEmpty()) {
                        btnAll.setBackgroundTintList(
                                ContextCompat.getColorStateList(this, R.color.basicGreen)
                        );
                    }
                } else {
                    selectedCategories.add(categoryName);

                    button.setBackgroundTintList(
                            ContextCompat.getColorStateList(this, R.color.basicGreen)
                    );

                    btnAll.setBackgroundTintList(
                            ContextCompat.getColorStateList(this, R.color.basicBrown)
                    );
                }
            });

            filterContainer.addView(button);
        }
    }

    private void filterExpenses() {

        minAmountLayout.setError(null);
        maxAmountLayout.setError(null);

        Double minAmount = parseAmount(edtMinAmount.getText());
        Double maxAmount = parseAmount(edtMaxAmount.getText());

        if (hasText(edtMinAmount) && minAmount == null) {
            minAmountLayout.setError("Enter a valid amount");
            return;
        }

        if (hasText(edtMaxAmount) && maxAmount == null) {
            maxAmountLayout.setError("Enter a valid amount");
            return;
        }

        if (minAmount != null && maxAmount != null && minAmount > maxAmount) {
            maxAmountLayout.setError("Must be greater than minimum");
            return;
        }

        LocalDate from = dateFrom.isEmpty() ? null : parseDate(dateFrom);
        LocalDate to = dateTo.isEmpty() ? null : parseDate(dateTo);

        if (from != null && to != null && from.isAfter(to)) {
            Toast.makeText(
                    this,
                    "Start date cannot be after end date",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String descriptionQuery = edtDescriptionSearch.getText() == null
                ? ""
                : edtDescriptionSearch.getText().toString().trim().toLowerCase(Locale.ROOT);

        List<Expense> filteredExpenses = new ArrayList<>();

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
                matchesDate = from == null && to == null;
            } else {
                if (from != null && expenseDate.isBefore(from)) {
                    matchesDate = false;
                }

                if (to != null && expenseDate.isAfter(to)) {
                    matchesDate = false;
                }
            }

            boolean matchesDescription = expense.getDescription() != null
                    && expense.getDescription().toLowerCase(Locale.ROOT)
                    .contains(descriptionQuery);

            boolean matchesMinAmount = minAmount == null
                    || expense.getExpense() >= minAmount;

            boolean matchesMaxAmount = maxAmount == null
                    || expense.getExpense() <= maxAmount;

            if (matchesCategory && matchesDate && matchesDescription
                    && matchesMinAmount && matchesMaxAmount) {
                filteredExpenses.add(expense);
            }
        }

        sortExpenses(filteredExpenses);
        showExpenses(filteredExpenses);
    }

    private void sortExpenses(List<Expense> expenses) {
        String sort = spinnerSort.getText().toString();

        if ("Oldest first".equals(sort)) {
            expenses.sort(Comparator.comparing(
                    expense -> parseDate(expense.getDate()),
                    Comparator.nullsLast(Comparator.naturalOrder())
            ));
        } else if ("Highest amount".equals(sort)) {
            expenses.sort(
                    Comparator.comparingDouble(Expense::getExpense).reversed()
            );
        } else if ("Lowest amount".equals(sort)) {
            expenses.sort(Comparator.comparingDouble(Expense::getExpense));
        } else {
            expenses.sort(Comparator.comparing(
                    (Expense expense) -> parseDate(expense.getDate()),
                    Comparator.nullsLast(Comparator.reverseOrder())
            ));
        }
    }

    private void showExpenses(List<Expense> expenses) {
        ExpensesAdapter adapter = new ExpensesAdapter(
                this,
                expenses.toArray(new Expense[0])
        );

        listado.setAdapter(adapter);

        boolean empty = expenses.isEmpty();

        listado.setVisibility(empty ? View.GONE : View.VISIBLE);
        txtNoList.setVisibility(empty ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void selectDate(Button button, boolean isFrom) {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (datePicker, year, month, day) -> {

                    String date = String.format(
                            Locale.ROOT,
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

        datePickerDialog.setOnShowListener(dialog -> {
            Button positive = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE);
            Button negative = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE);

            positive.setTextColor(ContextCompat.getColor(this, R.color.basicWhite));
            negative.setTextColor(ContextCompat.getColor(this, R.color.basicWhite));
        });

        datePickerDialog.show();
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.trim().isEmpty()) return null;

        String[] parts = date.trim().split("-");

        if (parts.length != 3) return null;

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

    private Double parseAmount(CharSequence value) {
        if (value == null || value.toString().trim().isEmpty()) return null;

        try {
            return Double.parseDouble(value.toString().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean hasText(TextInputEditText field) {
        return field.getText() != null
                && !field.getText().toString().trim().isEmpty();
    }

    private void clearFilters() {
        selectedCategories.clear();
        dateFrom = "";
        dateTo = "";

        btnDateFrom.setText("From");
        btnDateTo.setText("To");

        edtDescriptionSearch.setText("");
        edtMinAmount.setText("");
        edtMaxAmount.setText("");

        minAmountLayout.setError(null);
        maxAmountLayout.setError(null);

        spinnerSort.setText(sortOptions[0], false);

        LinearLayout filterContainer = findViewById(R.id.filterContainer);

        for (int i = 1; i < filterContainer.getChildCount(); i++) {
            Button button = (Button) filterContainer.getChildAt(i);

            button.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.basicBrown)
            );
        }

        btnAll.setBackgroundTintList(
                ContextCompat.getColorStateList(this, R.color.basicGreen)
        );

        showExpenses(listaGastos);
    }

    private void showExpenseOptions(Expense expense) {

        String[] options = {"Edit Expense", "Delete Expense"};

        new AlertDialog.Builder(this)
                .setTitle("Manage Expense")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        editExpense(expense);
                    } else if (which == 1) {
                        confirmDeleteExpense(expense);
                    }
                })
                .show();
    }

    private void editExpense(Expense expense) {

        AddExpense dialog = new AddExpense();

        dialog.setExpenseToEdit(expense);
        dialog.setOnExpenseAddedListener(
                this::readRecentsExpensesFromDatabase
        );

        dialog.show(getSupportFragmentManager(), "EditExpense");
    }

    private void confirmDeleteExpense(Expense expense) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes, delete", (dialog, which) -> {

                    FirebaseUser user =
                            FirebaseAuth.getInstance().getCurrentUser();

                    if (user == null || expense.getFirebaseKey() == null) {
                        Toast.makeText(this,
                                "Unable to identify this expense",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DatabaseReference expenseRef = FirebaseDatabase
                            .getInstance()
                            .getReference("expenses")
                            .child(user.getUid())
                            .child("userExpenses")
                            .child(expense.getFirebaseKey());

                    expenseRef.removeValue()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this,
                                        "Expense deleted",
                                        Toast.LENGTH_SHORT).show();

                                readRecentsExpensesFromDatabase();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            "Error deleting expense",
                                            Toast.LENGTH_SHORT).show()
                            );
                })
                .show();
    }
}