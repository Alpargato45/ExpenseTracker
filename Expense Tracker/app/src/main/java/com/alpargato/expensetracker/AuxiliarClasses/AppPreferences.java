package com.alpargato.expensetracker.AuxiliarClasses;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
    private static final String PREFS_NAME = "AppPreferences";
    private static final String KEY_CURRENCY = "tipoMoneda";

    public static void guardarMoneda(Context context, String moneda) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );

        prefs.edit()
                .putString(KEY_CURRENCY, moneda)
                .apply();
    }

    public static String obtenerMoneda(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );

        return prefs.getString(KEY_CURRENCY, "EUR");
    }
}
