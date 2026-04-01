create database EasyFactura;
use EasyFactura;

create table empresa(
id_empresa int auto_increment primary key,
nombre varchar(50) not null,
cif varchar(20) unique not null,
email varchar(50) unique not null,
password varchar(30) not null,
direccion text,
telefono int);


create table cliente(
id_cliente int auto_increment primary key,
id_empresa int not null,
nombre varchar(100) not null,
nif varchar(20) not null,
email varchar(50) not null,
direccion text,
telefono int,
foreign key (id_empresa) references empresa (id_empresa));

create table producto(
id_producto int auto_increment primary key,
id_empresa int not null,
nombre varchar(100) not null,
descripcion text,
precio decimal(10,2) not null,
iva int not null,
foreign key (id_empresa) references empresa (id_empresa));


create table factura(
id_factura int auto_increment primary key,
id_empresa int not null,
id_cliente int not null,
fecha timestamp default current_timestamp,
estado enum("activa","rectificada") default "activa",
hash_verifactu varchar(150) not null,
foreign key (id_empresa) references empresa (id_empresa),
foreign key (id_cliente) references cliente (id_cliente));


create table factura_detalle(
id_detalle int auto_increment primary key,
id_factura int not null,
id_producto int not null,
cantidad int not null,
precio_unitario decimal(10,2) not null,
iva int not null,
total decimal(10,2) not null,
foreign key (id_factura) references factura (id_factura),
foreign key (id_producto) references producto (id_producto));


create table factura_rectificada(
id_rectificacion int auto_increment primary key,
id_factura_original int not null,
id_factura_rectificada int not null,
motivo text,
fecha timestamp default current_timestamp,
foreign key (id_factura_original) references factura (id_factura),
foreign key (id_factura_rectificada) references factura (id_factura));



create index idx_factura_empresa on factura(id_empresa);
create index idx_cliente_empresa on cliente(id_empresa);

delimiter $$ 
create trigger bloquear_delete_factura 
before delete on factura 
for each row 
begin
	signal sqlstate "45000" 
    set message_text = "No se pueden eliminar facturas por el control de verifactu";
end$$
delimiter ;













