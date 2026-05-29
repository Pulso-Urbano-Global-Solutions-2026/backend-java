-- Seed DDL minimo para testes de integracao
-- Executado apenas no perfil test; schema completo fica no Clayton (database/init.sql)

CREATE SEQUENCE seq_usuario START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_zona    START WITH 1 INCREMENT BY 1;

CREATE TABLE usuario (
  id_usuario           NUMBER DEFAULT seq_usuario.NEXTVAL PRIMARY KEY,
  nome                 VARCHAR2(150)  NOT NULL,
  email                VARCHAR2(200)  NOT NULL UNIQUE,
  hash_senha           VARCHAR2(255)  NOT NULL,
  faz_exercicio        NUMBER(1)      DEFAULT 0,
  tem_crianca          NUMBER(1)      DEFAULT 0,
  tem_problema_resp    NUMBER(1)      DEFAULT 0,
  role                 VARCHAR2(20)   DEFAULT 'USER',
  dt_criacao           DATE           DEFAULT SYSDATE,
  ativo                NUMBER(1)      DEFAULT 1,
  CONSTRAINT chk_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE zona_cidade (
  id_zona    NUMBER DEFAULT seq_zona.NEXTVAL PRIMARY KEY,
  nome       VARCHAR2(100) NOT NULL,
  municipio  VARCHAR2(100) DEFAULT 'Sao Paulo',
  lat        NUMBER(9,6),
  lon        NUMBER(9,6),
  ativo      NUMBER(1) DEFAULT 1
);
