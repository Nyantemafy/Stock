import dao.ArticleDao;
import model.Article;
import model.ArticleForm;
import model.FiltreDateForm;
import model.MouvementStock;
import model.MouvementForm;
import service.AffichageGenerator;
import service.FicheGenerator;
import service.FormGenerator;
import service.SwingGenerator;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private ArticleDao dao = new ArticleDao();
    private JFrame frame;
    private JPanel centre;
    private JTable tableArticles;
    private JLabel etatGlobal;
    private JLabel valeurStockGlobal;
    private JPanel filtreForm;
    private JPanel articleForm;

    public static void main(String[] args) {
        Main app = new Main();
        app.demarrer();
    }

    private void demarrer() {
        frame = AffichageGenerator.genererFenetre("Gestion de stock", 900, 550);

        AffichageGenerator.ajouterNord(frame, creerPanelHaut());

        centre = AffichageGenerator.genererPanelBordure();
        AffichageGenerator.ajouterCentre(frame, centre);

        AffichageGenerator.ajouterBas(frame, creerPanelBas());

        FormGenerator.setValeur(articleForm, "idTypeGestion", "1");
        afficherArticles();
        AffichageGenerator.afficherFenetre(frame);
    }

    private JPanel creerPanelHaut() {
        JPanel panel = AffichageGenerator.genererPanelGrille(2, 1);

        JPanel filtrePanel = AffichageGenerator.genererPanelSimple();
        filtreForm = FormGenerator.genererFormulaire(FiltreDateForm.class, "dateDebut", "dateFin");

        AffichageGenerator.ajouterDansPanel(filtrePanel, filtreForm);
        AffichageGenerator.ajouterDansPanel(filtrePanel,
                AffichageGenerator.genererBouton("Filtrer", () -> afficherArticles()));

        JPanel formPanel = AffichageGenerator.genererPanelSimple();
        articleForm = FormGenerator.genererFormulaire(ArticleForm.class, "nom", "idTypeGestion");

        AffichageGenerator.ajouterDansPanel(formPanel, articleForm);
        AffichageGenerator.ajouterDansPanel(formPanel,
                AffichageGenerator.genererLabel("Modes: 1 FIFO, 2 LIFO, 3 CUMP"));
        AffichageGenerator.ajouterDansPanel(formPanel,
                AffichageGenerator.genererBouton("Creer", () -> creerArticle()));
        AffichageGenerator.ajouterDansPanel(formPanel,
                AffichageGenerator.genererBouton("Modifier", () -> modifierArticle()));

        AffichageGenerator.ajouterDansPanel(panel, filtrePanel);
        AffichageGenerator.ajouterDansPanel(panel, formPanel);
        return panel;
    }

    private void ajouterEntreeStock() {
        try {
            Article article = getArticleSelectionneDepuisTable();

            JPanel mouvementForms = FormGenerator.genererFormulaires(MouvementForm.class, 1,
                    "nombre", "prixUnitaire", "dateMouvement", "idSource");

            JPanel ficheStock = FicheGenerator.genererFiche(article,
                    "idArticle", "nom", "modeGestion", "quantiteStock", "valeurStock");

            JPanel panel = AffichageGenerator.genererPanelGrille(2, 1);
            AffichageGenerator.ajouterDansPanel(panel, ficheStock);
            AffichageGenerator.ajouterDansPanel(panel, creerPanelSaisiesMultiples(mouvementForms));

            if (!AffichageGenerator.validerFormulaire(frame, panel, "Entree stock")) {
                return;
            }

            List<MouvementStock> mouvements = lireEntreesStock(mouvementForms);

            dao.ajouterMouvementsStock(article.getIdArticle(), mouvements);
            afficherArticles();

        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void ajouterSortieStock() {
        try {
            Article article = getArticleSelectionneDepuisTable();

            JPanel sortieForms = FormGenerator.genererFormulaires(model.SortieStockForm.class, 1,
                    "nombre", "dateMouvement");

            JPanel ficheStock = FicheGenerator.genererFiche(article,
                    "idArticle", "nom", "modeGestion", "quantiteStock", "valeurStock");

            JPanel panel = AffichageGenerator.genererPanelGrille(2, 1);
            AffichageGenerator.ajouterDansPanel(panel, ficheStock);
            AffichageGenerator.ajouterDansPanel(panel, creerPanelSaisiesMultiples(sortieForms));

            if (!AffichageGenerator.validerFormulaire(frame, panel, "Sortie stock")) {
                return;
            }

            List<MouvementStock> mouvements = lireSortiesStock(sortieForms);

            dao.ajouterSortiesStock(article.getIdArticle(), article.getModeGestion(), mouvements);
            afficherArticles();

        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private JPanel creerPanelSaisiesMultiples(JPanel formulaires) {
        JPanel panel = AffichageGenerator.genererPanelBordure();
        JPanel boutons = AffichageGenerator.genererPanelSimple();
        JScrollPane scroll = AffichageGenerator.genererScrollPane(formulaires);

        scroll.setPreferredSize(new Dimension(520, 260));

        AffichageGenerator.ajouterDansPanel(boutons,
                AffichageGenerator.genererBouton("Ajouter ligne", () -> FormGenerator.ajouterFormulaire(formulaires)));
        AffichageGenerator.ajouterDansPanel(boutons,
                AffichageGenerator.genererBouton("Supprimer ligne",
                        () -> FormGenerator.supprimerDernierFormulaire(formulaires)));

        panel.add(scroll, java.awt.BorderLayout.CENTER);
        panel.add(boutons, java.awt.BorderLayout.SOUTH);
        return panel;
    }

    private List<MouvementStock> lireEntreesStock(JPanel mouvementForms) throws Exception {
        List<MouvementStock> mouvements = new ArrayList<MouvementStock>();

        for (JPanel mouvementForm : FormGenerator.getFormulaires(mouvementForms)) {
            if (FormGenerator.estVide(mouvementForm)) {
                continue;
            }

            BigDecimal nombre = new BigDecimal(lireValeurObligatoire(mouvementForm, "nombre"));
            BigDecimal pu = new BigDecimal(lireValeurObligatoire(mouvementForm, "prixUnitaire"));
            Timestamp date = lireDateMouvement(lireValeurObligatoire(mouvementForm, "dateMouvement"));
            String idSource = FormGenerator.getValeur(mouvementForm, "idSource");

            mouvements.add(new MouvementStock("ENTREE", nombre, pu, date, idSource));
        }

        verifierMouvementsSaisis(mouvements);
        return mouvements;
    }

    private List<MouvementStock> lireSortiesStock(JPanel sortieForms) throws Exception {
        List<MouvementStock> mouvements = new ArrayList<MouvementStock>();

        for (JPanel sortieForm : FormGenerator.getFormulaires(sortieForms)) {
            if (FormGenerator.estVide(sortieForm)) {
                continue;
            }

            BigDecimal nombre = new BigDecimal(lireValeurObligatoire(sortieForm, "nombre"));
            Timestamp date = lireDateMouvement(lireValeurObligatoire(sortieForm, "dateMouvement"));

            mouvements.add(new MouvementStock("SORTIE", nombre, BigDecimal.ZERO, date, null));
        }

        verifierMouvementsSaisis(mouvements);
        return mouvements;
    }

    private void verifierMouvementsSaisis(List<MouvementStock> mouvements) {
        if (mouvements.isEmpty()) {
            throw new RuntimeException("Saisir au moins une ligne.");
        }
    }

    private String lireValeurObligatoire(JPanel formulaire, String champ) {
        String valeur = FormGenerator.getValeur(formulaire, champ);

        if (valeur == null || valeur.trim().equals("")) {
            throw new RuntimeException("Champ obligatoire : " + champ);
        }

        return valeur.trim();
    }

    private Timestamp lireDateMouvement(String texte) throws Exception {
        return new Timestamp(new SimpleDateFormat("yyyy-MM-dd").parse(texte).getTime());
    }

    private JPanel creerPanelBas() {
        JPanel panel = AffichageGenerator.genererPanelGrille(2, 1);
        JPanel statistiques = AffichageGenerator.genererPanelSimple();
        JPanel actions = AffichageGenerator.genererPanelSimple();

        etatGlobal = AffichageGenerator.genererLabel("Stock global: 0");
        valeurStockGlobal = AffichageGenerator.genererLabel("0");

        AffichageGenerator.ajouterDansPanel(statistiques, etatGlobal);
        AffichageGenerator.ajouterDansPanel(statistiques, creerCarteStatistique(
                "Valeur de stock global",
                valeurStockGlobal,
                "Somme des valeurs de stock par article."));

        AffichageGenerator.ajouterDansPanel(actions, AffichageGenerator.genererBouton("Detail", () -> afficherDetail()));
        AffichageGenerator.ajouterDansPanel(actions,
                AffichageGenerator.genererBouton("Entree stock", () -> ajouterEntreeStock()));
        AffichageGenerator.ajouterDansPanel(actions,
                AffichageGenerator.genererBouton("Sortie stock", () -> ajouterSortieStock()));
        AffichageGenerator.ajouterDansPanel(actions,
                AffichageGenerator.genererBouton("Supprimer article", () -> supprimerArticle()));

        AffichageGenerator.ajouterDansPanel(panel, statistiques);
        AffichageGenerator.ajouterDansPanel(panel, actions);
        return panel;
    }

    private JPanel creerCarteStatistique(String titre, JLabel valeur, String description) {
        JPanel carte = new JPanel(new GridLayout(3, 1));
        carte.setBorder(BorderFactory.createTitledBorder(titre));
        carte.add(valeur);
        carte.add(AffichageGenerator.genererLabel(description));
        return carte;
    }

    private void afficherArticles() {
        try {
            Date dateDebut = lireDate(FormGenerator.getValeur(filtreForm, "dateDebut"));
            Date dateFin = lireDate(FormGenerator.getValeur(filtreForm, "dateFin"));
            List<Article> articles = dao.listerArticles(dateDebut, dateFin);

            tableArticles = SwingGenerator.genererTable(articles,
                    "idArticle", "nom", "modeGestion", "quantiteStock", "valeurStock");

            AffichageGenerator.afficherTableDansPanel(centre, tableArticles);

            BigDecimal stockGlobal = dao.calculerStockGlobal(dateDebut, dateFin);
            etatGlobal.setText("Stock global: " + stockGlobal);

            BigDecimal valeurGlobale = dao.calculerValeurStockGlobal(dateDebut, dateFin);
            valeurStockGlobal.setText(String.valueOf(valeurGlobale));
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void creerArticle() {
        try {
            String nom = FormGenerator.getValeur(articleForm, "nom");
            int idTypeGestion = Integer.parseInt(FormGenerator.getValeur(articleForm, "idTypeGestion"));
            dao.creerArticle(nom, idTypeGestion);
            FormGenerator.vider(articleForm);
            FormGenerator.setValeur(articleForm, "idTypeGestion", "1");
            afficherArticles();
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void modifierArticle() {
        try {
            int idArticle = getIdArticleSelectionne();
            String nom = FormGenerator.getValeur(articleForm, "nom");
            int idTypeGestion = Integer.parseInt(FormGenerator.getValeur(articleForm, "idTypeGestion"));
            dao.modifierArticle(idArticle, nom, idTypeGestion);
            afficherArticles();
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void supprimerArticle() {
        try {
            int idArticle = getIdArticleSelectionne();
            if (AffichageGenerator.estConfirme(frame, "Supprimer cet article et tous ses mouvements ?")) {
                dao.supprimerArticle(idArticle);
                afficherArticles();
            }
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void afficherDetail() {
        try {
            ouvrirDetail(getArticleSelectionneDepuisTable());
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void supprimerMouvement(JTable tableDetail, JFrame detailFrame, Article article) {
        try {
            int idMouvementStock = SwingGenerator.getIntSelectionne(tableDetail, 0);

            if (AffichageGenerator.estConfirme(detailFrame, "Supprimer ce mouvement ?")) {
                dao.supprimerMouvementStock(idMouvementStock);
                AffichageGenerator.fermerFenetre(detailFrame);
                afficherArticles();
                ouvrirDetail(article);
            }
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void ouvrirDetail(Article article) {
        try {
            Date dateDebut = lireDate(FormGenerator.getValeur(filtreForm, "dateDebut"));
            Date dateFin = lireDate(FormGenerator.getValeur(filtreForm, "dateFin"));
            List<MouvementStock> details = dao.listerDetails(article.getIdArticle(), dateDebut, dateFin);
            JTable tableDetail;

            if ("CUMP".equalsIgnoreCase(article.getModeGestion())) {
                tableDetail = SwingGenerator.genererTable(details,
                        "idMouvementStock", "typeMouvement", "nombre", "prixUnitaire", "dateMouvement", "idSource",
                        "coutUnitaireMoyenPondere");
            } else {
                tableDetail = SwingGenerator.genererTable(details,
                        "idMouvementStock", "typeMouvement", "nombre", "prixUnitaire", "dateMouvement", "idSource",
                        "coutTotal");
            }

            JFrame detailFrame = AffichageGenerator.genererFenetreSecondaire("Detail des mouvements de stock", 900,
                    420);
            JPanel centreDetail = AffichageGenerator.genererPanelBordure();

            AffichageGenerator.ajouterNord(detailFrame, FicheGenerator.genererFiche(article,
                    "idArticle", "nom", "modeGestion", "quantiteStock", "valeurStock"));
            AffichageGenerator.afficherTableDansPanel(centreDetail, tableDetail);
            AffichageGenerator.ajouterCentre(detailFrame, centreDetail);
            AffichageGenerator.ajouterBas(detailFrame,
                    AffichageGenerator.genererPanelAvecBouton("Supprimer mouvement",
                            () -> supprimerMouvement(tableDetail, detailFrame, article)));
            AffichageGenerator.afficherFenetre(detailFrame);
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private int getIdArticleSelectionne() {
        return SwingGenerator.getIntSelectionne(tableArticles, 0);
    }

    private Article getArticleSelectionneDepuisTable() {
        return new Article(
                SwingGenerator.getIntSelectionne(tableArticles, 0),
                SwingGenerator.getStringSelectionne(tableArticles, 1),
                SwingGenerator.getStringSelectionne(tableArticles, 2),
                SwingGenerator.getBigDecimalSelectionne(tableArticles, 3),
                SwingGenerator.getBigDecimalSelectionne(tableArticles, 4));
    }

    private Date lireDate(String texte) throws Exception {
        if (texte == null || texte.trim().equals("")) {
            return null;
        }
        java.util.Date date = new SimpleDateFormat("yyyy-MM-dd").parse(texte);
        return new Date(date.getTime());
    }

    private void afficherErreur(Exception e) {
        AffichageGenerator.afficherErreur(frame, e);
    }
}
