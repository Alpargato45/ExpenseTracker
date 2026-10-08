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


        auth = FirebaseAuth.getInstance();
        ExpenseTypeManager manager = new ExpenseTypeManager(requireContext());
        List<ExpenseType> categories = manager.getCategories();
        List<String> categoryNames = new ArrayList<>();

        for (ExpenseType category : categories) {
            categoryNames.add(category.getEmoji() + "  " + category.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_expense_type, categoryNames);
        expenseTypeSpinner.setAdapter(adapter);

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
        double amount = Double.parseDouble(txtAddAmount.getText().toString());
        String categoryEmoji = expenseTypeSpinner.getText().toString();
        String category = categoryEmoji.substring(categoryEmoji.indexOf(" ") + 1);
        String date = btnSetDate.getText().toString();
        String description = txtAddDescription.getText().toString();
        String emoji = category.substring(0, category.indexOf(" "));

        if (description == null) {
            description = "";
        }

        Expense expense = new Expense(amount,date,category, description,emoji);

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String UID = user.getUid();

        DatabaseReference expenseRef = FirebaseDatabase.getInstance().getReference("expenses").child(UID).child("userExpenses");

        expenseRef.push().setValue(expense).addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Added Successfully", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onExpenseAdded();
                    }
                    dismiss();
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error adding expense", Toast.LENGTH_SHORT).show();
                });
    }
}
