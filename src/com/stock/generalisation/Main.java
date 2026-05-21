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
import javax.swing.JTable;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
            int idArticle = getIdArticleSelectionne();

            JPanel mouvementForm = FormGenerator.genererFormulaire(MouvementForm.class,
                    "nombre", "prixUnitaire", "dateMouvement", "idSource");

            if (!AffichageGenerator.validerFormulaire(frame, mouvementForm, "Entree stock")) {
                return;
            }

            BigDecimal nombre = new BigDecimal(FormGenerator.getValeur(mouvementForm, "nombre"));
            BigDecimal pu = new BigDecimal(FormGenerator.getValeur(mouvementForm, "prixUnitaire"));
            String dateTexte = FormGenerator.getValeur(mouvementForm, "dateMouvement");
            String idSource = FormGenerator.getValeur(mouvementForm, "idSource");

            Timestamp date = new Timestamp(new SimpleDateFormat("yyyy-MM-dd").parse(dateTexte).getTime());

            dao.ajouterMouvementStock(idArticle, "ENTREE", nombre, pu, date, idSource);
            afficherArticles();

        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void ajouterSortieStock() {
        try {
            Article article = getArticleSelectionneDepuisTable();

            JPanel sortieForm = FormGenerator.genererFormulaire(model.SortieStockForm.class,
                    "nombre", "dateMouvement");

            JPanel ficheStock = FicheGenerator.genererFiche(article,
                    "idArticle", "nom", "modeGestion", "quantiteStock", "valeurStock");

            JPanel panel = AffichageGenerator.genererPanelGrille(2, 1);
            AffichageGenerator.ajouterDansPanel(panel, ficheStock);
            AffichageGenerator.ajouterDansPanel(panel, sortieForm);

            if (!AffichageGenerator.validerFormulaire(frame, panel, "Sortie stock")) {
                return;
            }

            BigDecimal nombre = new BigDecimal(FormGenerator.getValeur(sortieForm, "nombre"));
            String dateTexte = FormGenerator.getValeur(sortieForm, "dateMouvement");

            Timestamp date = new Timestamp(new SimpleDateFormat("yyyy-MM-dd").parse(dateTexte).getTime());

            dao.ajouterSortieStock(article.getIdArticle(), article.getModeGestion(), nombre, date);
            afficherArticles();

        } catch (Exception e) {
            afficherErreur(e);
        }
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
