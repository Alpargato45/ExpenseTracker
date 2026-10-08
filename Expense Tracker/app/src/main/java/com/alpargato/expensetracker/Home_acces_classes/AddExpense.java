package com.alpargato.expensetracker.Home_acces_classes;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.alpargato.expensetracker.AuxiliarClasses.AppPreferences;
import com.alpargato.expensetracker.CustomAdapters.Expense;
import com.alpargato.expensetracker.ExpenseType.ExpenseType;
import com.alpargato.expensetracker.ExpenseType.ExpenseTypeManager;
import com.alpargato.expensetracker.Fragments.HomeFragment;
import com.alpargato.expensetracker.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddExpense extends DialogFragment {

    private Button btnSetDate;
    private Button btnAddExpenseToDB;
    private AutoCompleteTextView expenseTypeSpinner;
    private TextInputEditText txtAddAmount;
    private TextInputEditText txtAddDescription;
    private TextView txtModifyCategories;
    private OnExpenseAddedListener listener;
    private FirebaseAuth auth;

    private TextView txtAddExpenseTitle;
    private Expense expenseToEdit;
    private String editingExpenseKey;

    public AddExpense() {
        // Constructor vacío obligatorio
    }
    public void setOnExpenseAddedListener(OnExpenseAddedListener listener) {
        this.listener = listener;
    }
    public interface OnExpenseAddedListener {
        void onExpenseAdded();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_expense, container, false);

        btnSetDate = view.findViewById(R.id.btnSelectDate);
        btnAddExpenseToDB = view.findViewById(R.id.btnAddExpense);
        expenseTypeSpinner = view.findViewById(R.id.expenseTypeSpinner);
        txtAddAmount = view.findViewById(R.id.edtAmount);
        txtAddDescription = view.findViewById(R.id.edtDescription);
        txtModifyCategories = view.findViewById(R.id.txtModifyCategories);
        txtAddExpenseTitle = view.findViewById(R.id.txtAddExpenseTitle);


        auth = FirebaseAuth.getInstance();
        ExpenseTypeManager manager = new ExpenseTypeManager(requireContext());
        List<ExpenseType> categories = manager.getCategories();
        List<String> categoryNames = new ArrayList<>();

        for (ExpenseType category : categories) {
            categoryNames.add(category.getEmoji() + "  " + category.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_expense_type, categoryNames);
        expenseTypeSpinner.setAdapter(adapter);

        if (expenseToEdit != null) {

            txtAddExpenseTitle.setText("Edit Expense");
            btnAddExpenseToDB.setText("Save");

            txtAddAmount.setText(String.valueOf(expenseToEdit.getExpense()));
            txtAddDescription.setText(expenseToEdit.getDescription());
            btnSetDate.setText(expenseToEdit.getDate());

            String currentCategory = expenseToEdit.getExpenseType().trim();

            for (ExpenseType category : categories) {
                if (currentCategory.equals(category.getName())
                        || currentCategory.endsWith(category.getName())) {

                    expenseTypeSpinner.setText(
                            category.getEmoji() + "  " + category.getName(),
                            false
                    );
                }
            }
        }

        btnSetDate.setOnClickListener(view1 -> {
            setDate();
        });

        btnAddExpenseToDB.setOnClickListener(view1 -> {
            addToDB();
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();

        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.70)
            );
        }
        txtAddAmount.setHint(AppPreferences.obtenerMoneda(requireContext()));
    }

    private void setDate() {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedYear + "-"
                            + (selectedMonth + 1) + "-"
                            + selectedDay;
                    btnSetDate.setText(date);
                },
                year,
                month,
                day
        );
        datePickerDialog.setOnShowListener(dialog -> {
            Button positiveButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE);
            Button negativeButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE);
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.basicWhite));
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.basicWhite));
        });
        datePickerDialog.show();
    }


    private void addToDB() {

        String amountText = txtAddAmount.getText() == null
                ? ""
                : txtAddAmount.getText().toString().trim();

        if (amountText.isEmpty()) {
            txtAddAmount.setError("Enter an amount");
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(amountText.replace(',', '.'));
        } catch (NumberFormatException e) {
            txtAddAmount.setError("Enter a valid amount");
            return;
        }

        String categoryEmoji = expenseTypeSpinner.getText().toString();

        if (categoryEmoji.trim().isEmpty()) {
            expenseTypeSpinner.setError("Select a category");
            return;
        }

        String date = btnSetDate.getText().toString();

        if (date.equals("Select date")) {
            Toast.makeText(requireContext(),
                    "Select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = categoryEmoji.substring(
                categoryEmoji.indexOf(" ") + 1
        );

        String emoji = category.substring(0, category.indexOf(" "));
        String description = txtAddDescription.getText() == null
                ? ""
                : txtAddDescription.getText().toString();

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            return;
        }

        DatabaseReference expenseRef = FirebaseDatabase.getInstance()
                .getReference("expenses")
                .child(user.getUid())
                .child("userExpenses");

        java.util.Map<String, Object> expenseData =
                new java.util.HashMap<>();

        expenseData.put("expense", amount);
        expenseData.put("date", date);
        expenseData.put("expenseType", category);
        expenseData.put("description", description);
        expenseData.put("emoji", emoji);

        if (expenseToEdit != null) {

            if (editingExpenseKey == null || editingExpenseKey.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Unable to identify this expense",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            expenseRef.child(editingExpenseKey)
                    .updateChildren(expenseData)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(requireContext(),
                                "Expense updated successfully",
                                Toast.LENGTH_SHORT).show();

                        if (listener != null) {
                            listener.onExpenseAdded();
                        }

                        dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(),
                                    "Error updating expense",
                                    Toast.LENGTH_SHORT).show()
                    );

        } else {

            expenseRef.push()
                    .setValue(expenseData)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(requireContext(),
                                "Added Successfully",
                                Toast.LENGTH_SHORT).show();

                        if (listener != null) {
                            listener.onExpenseAdded();
                        }

                        dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(),
                                    "Error adding expense",
                                    Toast.LENGTH_SHORT).show()
                    );
        }
    }

    public void setExpenseToEdit(Expense expense) {
        this.expenseToEdit = expense;
        this.editingExpenseKey = expense.getFirebaseKey();
    }
}
