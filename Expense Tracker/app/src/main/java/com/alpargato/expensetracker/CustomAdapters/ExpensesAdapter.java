package com.alpargato.expensetracker.CustomAdapters;

import static android.content.Context.MODE_PRIVATE;
import static java.security.AccessController.getContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alpargato.expensetracker.AuxiliarClasses.AppPreferences;
import com.alpargato.expensetracker.R;

public class ExpensesAdapter extends ArrayAdapter {

    private Expense[] expense;

    public ExpensesAdapter(Context context,Expense[] expense) {
        super(context,R.layout.expense_element,expense);
        this.expense = expense;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        //Inflo el elemento

        LayoutInflater mostrado = LayoutInflater.from(getContext());
        View elemento = mostrado.inflate(R.layout.expense_element,parent,false);

        TextView txtNumber = elemento.findViewById(R.id.txtNumber);
        txtNumber.setText(String.valueOf(expense[position].getExpense()));

        TextView txtDate = elemento.findViewById(R.id.txtDate);
        txtDate.setText(expense[position].getDate());

        TextView txtType = elemento.findViewById(R.id.txtType);
        txtType.setText(expense[position].getExpenseType());

        TextView txtEmoji = elemento.findViewById(R.id.expenseEmoji);
        txtEmoji.setText(expense[position].getEmoji());

        TextView txtMoneySymbol = elemento.findViewById(R.id.txtMoneySymbol);

        AppPreferences pref = new AppPreferences();
        //todo: mirar cuando se haga lo de la moneda bien MONEYSYMBOL

        txtMoneySymbol.setText(pref.obtenerMoneda(elemento.getContext()));

        return elemento;
    }
}
