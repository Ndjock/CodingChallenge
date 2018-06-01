--liquibase formatted sql

--changeset arthur:ddl-create-tables
create table contacts(
 id INTEGER NOT NULL AUTO_INCREMENT,
 firstname varchar(30),
 lastname varchar(30),
 fullname varchar(30),
 email varchar(50),
 address_line varchar(100),
 mobile_phone_number varchar (50),
 PRIMARY KEY(id)
);

create table skills(
  id INTEGER NOT NULL AUTO_INCREMENT,
  name varchar(50),
  level varchar(50),
  primary key (id)
);

create table contacts_with_skills(
  id INTEGER NOT NULL AUTO_INCREMENT,
  contact_id INTEGER NOT NULL,
  skill_id INTEGER NOT NULL,
  primary key (id),
  foreign key (contact_id) references contacts(id),
  foreign key (skill_id) references skills(id)
);
