package service;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public static JPanel genererFormulaires(Class<?> classe, int nombre, String... attributsChoisis) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));

        panel.putClientProperty("classe", classe);
        panel.putClientProperty("attributsChoisis", attributsChoisis);
        panel.putClientProperty("formulaires", new ArrayList<JPanel>());

        for (int i = 0; i < nombre; i++) {
            ajouterFormulaire(panel);
        }

        return panel;
    }

    public static JPanel ajouterFormulaire(JPanel panel) {
        Class<?> classe = (Class<?>) panel.getClientProperty("classe");
        String[] attributsChoisis = (String[]) panel.getClientProperty("attributsChoisis");
        List<JPanel> formulaires = getFormulaires(panel);
        JPanel formulaire = genererFormulaire(classe, attributsChoisis);

        formulaires.add(formulaire);
        panel.add(formulaire);
        numeroterFormulaires(formulaires);
        panel.revalidate();
        panel.repaint();

        return formulaire;
    }

    public static void supprimerDernierFormulaire(JPanel panel) {
        List<JPanel> formulaires = getFormulaires(panel);

        if (formulaires.size() <= 1) {
            return;
        }

        JPanel formulaire = formulaires.remove(formulaires.size() - 1);
        panel.remove(formulaire);
        numeroterFormulaires(formulaires);
        panel.revalidate();
        panel.repaint();
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

    public static boolean estVide(JPanel panel) {
        Map<String, JTextField> inputs = getInputs(panel);
        for (JTextField input : inputs.values()) {
            if (input.getText() != null && !input.getText().trim().equals("")) {
                return false;
            }
        }
        return true;
    }

    public static JTextField getInput(JPanel panel, String nomChamp) {
        return getInputs(panel).get(nomChamp);
    }

    @SuppressWarnings("unchecked")
    public static List<JPanel> getFormulaires(JPanel panel) {
        return (List<JPanel>) panel.getClientProperty("formulaires");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, JTextField> getInputs(JPanel panel) {
        return (Map<String, JTextField>) panel.getClientProperty("inputs");
    }

    private static void numeroterFormulaires(List<JPanel> formulaires) {
        for (int i = 0; i < formulaires.size(); i++) {
            formulaires.get(i).setBorder(BorderFactory.createTitledBorder("Ligne " + (i + 1)));
        }
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
