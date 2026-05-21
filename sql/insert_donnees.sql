begin;

insert into type_gestion(libeller)
values
    ('FIFO'),
    ('LIFO'),
    ('CUMP');

insert into article(nom, id_type_gestion)
select v.nom, tg.id_type_gestion
from (
    values
        ('Biscuit', 'FIFO'),
        ('Bonbon', 'LIFO'),
        ('Soda', 'CUMP')
) as v(nom, methode)
join type_gestion tg on tg.libeller = v.methode;

insert into mouvement_stock(type_mouvement, nombre, pu, date_mouvement, id_article)
select v.type_mouvement, v.nombre, v.pu, v.date_mouvement::timestamp, a.id_article
from (
    values
        ('Biscuit', '2026-05-01', 'ENTREE', 100, 500),
        ('Biscuit', '2026-05-03', 'ENTREE', 80, 550),
        ('Biscuit', '2026-05-05', 'ENTREE', 60, 600),
        ('Bonbon', '2026-05-01', 'ENTREE', 200, 50),
        ('Bonbon', '2026-05-03', 'ENTREE', 150, 55),
        ('Bonbon', '2026-05-05', 'ENTREE', 100, 60),
        ('Soda', '2026-05-01', 'ENTREE', 50, 1200),
        ('Soda', '2026-05-03', 'ENTREE', 40, 1250)
) as v(nom_article, date_mouvement, type_mouvement, nombre, pu)
join article a on a.nom = v.nom_article;

commit;
