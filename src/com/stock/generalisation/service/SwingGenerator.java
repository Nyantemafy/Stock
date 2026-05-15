package service;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.util.List;

public class SwingGenerator {

    public static JTable genererTable(List<?> objets, String... attributsChoisis) {
        DefaultTableModel model = new DefaultTableModel();

        if (objets == null || objets.isEmpty()) {
            return new JTable(model);
        }

        Class<?> classe = objets.get(0).getClass();

        Field[] champs = getChamps(classe, attributsChoisis);

        for (Field champ : champs) {
            model.addColumn(champ.getName());
        }

        for (Object obj : objets) {
            Object[] ligne = new Object[champs.length];

            for (int i = 0; i < champs.length; i++) {
                try {
                    champs[i].setAccessible(true);
                    ligne[i] = champs[i].get(obj);
                } catch (Exception e) {
                    ligne[i] = "";
                }
            }

            model.addRow(ligne);
        }

        return new JTable(model);
    }

    public static Object getValeurSelectionnee(JTable table, int colonne) {
        int ligne = table.getSelectedRow();
        if (ligne < 0) {
            throw new RuntimeException("Selectionner d'abord une ligne dans le tableau.");
        }
        return table.getValueAt(ligne, colonne);
    }

    public static int getIntSelectionne(JTable table, int colonne) {
        return Integer.parseInt(getValeurSelectionnee(table, colonne).toString());
    }

    public static String getStringSelectionne(JTable table, int colonne) {
        return getValeurSelectionnee(table, colonne).toString();
    }

    public static BigDecimal getBigDecimalSelectionne(JTable table, int colonne) {
        return new BigDecimal(getValeurSelectionnee(table, colonne).toString());
    }

    private static Field[] getChamps(Class<?> classe, String... attributsChoisis) {
        Field[] tousLesChamps = classe.getDeclaredFields();

        if (attributsChoisis == null || attributsChoisis.length == 0) {
            return tousLesChamps;
        }

        return java.util.Arrays.stream(tousLesChamps)
                .filter(field -> java.util.Arrays.asList(attributsChoisis).contains(field.getName()))
                .toArray(Field[]::new);
    }
}
