-- =====================================================================
-- Projeto Netflix (POO)
-- O sistema roda este script SOZINHO na primeira execucao (util/CriadorBanco.java)
-- e de novo no botao "Restaurar dados de exemplo". Tambem pode ser aberto no Workbench.
-- Recorte das 13 tabelas do modelo logico em 5 tabelas:
--   plano -> conta -> perfil -> historico_visualizacao <- titulo
-- =====================================================================

DROP DATABASE IF EXISTS netflix;
CREATE DATABASE netflix CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE netflix;

-- ---------------------------------------------------------------------
-- 1) PLANO  (lado "1" da relacao plano -> conta)
-- ---------------------------------------------------------------------
CREATE TABLE plano (
    id_plano          INT AUTO_INCREMENT PRIMARY KEY,
    nome              VARCHAR(40)   NOT NULL,
    descricao         TEXT,
    valor_mensal      DECIMAL(6,2)  NOT NULL,
    qualidade_maxima  ENUM('SD','HD','Full HD','4K UHD') NOT NULL DEFAULT 'HD',
    telas_simultaneas TINYINT       NOT NULL DEFAULT 1,
    permite_download  TINYINT(1)    NOT NULL DEFAULT 0,
    data_criacao      DATE          NOT NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 2) CONTA  (N contas -> 1 plano)
-- ---------------------------------------------------------------------
CREATE TABLE conta (
    id_conta      INT AUTO_INCREMENT PRIMARY KEY,
    id_plano      INT          NOT NULL,
    email         VARCHAR(120) NOT NULL UNIQUE,
    senha_hash    VARCHAR(255) NOT NULL,
    telefone      VARCHAR(15),
    pais          CHAR(2)      NOT NULL DEFAULT 'BR',
    data_cadastro DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status        ENUM('Ativa','Suspensa','Cancelada') NOT NULL DEFAULT 'Ativa',
    CONSTRAINT fk_conta_plano FOREIGN KEY (id_plano) REFERENCES plano (id_plano)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3) PERFIL  (N perfis -> 1 conta)
-- ---------------------------------------------------------------------
CREATE TABLE perfil (
    id_perfil      INT AUTO_INCREMENT PRIMARY KEY,
    id_conta       INT         NOT NULL,
    nome           VARCHAR(50) NOT NULL,
    avatar         VARCHAR(60),
    data_criacao   DATE        NOT NULL,
    perfil_infantil TINYINT(1) NOT NULL DEFAULT 0,
    pin            CHAR(4),
    CONSTRAINT fk_perfil_conta FOREIGN KEY (id_conta) REFERENCES conta (id_conta)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 4) TITULO  (catalogo: filmes, series, documentarios, stand-ups)
-- ---------------------------------------------------------------------
CREATE TABLE titulo (
    id_titulo               INT AUTO_INCREMENT PRIMARY KEY,
    nome                    VARCHAR(150) NOT NULL,
    sinopse                 TEXT,
    tipo                    ENUM('Filme','Serie','Documentario','Stand-up') NOT NULL,
    ano_lancamento          SMALLINT,
    classificacao_indicativa ENUM('L','10','12','14','16','18') NOT NULL DEFAULT 'L',
    duracao_minutos         INT,   -- NULL para series (a duracao fica nos episodios)
    idioma_original         VARCHAR(30),
    data_adicao_catalogo    DATE   NOT NULL,
    disponivel              TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 5) HISTORICO_VISUALIZACAO  (tabela associativa N:N perfil <-> titulo)
--    Cada linha = "o perfil X assistiu o titulo Y, em tal data, ate tal minuto".
--    Se o perfil ou o titulo for excluido, o historico dele some junto
--    (ON DELETE CASCADE), para nao sobrar registro apontando para o nada.
-- ---------------------------------------------------------------------
CREATE TABLE historico_visualizacao (
    id_visualizacao          INT AUTO_INCREMENT PRIMARY KEY,
    id_perfil                INT      NOT NULL,
    id_titulo                INT      NOT NULL,
    data_inicio              DATETIME NOT NULL,
    data_ultima_visualizacao DATETIME NOT NULL,
    minuto_parada            INT      NOT NULL DEFAULT 0,
    concluido                TINYINT(1) NOT NULL DEFAULT 0,
    dispositivo              ENUM('TV','Celular','Notebook','Tablet','Console') NOT NULL DEFAULT 'TV',
    CONSTRAINT fk_hist_perfil FOREIGN KEY (id_perfil) REFERENCES perfil (id_perfil) ON DELETE CASCADE,
    CONSTRAINT fk_hist_titulo FOREIGN KEY (id_titulo) REFERENCES titulo (id_titulo) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- DADOS DE TESTE
-- =====================================================================
INSERT INTO plano (nome, descricao, valor_mensal, qualidade_maxima, telas_simultaneas, permite_download, data_criacao) VALUES
('Basico',  'Uma tela, qualidade SD',          20.90, 'SD',     1, 0, '2024-01-01'),
('Padrao',  'Duas telas, qualidade Full HD',   39.90, 'Full HD',2, 1, '2024-01-01'),
('Premium', 'Quatro telas, qualidade 4K UHD',  55.90, '4K UHD', 4, 1, '2024-01-01');

-- senha_hash: valores ficticios (em producao seria um hash real, ex.: bcrypt)
INSERT INTO conta (id_plano, email, senha_hash, telefone, pais, data_cadastro, status) VALUES
(3, 'ana.silva@email.com',   'hash_ana_123',   '11999990001', 'BR', '2025-02-10 10:00:00', 'Ativa'),
(2, 'bruno.souza@email.com', 'hash_bruno_456', '21988880002', 'BR', '2025-03-15 14:30:00', 'Ativa'),
(1, 'carla.lima@email.com',  'hash_carla_789', NULL,          'BR', '2025-05-20 09:15:00', 'Suspensa');

INSERT INTO perfil (id_conta, nome, avatar, data_criacao, perfil_infantil, pin) VALUES
(1, 'Ana',    'azul',      '2025-02-10', 0, '1234'),
(1, 'Pedro',  'vermelho',  '2025-02-11', 0, NULL),
(1, 'Kids',   'amarelo',   '2025-02-12', 1, NULL),
(2, 'Bruno',  'verde',     '2025-03-15', 0, NULL),
(3, 'Carla',  'amarelo',   '2025-05-20', 0, '4321');

INSERT INTO titulo (nome, sinopse, tipo, ano_lancamento, classificacao_indicativa, duracao_minutos, idioma_original, data_adicao_catalogo, disponivel) VALUES
('Stranger Things',   'Criancas enfrentam forcas sobrenaturais em Hawkins.', 'Serie',        2016, '14', NULL, 'Ingles',    '2024-02-01', 1),
('Ainda Estou Aqui',  'Drama baseado em fatos reais durante a ditadura.',    'Filme',        2024, '14', 137,  'Portugues', '2025-01-10', 1),
('Our Planet',        'A natureza do planeta em imagens espetaculares.',     'Documentario', 2019, 'L',  50,   'Ingles',    '2024-03-05', 1),
('Os Parcas Contam',  'Show de comedia stand-up.',                           'Stand-up',     2023, '16', 65,   'Portugues', '2024-06-12', 1),
('Round 6',           'Competicao mortal com premio bilionario.',            'Serie',        2021, '16', NULL, 'Coreano',   '2024-04-20', 1),
('Se a Vida te Der Tangerinas', 'Drama sobre a vida aventureira de Ae-soon e Gwan-sik, nascidos em Jeju na decada de 1950.', 'Serie', 2025, '12', NULL, 'Coreano', '2025-03-07', 1);

INSERT INTO historico_visualizacao (id_perfil, id_titulo, data_inicio, data_ultima_visualizacao, minuto_parada, concluido, dispositivo) VALUES
(1, 1, '2025-06-01 20:00:00', '2025-06-01 21:00:00',  55, 0, 'TV'),
(1, 2, '2025-06-03 21:00:00', '2025-06-03 23:20:00', 137, 1, 'TV'),
(2, 5, '2025-06-05 19:30:00', '2025-06-05 20:15:00',  45, 0, 'Celular'),
(3, 3, '2025-06-06 16:00:00', '2025-06-06 16:50:00',  50, 1, 'Tablet'),
(4, 4, '2025-06-08 22:00:00', '2025-06-08 23:05:00',  65, 1, 'Notebook'),
(5, 2, '2025-06-10 20:00:00', '2025-06-10 21:10:00',  70, 0, 'TV');

-- Teste rapido do INNER JOIN (o mesmo usado em HistoricoVisualizacaoDAO):
-- SELECT h.id_visualizacao, p.nome AS perfil, t.nome AS titulo, h.minuto_parada, h.dispositivo
-- FROM historico_visualizacao h
-- INNER JOIN perfil p ON p.id_perfil = h.id_perfil
-- INNER JOIN titulo t ON t.id_titulo = h.id_titulo;
