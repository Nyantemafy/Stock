package service;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class AffichageGenerator {

    public static JFrame genererFenetre(String titre, int largeur, int hauteur) {
        JFrame frame = new JFrame(titre);
        frame.setSize(largeur, hauteur);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        return frame;
    }

    public static JFrame genererFenetreSecondaire(String titre, int largeur, int hauteur) {
        JFrame frame = new JFrame(titre);
        frame.setSize(largeur, hauteur);
        frame.setLayout(new BorderLayout());
        return frame;
    }

    public static JPanel genererPanelSimple() {
        return new JPanel(new FlowLayout());
    }

    public static JPanel genererPanelBordure() {
        return new JPanel(new BorderLayout());
    }

    public static JPanel genererPanelGrille(int lignes, int colonnes) {
        return new JPanel(new GridLayout(lignes, colonnes));
    }

    public static JLabel genererLabel(String texte) {
        return new JLabel(texte);
    }

    public static JButton genererBouton(String texte, Runnable action) {
        JButton bouton = new JButton(texte);
        bouton.addActionListener(e -> action.run());
        return bouton;
    }

    public static JPanel genererPanelAvecBouton(String texteBouton, Runnable action) {
        JPanel panel = genererPanelSimple();
        ajouterDansPanel(panel, genererBouton(texteBouton, action));
        return panel;
    }

    public static void ajouterNord(JFrame frame, JPanel panel) {
        frame.add(panel, BorderLayout.NORTH);
    }

    public static void ajouterCentre(JFrame frame, JPanel panel) {
        frame.add(panel, BorderLayout.CENTER);
    }

    public static void ajouterBas(JFrame frame, JPanel panel) {
        frame.add(panel, BorderLayout.SOUTH);
    }

    public static void ajouterDansPanel(JPanel panel, java.awt.Component composant) {
        panel.add(composant);
    }

    public static void afficherTableDansPanel(JPanel panel, JTable table) {
        panel.removeAll();
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    public static int confirmer(java.awt.Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirmation", JOptionPane.YES_NO_OPTION);
    }

    public static boolean estConfirme(java.awt.Component parent, String message) {
        return confirmer(parent, message) == JOptionPane.YES_OPTION;
    }

    public static boolean validerFormulaire(java.awt.Component parent, JPanel formulaire, String titre) {
        int choix = JOptionPane.showConfirmDialog(parent, formulaire, titre, JOptionPane.OK_CANCEL_OPTION);
        return choix == JOptionPane.OK_OPTION;
    }

    public static void afficherErreur(java.awt.Component parent, Exception e) {
        JOptionPane.showMessageDialog(parent, e.getMessage());
    }

    public static void afficherFenetre(JFrame frame) {
        frame.setVisible(true);
    }

    public static void fermerFenetre(JFrame frame) {
        frame.dispose();
    }
}
