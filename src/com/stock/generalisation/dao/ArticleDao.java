package dao;

import model.Article;
import model.MouvementStock;
import model.TypeGestion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ArticleDao {

    public List<TypeGestion> listerTypesGestion() throws SQLException {
        List<TypeGestion> types = new ArrayList<TypeGestion>();
        String sql = "select id_type_gestion, libeller from type_gestion order by id_type_gestion";

        Connection con = ConnexionPostgres.ouvrir();
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            types.add(new TypeGestion(rs.getInt("id_type_gestion"), rs.getString("libeller")));
        }

        rs.close();
        ps.close();
        con.close();
        return types;
    }

    public List<Article> listerArticles(Date dateDebut, Date dateFin) throws SQLException {
        List<Article> articles = new ArrayList<Article>();

        String sql = ""
                + "select "
                + "a.id_article, "
                + "a.nom, "
                + "tg.libeller as mode_gestion, "
                + "coalesce(sum(ms.nombre_signe), 0) as quantite_stock, "
                + "coalesce(sum(ms.valeur_signe), 0) as valeur_stock "
                + "from article a "
                + "join type_gestion tg on tg.id_type_gestion = a.id_type_gestion "
                + "left join ( "
                + "    select "
                + "    id_article, "
                + "    date_mouvement, "
                + "    case when type_mouvement = 'ENTREE' then nombre else -nombre end as nombre_signe, "
                + "    case when type_mouvement = 'ENTREE' then nombre * pu else -nombre * pu end as valeur_signe "
                + "    from mouvement_stock "
                + ") ms on ms.id_article = a.id_article "
                + "and (? is null or ms.date_mouvement::date >= ?) "
                + "and (? is null or ms.date_mouvement::date <= ?) "
                + "group by a.id_article, a.nom, tg.libeller "
                + "order by a.nom";

        Connection con = ConnexionPostgres.ouvrir();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setDate(1, dateDebut);
        ps.setDate(2, dateDebut);
        ps.setDate(3, dateFin);
        ps.setDate(4, dateFin);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            BigDecimal quantiteStock = rs.getBigDecimal("quantite_stock");
            BigDecimal valeurStock = rs.getBigDecimal("valeur_stock");
            String modeGestion = rs.getString("mode_gestion");

            if ("CUMP".equalsIgnoreCase(modeGestion)) {
                BigDecimal dernierCump = calculerDernierCump(
                        con,
                        rs.getInt("id_article"),
                        dateDebut,
                        dateFin);

                valeurStock = quantiteStock
                        .multiply(dernierCump)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            articles.add(new Article(
                    rs.getInt("id_article"),
                    rs.getString("nom"),
                    modeGestion,
                    quantiteStock,
                    valeurStock));
        }

        rs.close();
        ps.close();
        con.close();

        return articles;
    }

    public BigDecimal calculerStockGlobal(Date dateDebut, Date dateFin) throws SQLException {
        String sql = ""
                + "select coalesce(sum(stock_article), 0) as stock_global "
                + "from ( "
                + "select upper(trim(a.nom)) as article_unique, "
                + "coalesce(sum(case when ms.type_mouvement = 'ENTREE' then ms.nombre else -ms.nombre end), 0) "
                + "as stock_article "
                + "from article a "
                + "left join mouvement_stock ms on ms.id_article = a.id_article "
                + "and (cast(? as date) is null or ms.date_mouvement::date >= cast(? as date)) "
                + "and (cast(? as date) is null or ms.date_mouvement::date <= cast(? as date)) "
                + "group by upper(trim(a.nom)) "
                + ") stocks";

        Connection con = ConnexionPostgres.ouvrir();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setDate(1, dateDebut);
        ps.setDate(2, dateDebut);
        ps.setDate(3, dateFin);
        ps.setDate(4, dateFin);
        ResultSet rs = ps.executeQuery();

        BigDecimal stockGlobal = BigDecimal.ZERO;
        if (rs.next()) {
            stockGlobal = rs.getBigDecimal("stock_global");
        }

        rs.close();
        ps.close();
        con.close();
        return stockGlobal;
    }

    public BigDecimal calculerValeurStockGlobal(Date dateDebut, Date dateFin) throws SQLException {
        String sql = ""
                + "select coalesce(sum(valeur_article), 0) as valeur_stock_global "
                + "from ( "
                + "select upper(trim(a.nom)) as article_unique, "
                + "coalesce(sum(case when ms.type_mouvement = 'ENTREE' then ms.nombre * ms.pu "
                + "else -ms.nombre * ms.pu end), 0) as valeur_article "
                + "from article a "
                + "left join mouvement_stock ms on ms.id_article = a.id_article "
                + "and (cast(? as date) is null or ms.date_mouvement::date >= cast(? as date)) "
                + "and (cast(? as date) is null or ms.date_mouvement::date <= cast(? as date)) "
                + "group by upper(trim(a.nom)) "
                + ") valeurs";

        Connection con = ConnexionPostgres.ouvrir();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setDate(1, dateDebut);
        ps.setDate(2, dateDebut);
        ps.setDate(3, dateFin);
        ps.setDate(4, dateFin);
        ResultSet rs = ps.executeQuery();

        BigDecimal valeurStockGlobal = BigDecimal.ZERO;
        if (rs.next()) {
            valeurStockGlobal = rs.getBigDecimal("valeur_stock_global");
        }

        rs.close();
        ps.close();
        con.close();
        return valeurStockGlobal;
    }

    public void creerArticle(String nom, int idTypeGestion) throws SQLException {
        String sql = "insert into article(nom, id_type_gestion) values(?, ?)";
        Connection con = ConnexionPostgres.ouvrir();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, nom);
        ps.setInt(2, idTypeGestion);
        ps.executeUpdate();
        ps.close();
        con.close();
    }

    public void modifierArticle(int idArticle, String nom, int idTypeGestion) throws SQLException {
        String sql = "update article set nom = ?, id_type_gestion = ? where id_article = ?";
        Connection con = ConnexionPostgres.ouvrir();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, nom);
        ps.setInt(2, idTypeGestion);
        ps.setInt(3, idArticle);
        ps.executeUpdate();
        ps.close();
        con.close();
    }

    public void supprimerArticle(int idArticle) throws SQLException {
        Connection con = ConnexionPostgres.ouvrir();

        try {
            con.setAutoCommit(false);

            PreparedStatement psMouvement = con.prepareStatement("delete from mouvement_stock where id_article = ?");
            psMouvement.setInt(1, idArticle);
            psMouvement.executeUpdate();
            psMouvement.close();

            PreparedStatement psArticle = con.prepareStatement("delete from article where id_article = ?");
            psArticle.setInt(1, idArticle);
            int articlesSupprimes = psArticle.executeUpdate();
            psArticle.close();

            if (articlesSupprimes == 0) {
                throw new SQLException("Article introuvable pour la suppression.");
            }

            con.commit();

        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            con.close();
        }
    }

    public void ajouterMouvementStock(int idArticle, String typeMouvement, BigDecimal nombre, BigDecimal pu,
            Timestamp dateMouvement, String idSource)
            throws SQLException {
        Connection con = ConnexionPostgres.ouvrir();

        try {
            insererMouvement(con, idArticle, typeMouvement, nombre, pu, dateMouvement, idSource);
        } finally {
            con.close();
        }
    }

    public void ajouterMouvementsStock(int idArticle, List<MouvementStock> mouvements) throws SQLException {
        Connection con = ConnexionPostgres.ouvrir();

        try {
            con.setAutoCommit(false);

            for (MouvementStock mouvement : mouvements) {
                insererMouvement(
                        con,
                        idArticle,
                        mouvement.getTypeMouvement(),
                        mouvement.getNombre(),
                        mouvement.getPrixUnitaire(),
                        mouvement.getDateMouvement(),
                        mouvement.getIdSource());
            }

            con.commit();

        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            con.close();
        }
    }

    public void supprimerMouvementStock(int idMouvementStock) throws SQLException {
        String sql = "delete from mouvement_stock where id_mouvement_stock = ?";
        Connection con = ConnexionPostgres.ouvrir();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idMouvementStock);
        ps.executeUpdate();
        ps.close();
        con.close();
    }

    public List<MouvementStock> listerDetails(int idArticle, Date dateDebut, Date dateFin) throws SQLException {
        List<MouvementStock> mouvements = new ArrayList<MouvementStock>();
        String sql = ""
                + "select id_mouvement_stock, type_mouvement, nombre, pu, date_mouvement, id_source "
                + "from mouvement_stock where id_article = ? "
                + "and (cast(? as date) is null or date_mouvement::date >= cast(? as date)) "
                + "and (cast(? as date) is null or date_mouvement::date <= cast(? as date)) "
                + "order by date_mouvement";

        Connection con = ConnexionPostgres.ouvrir();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idArticle);
        ps.setDate(2, dateDebut);
        ps.setDate(3, dateDebut);
        ps.setDate(4, dateFin);
        ps.setDate(5, dateFin);
        ResultSet rs = ps.executeQuery();

        BigDecimal quantiteCumulee = BigDecimal.ZERO;
        BigDecimal valeurCumulee = BigDecimal.ZERO;
        List<MouvementStock> lignes = new ArrayList<MouvementStock>();

        while (rs.next()) {
            BigDecimal nombre = rs.getBigDecimal("nombre");
            BigDecimal pu = rs.getBigDecimal("pu");
            String typeMouvement = rs.getString("type_mouvement");
            BigDecimal coutTotal = nombre.multiply(pu).setScale(2, RoundingMode.HALF_UP);
            BigDecimal cump = BigDecimal.ZERO;

            if ("ENTREE".equals(typeMouvement)) {
                quantiteCumulee = quantiteCumulee.add(nombre);
                valeurCumulee = valeurCumulee.add(nombre.multiply(pu));
            } else {
                quantiteCumulee = quantiteCumulee.subtract(nombre);
                valeurCumulee = valeurCumulee.subtract(coutTotal);
            }

            if (quantiteCumulee.compareTo(BigDecimal.ZERO) > 0) {
                cump = valeurCumulee.divide(quantiteCumulee, 2, RoundingMode.HALF_UP);
            }

            lignes.add(new MouvementStock(
                    rs.getInt("id_mouvement_stock"),
                    typeMouvement,
                    nombre,
                    pu,
                    rs.getTimestamp("date_mouvement"),
                    rs.getString("id_source"),
                    coutTotal,
                    cump));
        }

        for (MouvementStock mouvement : lignes) {
            mouvements.add(new MouvementStock(
                    mouvement.getIdMouvementStock(),
                    mouvement.getTypeMouvement(),
                    mouvement.getNombre(),
                    mouvement.getPrixUnitaire(),
                    mouvement.getDateMouvement(),
                    mouvement.getIdSource(),
                    mouvement.getCoutTotal(),
                    mouvement.getCoutUnitaireMoyenPondere()));
        }

        rs.close();
        ps.close();
        con.close();
        return mouvements;
    }

    private void insererMouvement(Connection con, int idArticle, String typeMouvement,
            BigDecimal nombre, BigDecimal pu, Timestamp dateMouvement, String idSource) throws SQLException {

        String sql = ""
                + "insert into mouvement_stock(id_article, type_mouvement, nombre, pu, date_mouvement, id_source) "
                + "values(?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idArticle);
        ps.setString(2, typeMouvement);
        ps.setBigDecimal(3, nombre);
        ps.setBigDecimal(4, pu);
        ps.setTimestamp(5, dateMouvement);
        ps.setString(6, idSource);
        ps.executeUpdate();
        ps.close();
    }

    private BigDecimal calculerCump(Connection con, int idArticle) throws SQLException {
        String sql = ""
                + "select "
                + "coalesce(sum(case when type_mouvement = 'ENTREE' then nombre else -nombre end), 0) as qte, "
                + "coalesce(sum(case when type_mouvement = 'ENTREE' then nombre * pu else -nombre * pu end), 0) as valeur "
                + "from mouvement_stock "
                + "where id_article = ?";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idArticle);
        ResultSet rs = ps.executeQuery();

        BigDecimal cump = BigDecimal.ZERO;

        if (rs.next()) {
            BigDecimal qte = rs.getBigDecimal("qte");
            BigDecimal valeur = rs.getBigDecimal("valeur");

            if (qte.compareTo(BigDecimal.ZERO) > 0) {
                cump = valeur.divide(qte, 2, RoundingMode.HALF_UP);
            }
        }

        rs.close();
        ps.close();

        return cump;
    }

    private BigDecimal calculerDernierCump(Connection con, int idArticle, Date dateDebut, Date dateFin)
            throws SQLException {
        String sql = ""
                + "select type_mouvement, nombre, pu "
                + "from mouvement_stock "
                + "where id_article = ? "
                + "and (cast(? as date) is null or date_mouvement::date >= cast(? as date)) "
                + "and (cast(? as date) is null or date_mouvement::date <= cast(? as date)) "
                + "order by date_mouvement, id_mouvement_stock";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idArticle);
        ps.setDate(2, dateDebut);
        ps.setDate(3, dateDebut);
        ps.setDate(4, dateFin);
        ps.setDate(5, dateFin);
        ResultSet rs = ps.executeQuery();

        BigDecimal quantiteCumulee = BigDecimal.ZERO;
        BigDecimal valeurCumulee = BigDecimal.ZERO;
        BigDecimal dernierCump = BigDecimal.ZERO;

        while (rs.next()) {
            BigDecimal nombre = rs.getBigDecimal("nombre");
            BigDecimal pu = rs.getBigDecimal("pu");
            BigDecimal coutTotal = nombre.multiply(pu);

            if ("ENTREE".equals(rs.getString("type_mouvement"))) {
                quantiteCumulee = quantiteCumulee.add(nombre);
                valeurCumulee = valeurCumulee.add(coutTotal);
            } else {
                quantiteCumulee = quantiteCumulee.subtract(nombre);
                valeurCumulee = valeurCumulee.subtract(coutTotal);
            }

            if (quantiteCumulee.compareTo(BigDecimal.ZERO) > 0) {
                dernierCump = valeurCumulee.divide(quantiteCumulee, 2, RoundingMode.HALF_UP);
            }
        }

        rs.close();
        ps.close();
        return dernierCump;
    }

    public void ajouterSortieStock(int idArticle, String modeGestion, BigDecimal quantiteDemandee,
            Timestamp dateMouvement)
            throws SQLException {

        Connection con = ConnexionPostgres.ouvrir();

        try {
            con.setAutoCommit(false);
            ajouterSortieStock(con, idArticle, modeGestion, quantiteDemandee, dateMouvement);

            con.commit();

        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            con.close();
        }
    }

    public void ajouterSortiesStock(int idArticle, String modeGestion, List<MouvementStock> mouvements)
            throws SQLException {

        Connection con = ConnexionPostgres.ouvrir();

        try {
            con.setAutoCommit(false);

            for (MouvementStock mouvement : mouvements) {
                ajouterSortieStock(con, idArticle, modeGestion, mouvement.getNombre(), mouvement.getDateMouvement());
            }

            con.commit();

        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            con.close();
        }
    }

    private void ajouterSortieStock(Connection con, int idArticle, String modeGestion, BigDecimal quantiteDemandee,
            Timestamp dateMouvement)
            throws SQLException {

        BigDecimal reste = quantiteDemandee;

        String orderBy = "FIFO".equalsIgnoreCase(modeGestion)
                ? "order by e.date_mouvement asc, e.id_mouvement_stock asc"
                : "order by e.date_mouvement desc, e.id_mouvement_stock desc";

        if ("CUMP".equalsIgnoreCase(modeGestion)) {
            BigDecimal cump = calculerCump(con, idArticle);

            insererMouvement(con, idArticle, "SORTIE", quantiteDemandee, cump, dateMouvement, "CUMP");
            return;
        }

        String sql = ""
                + "select e.id_mouvement_stock, e.nombre, e.pu, "
                + "e.nombre - coalesce(sum(s.nombre), 0) as reste_disponible "
                + "from mouvement_stock e "
                + "left join mouvement_stock s on s.id_source = cast(e.id_mouvement_stock as varchar) "
                + "and s.type_mouvement = 'SORTIE' "
                + "where e.id_article = ? "
                + "and e.type_mouvement = 'ENTREE' "
                + "group by e.id_mouvement_stock, e.nombre, e.pu, e.date_mouvement "
                + "having e.nombre - coalesce(sum(s.nombre), 0) > 0 "
                + orderBy;

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idArticle);
        ResultSet rs = ps.executeQuery();

        while (rs.next() && reste.compareTo(BigDecimal.ZERO) > 0) {
            int idEntree = rs.getInt("id_mouvement_stock");
            BigDecimal disponible = rs.getBigDecimal("reste_disponible");
            BigDecimal pu = rs.getBigDecimal("pu");

            BigDecimal quantiteSortie = reste.min(disponible);

            insererMouvement(
                    con,
                    idArticle,
                    "SORTIE",
                    quantiteSortie,
                    pu,
                    dateMouvement,
                    String.valueOf(idEntree));

            reste = reste.subtract(quantiteSortie);
        }

        rs.close();
        ps.close();

        if (reste.compareTo(BigDecimal.ZERO) > 0) {
            throw new SQLException("Stock insuffisant. Quantite manquante : " + reste);
        }
    }
}
