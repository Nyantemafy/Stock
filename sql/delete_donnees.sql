begin;

delete from mouvement_stock;
delete from article;
delete from type_gestion;

alter sequence if exists mouvement_stock_id_mouvement_stock_seq restart with 1;
alter sequence if exists article_id_article_seq restart with 1;
alter sequence if exists type_gestion_id_type_gestion_seq restart with 1;

commit;
