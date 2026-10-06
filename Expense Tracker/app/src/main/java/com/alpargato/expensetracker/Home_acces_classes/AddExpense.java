package com.alpargato.expensetracker.Home_acces_classes;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.alpargato.expensetracker.R;

public class AddExpense extends AppCompatActivity {

    private Button btnSetDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_expense);

        //Paint the utility movile buttons to the color i want
        getWindow().setNavigationBarColor(
                ContextCompat.getColor(this, R.color.colorPrimary)
        );


    }
}
