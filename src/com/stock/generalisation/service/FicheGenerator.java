package service;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.Arrays;

public class FicheGenerator {

    public static JPanel genererFiche(Object objet, String... attributsChoisis) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));

        if (objet == null) {
            panel.add(new JLabel("Aucune donnee"));
            return panel;
        }

        Class<?> classe = objet.getClass();
        Field[] champs = getChamps(classe, attributsChoisis);

        for (Field champ : champs) {
            try {
                champ.setAccessible(true);

                JLabel label = new JLabel(champ.getName() + " :");
                JLabel valeur = new JLabel(String.valueOf(champ.get(objet)));

                panel.add(label);
                panel.add(valeur);

            } catch (Exception e) {
                panel.add(new JLabel(champ.getName() + " :"));
                panel.add(new JLabel("Erreur"));
            }
        }

        return panel;
    }

    private static Field[] getChamps(Class<?> classe, String... attributsChoisis) {
        Field[] tousLesChamps = classe.getDeclaredFields();

        if (attributsChoisis == null || attributsChoisis.length == 0) {
            return tousLesChamps;
        }

        return Arrays.stream(tousLesChamps)
                .filter(field -> Arrays.asList(attributsChoisis).contains(field.getName()))
                .toArray(Field[]::new);
    }
}
