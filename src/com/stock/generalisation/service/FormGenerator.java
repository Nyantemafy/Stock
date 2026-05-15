package service;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class FormGenerator {

    public static JPanel genererFormulaire(Class<?> classe, String... attributsChoisis) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));

        Field[] champs = getChamps(classe, attributsChoisis);

        Map<String, JTextField> inputs = new HashMap<>();

        for (Field champ : champs) {
            JLabel label = new JLabel(champ.getName());
            JTextField input = new JTextField();

            inputs.put(champ.getName(), input);

            panel.add(label);
            panel.add(input);
        }

        panel.putClientProperty("inputs", inputs);

        return panel;
    }

    public static String getValeur(JPanel panel, String nomChamp) {
        JTextField input = getInput(panel, nomChamp);
        return input.getText();
    }

    public static void setValeur(JPanel panel, String nomChamp, String valeur) {
        JTextField input = getInput(panel, nomChamp);
        input.setText(valeur);
    }

    public static void vider(JPanel panel) {
        Map<String, JTextField> inputs = getInputs(panel);
        for (JTextField input : inputs.values()) {
            input.setText("");
        }
    }

    public static JTextField getInput(JPanel panel, String nomChamp) {
        return getInputs(panel).get(nomChamp);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, JTextField> getInputs(JPanel panel) {
        return (Map<String, JTextField>) panel.getClientProperty("inputs");
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
