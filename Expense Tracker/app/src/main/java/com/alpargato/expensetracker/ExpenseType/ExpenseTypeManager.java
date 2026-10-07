package com.alpargato.expensetracker.ExpenseType;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ExpenseTypeManager {

    private static final String PREFS_NAME = "expense_preferences";
    private static final String CATEGORIES_KEY = "expense_types";

    private final SharedPreferences preferences;
    private final Gson gson;

    public ExpenseTypeManager(Context context) {

        preferences = context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );

        gson = new Gson();
    }

    // Obtener todas las categorías
    public List<ExpenseType> getCategories() {

        String json = preferences.getString(CATEGORIES_KEY, null);

        if (json == null) {
            return getDefaultCategories();
        }

        Type type = new TypeToken<List<ExpenseType>>() {}.getType();

        return gson.fromJson(json, type);
    }

    // Añadir una categoría
    public void addCategory(ExpenseType category) {

        List<ExpenseType> categories = getCategories();

        categories.add(category);

        saveCategories(categories);
    }

    // Borrar una categoría
    public void deleteCategory(String name) {

        List<ExpenseType> categories = getCategories();

        categories.removeIf(category ->
                category.getName().equals(name)
        );

        saveCategories(categories);
    }

    // Guardar la lista
    private void saveCategories(List<ExpenseType> categories) {

        String json = gson.toJson(categories);

        preferences.edit()
                .putString(CATEGORIES_KEY, json)
                .apply();
    }

    // Categorías iniciales
    private List<ExpenseType> getDefaultCategories() {

        List<ExpenseType> categories = new ArrayList<>();

        categories.add(new ExpenseType("Food", "🍔"));
        categories.add(new ExpenseType("Transport", "🚗"));
        categories.add(new ExpenseType("Entertainment", "🎮"));
        categories.add(new ExpenseType("Shopping", "🛍️"));
        categories.add(new ExpenseType("Health", "💊"));
        categories.add(new ExpenseType("Other", "📦"));

        saveCategories(categories);

        return categories;
    }
}
