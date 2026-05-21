CREATE DATABASE stock;
\c stock;

drop table if exists mouvement_stock;
drop table if exists article;
drop table if exists type_gestion;

create table type_gestion(
   id_type_gestion serial primary key,
   libeller varchar(50) not null
);

create table article(
   id_article serial primary key,
   nom varchar(100) not null,
   id_type_gestion int not null,
   foreign key(id_type_gestion) references type_gestion(id_type_gestion)
);

create table mouvement_stock(
   id_mouvement_stock serial primary key,
   type_mouvement varchar(10) not null,
   nombre numeric(15,2) not null,
   pu numeric(15,2) not null,
   date_mouvement timestamp not null,
   id_source varchar(50),
   id_article int not null,
   check(type_mouvement in ('ENTREE', 'SORTIE')),
   foreign key(id_article) references article(id_article)
);

insert into type_gestion(libeller) values ('FIFO'), ('LIFO'), ('');
