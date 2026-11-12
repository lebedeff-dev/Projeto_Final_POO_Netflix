# Documentação do Projeto Netflix (POO) – versão principal

> Versão 2 da documentação. Mesmo estilo da primeira versão (cada classe explicada linha a linha), atualizada com o que mudou:
> **build pronta (`Netflix.jar`)**, **tela de conexão**, **banco criado sozinho**, **relatório da rotina principal** e **código mais simples de explicar**.

## Sumário

0. [O que mudou da primeira versão para a versão principal](#0-o-que-mudou-da-primeira-versão-para-a-versão-principal)
1. [Visão geral](#1-visão-geral)
2. [Banco de dados (`sql/netflix.sql`)](#2-banco-de-dados-sqlnetflixsql)
3. [Pacote `util`](#3-pacote-util)
4. [Pacote `model`](#4-pacote-model-entidades)
5. [Pacote `dao`](#5-pacote-dao)
6. [Pacote `controller`](#6-pacote-controller)
7. [Pacote `view` e o `Main`](#7-pacote-view-telas-swing-e-o-main)
8. [Integração Java ↔ MySQL](#8-integração-java--mysql-o-que-o-professor-mais-cobra)
9. [Conceitos de POO no projeto](#9-conceitos-de-poo-dos-slides-do-professor-aplicados-ao-projeto)
10. [Como gerar a build](#10-como-gerar-a-build-netflixjar)
11. [Passo a passo para rodar e testar](#11-passo-a-passo-para-rodar-e-testar)
12. [Perguntas prováveis do professor](#12-perguntas-prováveis-do-professor-com-respostas)

---

## 0. O que mudou da primeira versão para a versão principal

| Problema na primeira versão | Como a versão principal resolve | Onde no código |
|---|---|---|
| **Faltava o relatório** da rotina principal (a proposta pede JOIN **e** relatório; a primeira versão só tinha a tabela do JOIN) | Nova tela **Relatório: visualizações por período**, com filtro de datas e de perfil, totais (concluídas, em andamento, minutos) e botão **Salvar .txt** | `TelaRelatorio`, `RelatorioController`, `HistoricoVisualizacaoDAO.listarPorPeriodo()` |
| Não tinha **build**: cada um precisava compilar | `Netflix.jar` pronto (com o driver do MySQL e o script SQL dentro). Dois cliques e abre | `Netflix.jar`, `Netflix/build.bat` |
| Tinha que abrir o **Workbench** e rodar o script na mão | O sistema cria o banco **sozinho** na primeira vez. O menu tem **Restaurar dados de exemplo** | `util/CriadorBanco` |
| A senha do MySQL ficava **escrita no código** (editar e recompilar) | **Tela de conexão** ao abrir; os dados ficam salvos em `config.properties` | `TelaConexao`, `ConexaoController`, `ConnectionFactory` |
| Contas e Perfis mostravam **só o número da FK** ("Plano (id) = 3"), o que a proposta pede para evitar | As tabelas mostram **"3 - Premium"** e **"1 - ana.silva@email.com"** (INNER JOIN em `ContaDAO` e `PerfilDAO`) | `ContaDAO.listar`, `PerfilDAO.listar` |
| Código difícil de explicar: `Supplier<String>`, `this::inserir`, `TelaPlano::new`, colunas escondidas no Histórico | Botões chamam o método direto (`e -> mostrarResultado(inserir())`); colunas escondidas eliminadas porque tabela e combo usam o mesmo texto `"id - nome"` | `TelaCrud`, `TelaHistorico`, `TelaPrincipal` |
| Combos de FK **não atualizavam** (cadastrar um plano e ele não aparecer na tela de Conta) | Combos recarregam junto com a tabela, depois de cada ação (`carregarCombos()`) | `TelaCrud.recarregar()` |
| `ConnectionFactory` lançava `RuntimeException` | Lança `SQLException`. Assim o erro "MySQL desligado" cai no mesmo `catch (SQLException)` dos controllers | `ConnectionFactory.getConnection()` |

O **modelo do banco é o mesmo da primeira versão** (5 tabelas, mesmas colunas e dados de exemplo).

---

## 1. Visão geral

O programa é um sistema desktop (Java Swing + MySQL) que gerencia um recorte do banco da Netflix: **planos, contas, perfis, títulos** e o **histórico do que cada perfil assistiu**. Cada tela permite **inserir, listar, atualizar e eliminar** registros. A tela de Histórico é o **CRUD principal** e mostra os **nomes** do perfil e do título (INNER JOIN) em vez dos IDs. A tela de **Relatório** filtra as visualizações por período e perfil e calcula totais.

### 1.1 Tecnologias

- **Java 11+** (compilado com `--release 11`) com orientação a objetos (classes, herança, encapsulamento, polimorfismo).
- **Java Swing** para as telas (`JFrame`, `JTable`, `DefaultTableModel`, `JComboBox`, `JTextField`, `JPasswordField`, `JFileChooser`).
- **JDBC** com `PreparedStatement` para falar com o banco.
- **MySQL** com o driver `mysql-connector-j-9.3.0.jar` (vai **dentro** do `Netflix.jar`).

### 1.2 Arquitetura MVC

Cada camada só conversa com a vizinha:

```
View  -->  Controller  -->  DAO  -->  MySQL
(tela)     (regras)        (SQL)     (dados)
               |
             Model  (objetos que carregam os dados entre as camadas)
```

| Pacote | Papel | O que NÃO pode ter |
|---|---|---|
| `model` | Entidades: guardam dados (atributos, getters e setters) | SQL, telas, regras |
| `dao` | Data Access Object: **único lugar com SQL** (`PreparedStatement`) | telas, regras de negócio |
| `controller` | Valida, converte textos, aplica regras de negócio, chama o DAO | telas, SQL |
| `view` | Telas Swing: lê campos, chama o Controller, mostra mensagens e tabelas | SQL, regras de negócio |
| `util` | Apoio: conexão, criação do banco, conversão de textos | regras específicas de uma tela |

### 1.3 O caminho de uma ação (exemplo: inserir um plano)

1. O usuário preenche os campos da `TelaPlano` e clica em **Inserir**.
2. O botão faz `mostrarResultado(inserir())` (na `TelaCrud`). Por polimorfismo, `inserir()` executa a versão da `TelaPlano`.
3. `TelaPlano.inserir()` lê os campos como `String` e chama `PlanoController.inserir(...)`.
4. O Controller valida e converte (via `Conversor`), cria um objeto `Plano` e chama `PlanoDAO.inserir(plano)`.
5. O DAO abre a conexão (`ConnectionFactory`), prepara o `INSERT` com `PreparedStatement` e executa.
6. A mensagem de sucesso (ou erro) volta pelo mesmo caminho; `mostrarResultado` mostra um `JOptionPane` e recarrega combos e tabela; o formulário só é limpo quando deu certo (se deu erro, o usuário mantém o que digitou e corrige só o campo errado).

### 1.4 Estrutura de pastas

```
Projeto_Final_POO_Netflix/
  Netflix.jar                  (BUILD PRONTA: dois cliques para abrir)
  Abrir Netflix.bat            (alternativa: abre o jar e mostra erros, se houver)
  DOCUMENTACAO.md              (este arquivo)
  Netflix/                     (código-fonte)
    build.bat / build.sh       (geram o Netflix.jar de novo)
    lib/mysql-connector-j-9.3.0.jar  (driver JDBC do MySQL)
    sql/netflix.sql            (cria o banco, as tabelas e os dados de teste)
    src/
      Main.java                (ponto de entrada: abre a TelaConexao)
      model/       Plano, Conta, Perfil, Titulo, HistoricoVisualizacao
      dao/         PlanoDAO, ContaDAO, PerfilDAO, TituloDAO, HistoricoVisualizacaoDAO
      controller/  PlanoController, ContaController, PerfilController, TituloController,
                   HistoricoController, RelatorioController, ConexaoController
      view/        TelaConexao, TelaPrincipal, BarraNavegacao, Navegador, TelaCrud, TelaPlano, TelaConta, TelaPerfil,
                   TelaTitulo, TelaHistorico, TelaRelatorio
      util/        ConnectionFactory, CriadorBanco, Conversor
  Banco de dados/              (modelo lógico do grupo: .mwb)
  Proposta/                    (enunciado do trabalho)
  Fontes/                      (aulas e exemplos do professor)
```

### 1.5 Escopo cumprido (checklist da proposta)

| Exigência | Onde está |
|---|---|
| **4 CRUDs simples** | Plano, Conta, Perfil, Título (telas 1 a 4 do menu) |
| **1 CRUD principal** (com FKs) | `historico_visualizacao`: liga Perfil e Título (tela 5) |
| **Consulta com JOIN** | `HistoricoVisualizacaoDAO.listarComNomes()` (5 tabelas: histórico, perfil, título, conta e plano). Também em `ContaDAO.listar()` e `PerfilDAO.listar()` |
| **Relatório da rotina principal** | Tela 6: `TelaRelatorio` → `RelatorioController` → `HistoricoVisualizacaoDAO.listarPorPeriodo()` |
| POO + MVC | pacotes `model`/`dao`/`controller`/`view`; herança `TelaCrud` (seção 9) |
| MySQL: banco, tabelas, PK, FK, dados, script | `sql/netflix.sql` (seção 2) |
| Fluxo completo: Cadastro → Relacionamento → Rotina Principal → JOIN → Relatório | menu de cima para baixo: 1 → 2 → 3 → 4 → 5 → 6 |

**Convenção de nomes:** atributos em `camelCase` no Java, colunas em `snake_case` no banco; a conversão é feita nos DAOs.

> **Como ler as próximas seções:** cada classe aparece com seus trechos e cada linha (ou grupo de linhas idênticas, como getters e setters) vem com a explicação do que faz e **por quê**. Linhas só com `}` fecham o bloco aberto acima e não são comentadas.

---

## 2. Banco de dados (`sql/netflix.sql`)

O script cria o banco `netflix`, 5 tabelas (um recorte das 13 do modelo lógico do grupo) e dados de teste. **Na versão principal ninguém precisa rodá-lo na mão:** o sistema o executa sozinho na primeira vez (seção 3.2). Ele continua podendo ser aberto no Workbench.

### 2.1 Como as tabelas se relacionam

```
plano 1 ---< N conta 1 ---< N perfil N >--- historico_visualizacao ---< N titulo
```

- **plano → conta (1:N):** um plano tem várias contas; cada conta tem um plano (FK `id_plano`).
- **conta → perfil (1:N):** uma conta tem vários perfis (FK `id_conta`).
- **perfil ↔ titulo (N:N):** um perfil assiste vários títulos e um título é assistido por vários perfis. Uma relação N:N não cabe em duas tabelas; ela vira uma **tabela associativa** (`historico_visualizacao`) com duas FKs.

### 2.2 Cabeçalho e criação do banco

- `-- ...`: linhas que começam com `--` são comentários; o MySQL as ignora.
- `DROP DATABASE IF EXISTS netflix;`: apaga o banco se ele já existir. O `IF EXISTS` evita erro quando não existe. Por isso o script pode ser rodado várias vezes (é o que o botão **Restaurar dados de exemplo** faz).
- `CREATE DATABASE netflix CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`: cria o banco. `utf8mb4` aceita acentos e emojis; `utf8mb4_unicode_ci` define como comparar textos (`ci` = ignora maiúsculas/minúsculas).
- `USE netflix;`: os próximos comandos valem para o banco `netflix`.

### 2.3 Tabela `plano`

- `id_plano INT AUTO_INCREMENT PRIMARY KEY`: número que identifica cada plano. `AUTO_INCREMENT` gera 1, 2, 3... sozinho. **PRIMARY KEY** = identificador único, não repete e não é nulo.
- `nome VARCHAR(40) NOT NULL`: texto de até 40 caracteres, obrigatório.
- `descricao TEXT`: texto longo, opcional (aceita NULL).
- `valor_mensal DECIMAL(6,2) NOT NULL`: até 6 dígitos, 2 depois da vírgula (até 9999,99). DECIMAL, e não FLOAT, para dinheiro: não tem erro de arredondamento.
- `qualidade_maxima ENUM('SD','HD','Full HD','4K UHD') NOT NULL DEFAULT 'HD'`: só aceita um dos valores listados; padrão `'HD'`.
- `telas_simultaneas TINYINT NOT NULL DEFAULT 1`: inteiro pequeno.
- `permite_download TINYINT(1) NOT NULL DEFAULT 0`: o MySQL não tem booleano; `TINYINT(1)` guarda 0 (falso) ou 1 (verdadeiro).
- `data_criacao DATE NOT NULL`: só a data. O Java não preenche: o INSERT usa `CURDATE()`.
- `) ENGINE=InnoDB;`: InnoDB é o motor que **suporta chaves estrangeiras** e transações.

### 2.4 Tabela `conta`

- `id_conta INT AUTO_INCREMENT PRIMARY KEY`: chave primária.
- `id_plano INT NOT NULL`: o plano da conta (FK, definida no final).
- `email VARCHAR(120) NOT NULL UNIQUE`: `UNIQUE` impede duas contas com o mesmo e-mail.
- `senha_hash VARCHAR(255) NOT NULL`: guarda a senha "embaralhada" (hash). Os dados de teste são fictícios.
- `telefone VARCHAR(15)`: opcional.
- `pais CHAR(2) NOT NULL DEFAULT 'BR'`: tamanho fixo de 2 letras.
- `data_cadastro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP`: data **e hora**; se não informarem, usa o momento atual.
- `status ENUM('Ativa','Suspensa','Cancelada') NOT NULL DEFAULT 'Ativa'`.
- `CONSTRAINT fk_conta_plano FOREIGN KEY (id_plano) REFERENCES plano (id_plano)`: **chave estrangeira**. O valor de `conta.id_plano` precisa existir em `plano.id_plano`. Resultado: não dá para criar conta com plano inexistente nem apagar um plano que ainda tem contas (**integridade referencial**).

### 2.5 Tabela `perfil`

- `id_perfil` (PK), `id_conta` (FK), `nome VARCHAR(50) NOT NULL`, `avatar VARCHAR(60)` (opcional), `data_criacao DATE NOT NULL`, `perfil_infantil TINYINT(1) DEFAULT 0`, `pin CHAR(4)` (opcional).
- `CONSTRAINT fk_perfil_conta FOREIGN KEY (id_conta) REFERENCES conta (id_conta)`: o perfil só existe se a conta existir; não dá para apagar uma conta que ainda tem perfis.

### 2.6 Tabela `titulo`

- `id_titulo` (PK), `nome VARCHAR(150) NOT NULL`, `sinopse TEXT`, `tipo ENUM('Filme','Serie','Documentario','Stand-up') NOT NULL`, `ano_lancamento SMALLINT`, `classificacao_indicativa ENUM('L','10','12','14','16','18') DEFAULT 'L'`.
- `duracao_minutos INT`: **NULL para séries** (a duração fica nos episódios).
- `idioma_original VARCHAR(30)`, `data_adicao_catalogo DATE NOT NULL`, `disponivel TINYINT(1) DEFAULT 1`.

### 2.7 Tabela `historico_visualizacao` (a tabela associativa e o CRUD principal)

- `id_visualizacao INT AUTO_INCREMENT PRIMARY KEY`: identifica cada "sessão assistida".
- `id_perfil INT NOT NULL` e `id_titulo INT NOT NULL`: as duas FKs. Cada linha diz "o perfil X assistiu o título Y".
- `data_inicio DATETIME`, `data_ultima_visualizacao DATETIME`, `minuto_parada INT DEFAULT 0`, `concluido TINYINT(1) DEFAULT 0`, `dispositivo ENUM('TV','Celular','Notebook','Tablet','Console') DEFAULT 'TV'`.
- `CONSTRAINT fk_hist_perfil FOREIGN KEY (id_perfil) REFERENCES perfil (id_perfil) ON DELETE CASCADE`: se o perfil for apagado, o histórico dele é apagado junto (não sobra registro apontando para o nada).
- `CONSTRAINT fk_hist_titulo ... ON DELETE CASCADE`: o mesmo para o título.

### 2.8 Dados de teste (`INSERT`)

Sintaxe: `INSERT INTO tabela (colunas...) VALUES (valores...), (valores...);`. As colunas `id_*` ficam de fora porque o `AUTO_INCREMENT` as gera.

- **plano:** 3 linhas (Basico 20,90 SD; Padrao 39,90 Full HD; Premium 55,90 4K UHD) → ids 1, 2, 3.
- **conta:** 3 contas; o primeiro número é o `id_plano` (3, 2, 1). A terceira tem telefone `NULL` e status 'Suspensa'.
- **perfil:** 5 perfis; o primeiro número é o `id_conta` (1, 1, 1, 2, 3): a conta 1 tem Ana, Pedro e Kids.
- **titulo:** 5 títulos; as séries (Stranger Things, Round 6) têm `NULL` na duração.
- **historico_visualizacao:** 6 linhas entre 01/06/2025 e 10/06/2025 (ex.: perfil 1 assistiu o título 1 até o minuto 55 na TV).

### 2.9 Consulta de teste (comentada no final)

```sql
SELECT h.id_visualizacao, p.nome AS perfil, t.nome AS titulo, h.minuto_parada, h.dispositivo
FROM historico_visualizacao h
INNER JOIN perfil p ON p.id_perfil = h.id_perfil
INNER JOIN titulo t ON t.id_titulo = h.id_titulo;
```

- `SELECT ...`: colunas a mostrar; `p.nome AS perfil` renomeia a coluna na saída.
- `FROM historico_visualizacao h`: tabela principal; `h` é um apelido (alias).
- `INNER JOIN perfil p ON p.id_perfil = h.id_perfil`: junta cada linha do histórico com o perfil de mesmo id. Assim aparece o **nome**, não o número.
- `INNER JOIN titulo t ON t.id_titulo = h.id_titulo`: o mesmo para o título.

Está comentada (`--`) para não rodar sozinha; copie e execute no Workbench para conferir.

---

## 3. Pacote `util`

Classes de apoio usadas por várias camadas.

### 3.1 `ConnectionFactory` (mudou na versão principal)

**Para que serve:** é a "fábrica" de conexões JDBC. Os dados de acesso (servidor, porta, usuário, senha) ficam em **um único lugar**. Os DAOs só chamam `ConnectionFactory.getConnection()`. **Na versão principal esses dados não ficam mais fixos no código:** vêm da `TelaConexao` e são salvos no `config.properties`.

**Linha a linha**

- `import java.io.FileInputStream / FileOutputStream / IOException`: ler e gravar o arquivo de configuração.
- `import java.sql.Connection / DriverManager / SQLException`: `Connection` é a conexão aberta; `DriverManager` sabe abrir conexões a partir de uma URL; `SQLException` é o erro de banco.
- `import java.util.Properties`: classe do Java que lê/grava arquivos no formato `chave=valor`.
- `private static final String BANCO = "netflix";`: nome do banco; constante (`final`), da classe (`static`), só esta classe lê (`private`).
- `private static final String ARQUIVO_CONFIG = "config.properties";`: nome do arquivo de configuração, criado ao lado do `Netflix.jar`.
- `private static String servidor = "localhost"; porta = 3306; usuario = "root"; senha = "";`: valores **padrão**. Não são `final` porque mudam quando o usuário conecta.
- `private ConnectionFactory() { }`: construtor **privado**: a classe só tem métodos estáticos, ninguém precisa dar `new`.
- `public static Connection getConnection() throws SQLException`: abre a conexão com o banco `netflix`. `DriverManager.getConnection(montarUrl(BANCO), usuario, senha)` procura o driver que entende a URL e conecta. Se o MySQL estiver desligado ou a senha errada, lança `SQLException`, **que o DAO repassa e o Controller captura** (na primeira versão virava `RuntimeException`; agora o caminho do erro é o mesmo de qualquer erro de banco).
- `getConnectionServidor()`: conecta ao **servidor** sem escolher banco (URL termina em `/`). É usada para criar o banco `netflix` quando ele ainda não existe.
- `montarUrl(String banco)`: monta `jdbc:mysql://servidor:porta/banco?useSSL=false&allowPublicKeyRetrieval=true&connectionTimeZone=LOCAL&forceConnectionTimeZoneToSession=false`. `useSSL=false` desliga a criptografia (desnecessária localmente), `allowPublicKeyRetrieval=true` evita um erro comum de login no MySQL 8, `connectionTimeZone=LOCAL` faz o driver usar o fuso do próprio computador, então o horário digitado é sempre o horário gravado e mostrado (com o fuso fixo em São Paulo, um computador em outro fuso via 20:00 como 23:00).
- `configurar(...)`: troca os quatro valores (chamado pelo `ConexaoController`).
- `carregarConfiguracao()`: `Properties config = new Properties();` cria o "dicionário"; `try (FileInputStream arquivo = new FileInputStream(ARQUIVO_CONFIG))` abre o arquivo (try-with-resources fecha sozinho); `config.load(arquivo)` lê as linhas `chave=valor`; `config.getProperty("servidor", servidor)` devolve o valor salvo ou, se não houver, o padrão. O `catch (IOException | NumberFormatException e)` (multi-catch) ignora a falta do arquivo na primeira execução.
- `salvarConfiguracao()`: o inverso: `setProperty` para cada valor e `config.store(arquivo, comentario)` grava o arquivo.
- `getServidor()`, `getPorta()`, `getUsuario()`, `getSenha()`: getters estáticos usados para preencher a tela de conexão.

> **Por que não tem mais `Class.forName("com.mysql.cj.jdbc.Driver")`?** Desde o JDBC 4 o driver se registra sozinho quando o `.jar` está no classpath (arquivo `META-INF/services/java.sql.Driver` dentro do driver). O exemplo do professor usa `Class.forName` porque é o jeito antigo; os dois funcionam.

### 3.2 `CriadorBanco` (novo na versão principal)

**Para que serve:** criar o banco **sozinho**, rodando o `netflix.sql` que vai dentro do `Netflix.jar`. Com isso ninguém do grupo precisa abrir o Workbench.

- `criarSeNaoExistir()`: `if (!bancoExiste() || bancoSemTabelas()) { recriarBanco(); }`. Cria na primeira vez, quando o banco não existe **ou existe vazio** (sem nenhuma tabela). Um banco que já tem tabelas é reaproveitado e nunca é apagado aqui, para não perder dados que alguém cadastrou.
- `bancoSemTabelas()`: roda `SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'netflix'` e devolve `true` se o resultado for 0.
- `bancoExiste()`: roda `SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = 'netflix'`. `information_schema` é o "banco de informações" do próprio MySQL; `schemata` lista os bancos existentes. `rs.next()` vai para a única linha e `rs.getInt(1) > 0` diz se achou.
- `recriarBanco()`:
  - `String script = lerScript();`: texto do script, já sem comentários.
  - `try (Connection con = ConnectionFactory.getConnectionServidor(); Statement st = con.createStatement())`: conexão com o servidor e um `Statement` (sem `?`, pois o script não tem valores vindos do usuário).
  - `for (String comando : script.split(";"))`: o JDBC executa **um comando por vez**; `split(";")` corta o script em cada `;`.
  - `if (!comando.trim().isEmpty()) { st.execute(comando); }`: ignora pedaços vazios e executa os outros em ordem (DROP, CREATE DATABASE, USE, 5 CREATE TABLE, 5 INSERT = 13 comandos).
- `lerScript()`:
  - `CriadorBanco.class.getResourceAsStream("/netflix.sql")`: lê um arquivo **de dentro do jar** (o `build.bat` copia o script para lá).
  - `entrada.transferTo(bytes)` e `bytes.toString("UTF-8")`: transforma o conteúdo em texto, respeitando acentos.
  - O `for` por linha corta tudo depois de `--` (comentários), para um `;` dentro de comentário não atrapalhar o `split`.

### 3.3 `Conversor`

**Para que serve:** a View entrega tudo como texto (`String`). O Controller usa o `Conversor` para transformar em `int`, `BigDecimal`, `LocalDate` etc. e validar. Texto inválido → `IllegalArgumentException` com mensagem em português; o Controller captura e devolve essa mensagem para a tela.

- `DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu")` (com `ResolverStyle.STRICT`, que recusa 31/02): molde da data digitada, no formato brasileiro.
- `DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")`: molde de data com hora para **mostrar** (ex.: `01/06/2025 20:00`). Para trocar a ordem, muda só essa linha.
- `obrigatorio(texto, campo)`: se `texto == null || texto.trim().isEmpty()`, lança "O campo 'X' é obrigatório."; senão devolve `texto.trim()`.
- `vazioParaNulo(texto)`: operador ternário `condição ? seVerdade : seFalso`. Vazio vira `null` (que o DAO grava como NULL).
- `paraInt` / `paraIntOuNulo`: `Integer.parseInt`; o `catch (NumberFormatException e)` troca o erro técnico por "deve ser um número inteiro". `paraIntOuNulo` devolve `Integer` (objeto) porque pode ser `null`.
- `paraDecimal`: `new BigDecimal(texto.replace(',', '.'))`: o usuário digita `29,90` e o `BigDecimal` só entende `29.90`.
- `paraData`: `LocalDate.parse(texto, DATA)` (dd/mm/aaaa). Só o Relatório pede data digitada.
- `paraBoolean(texto)`: `"Sim".equals(texto)`; escrito assim (e não `texto.equals("Sim")`) para não dar `NullPointerException` se `texto` for `null`.
- `simNao(valor)`: boolean → "Sim"/"Nao" para a tabela.
- **`opcao(int id, String nome)` (novo):** devolve `id + " - " + nome` (ex.: `"3 - Premium"`). É usado **nos combos e nas colunas das tabelas**. Como o texto é idêntico, ao clicar numa linha o combo encontra o mesmo item e se posiciona sozinho.
- `extrairId(opcao, campo)`: o inverso: `opcao.split(" - ")[0]` pega o `"3"` e converte para `int` (a FK a gravar). Combo vazio → "Selecione um valor em 'Plano'."
- `paraId(texto)`: id da linha selecionada; vazio → "Selecione um registro na tabela primeiro."
- `formatarDataHora` (mostra data e hora nas tabelas e no .txt) e `hoje()` (data final padrão do Relatório).

---

## 4. Pacote `model` (entidades)

Cada classe representa **uma linha** de uma tabela. Só guarda dados: atributos privados (**encapsulamento**), construtores, getters e setters. Nenhuma tem SQL, tela ou regra de negócio.

### 4.1 Padrões que se repetem (explicados uma vez)

- `package model;`: a classe pertence ao pacote `model`.
- `private <tipo> atributo;`: só a própria classe acessa; o resto usa getter/setter. Isso é **encapsulamento**.
- **Construtor vazio** `public Plano() { }`: usado pelo DAO ao ler do banco (cria vazio e preenche com `setX`).
- **Construtor completo (sem o id):** usado pelo Controller; `this.nome = nome;` (o `this` diferencia o atributo do parâmetro). O id fica de fora porque o banco gera. Dois construtores com parâmetros diferentes = **sobrecarga**.
- **Getter** `getX()` (para boolean, `isX()`) e **setter** `setX(valor)`.

### 4.2 Mapeamento tipo SQL → tipo Java

| MySQL | Java | Por quê |
|---|---|---|
| INT, SMALLINT, TINYINT | `int` | inteiros |
| VARCHAR, CHAR, TEXT, ENUM | `String` | textos (ENUM vira o texto do valor) |
| DECIMAL | `BigDecimal` | decimal exato; `double` arredonda mal dinheiro |
| TINYINT(1) | `boolean` | 0 = falso, 1 = verdadeiro |
| DATE | `LocalDate` | só data |
| DATETIME | `LocalDateTime` | data e hora |
| INT que aceita NULL | `Integer` | tipo objeto pode ser `null`; `int` não |

### 4.3 As cinco classes

| Classe | Atributos | Observações |
|---|---|---|
| `Plano` | idPlano, nome, descricao, valorMensal (BigDecimal), qualidadeMaxima, telasSimultaneas, permiteDownload (boolean) | |
| `Conta` | idConta, **idPlano (FK)**, email, senhaHash, telefone (pode ser null), pais, status | **novo:** `nomePlano`, campo **só de exibição** preenchido pelo INNER JOIN do `ContaDAO.listar()` |
| `Perfil` | idPerfil, **idConta (FK)**, nome, avatar, perfilInfantil, pin | **novo:** `emailConta`, só de exibição (INNER JOIN do `PerfilDAO.listar()`) |
| `Titulo` | idTitulo, nome, sinopse, tipo, anoLancamento (int), classificacaoIndicativa, **duracaoMinutos (Integer)**, idiomaOriginal, dataAdicaoCatalogo, disponivel | `Integer` porque séries têm duração NULL |
| `HistoricoVisualizacao` | idVisualizacao, **idPerfil e idTitulo (FKs)**, dataInicio, dataUltimaVisualizacao, minutoParada, concluido, dispositivo | `nomePerfil`, `nomeTitulo`, `emailConta` e `nomePlano` só de exibição (JOIN). Representa a **tabela associativa** N:N |

> **Campos só de exibição** (`nomePlano`, `emailConta`, `nomePerfil`, `nomeTitulo`): **não existem nas tabelas**. São preenchidos apenas pelos SELECT com JOIN para a tela mostrar nomes. No INSERT/UPDATE são ignorados. Guardamos só o id da FK (e não um objeto `Plano` inteiro) para espelhar exatamente a tabela.

---

## 5. Pacote `dao`

DAO (Data Access Object) é a **única camada que escreve SQL**. Cada DAO cuida de uma tabela e traduz entre objetos Java e linhas do banco.

### 5.1 Conceitos que o professor vai cobrar

- **PreparedStatement e SQL Injection.** No SQL escrevemos `?` no lugar dos valores e depois enviamos os valores com `ps.setString(1, ...)`, `ps.setInt(2, ...)`. O banco recebe comando e dados **separados**: o dado é sempre valor, nunca código SQL. Se concatenássemos (`"... WHERE nome = '" + nome + "'"`) e o usuário digitasse `x' OR '1'='1`, ele alteraria o comando (**SQL Injection**). Os números dos `setX(n, valor)` começam em 1 e seguem a ordem dos `?`.
- **try-with-resources.** `try (Connection con = ...; PreparedStatement ps = ...) { ... }`: tudo nos parênteses é fechado **automaticamente** ao sair do bloco, mesmo com erro (na ordem inversa). Evita "vazamento de conexão".
- **executeUpdate x executeQuery.** `executeUpdate()` roda INSERT/UPDATE/DELETE; `executeQuery()` roda SELECT e devolve um `ResultSet`.
- **ResultSet.** A tabela resultado do SELECT. `rs.next()` avança uma linha e devolve `false` quando acabam; `rs.getInt("id_plano")`, `rs.getString("nome")` leem colunas pelo nome.
- **camelCase ↔ snake_case:** `plano.getValorMensal()` → coluna `valor_mensal`, e `rs.getBigDecimal("valor_mensal")` → `setValorMensal(...)`.
- **`throws SQLException`:** o DAO não trata o erro; repassa ao Controller, que decide a mensagem. O DAO não mostra janelas.
- **Datas:** gravar com `Date.valueOf(localDate)` / `Timestamp.valueOf(localDateTime)`; ler com `rs.getDate(...).toLocalDate()` / `rs.getTimestamp(...).toLocalDateTime()`.

### 5.2 `PlanoDAO` (referência: os outros seguem o mesmo molde)

**`inserir(Plano p)`**
- `String sql = "INSERT INTO plano (nome, descricao, valor_mensal, qualidade_maxima, telas_simultaneas, permite_download, data_criacao) VALUES (?, ?, ?, ?, ?, ?, CURDATE())";`: 7 colunas, 6 `?` (o id e a data de criação o banco preenche: `AUTO_INCREMENT` e `CURDATE()`).
- `try (Connection con = ConnectionFactory.getConnection(); PreparedStatement ps = con.prepareStatement(sql))`: abre a conexão e prepara o comando.
- `ps.setString(1, p.getNome())` ... `ps.setBoolean(6, p.isPermiteDownload())`: preenche cada `?` na ordem (`setString` aceita `null` e grava NULL; `setBoolean` vira 0/1).
- `ps.executeUpdate();`: **executa o INSERT**.

**`listar()`**
- `SELECT ... FROM plano ORDER BY id_plano`; `List<Plano> lista = new ArrayList<>();`
- `try (... ResultSet rs = ps.executeQuery())` e `while (rs.next()) { lista.add(montarPlano(rs)); }`.
- `return lista;` fica depois do `try`: quando roda, `rs`, `ps` e `con` já foram fechados.

**`atualizar(Plano p)`**: `UPDATE plano SET nome = ?, ... WHERE id_plano = ?`; os 7 primeiros `?` como no insert e `ps.setInt(8, p.getIdPlano())` no `WHERE`. **Sem o WHERE, todos os planos seriam alterados.**

**`eliminar(int idPlano)`**: `DELETE FROM plano WHERE id_plano = ?`. Se o plano tiver contas, o MySQL recusa (FK) e lança `SQLException`.

**`montarPlano(ResultSet rs)`** (privado): `new Plano()` e um `setX(rs.getX("coluna"))` por coluna. Evita repetir código.

### 5.3 `ContaDAO` (mudou: listar com INNER JOIN)

Mesma estrutura. `inserir`/`atualizar`/`eliminar` iguais à primeira versão (`ps.setInt(1, c.getIdPlano())` grava a FK; `setTimestamp(6, Timestamp.valueOf(...))` para a data com hora). **`listar()` agora faz JOIN**:

```sql
SELECT c.id_conta, c.id_plano, c.email, c.senha_hash, c.telefone, c.pais,
       c.data_cadastro, c.status, p.nome AS nome_plano
FROM conta c INNER JOIN plano p ON p.id_plano = c.id_plano
ORDER BY c.id_conta
```

- `c` e `p` são apelidos; `p.nome AS nome_plano` traz o nome do plano.
- `c.setNomePlano(rs.getString("nome_plano"));`: lê a coluna que veio do JOIN. A tela mostra **"3 - Premium"**.
- `eliminar` falha se a conta ainda tiver perfis (FK).

### 5.4 `PerfilDAO` (mudou: listar com INNER JOIN)

`listar()` faz `FROM perfil p INNER JOIN conta c ON c.id_conta = p.id_conta` e traz `c.email AS email_conta`, lido com `p.setEmailConta(rs.getString("email_conta"))`. `eliminar`: por causa do `ON DELETE CASCADE`, o histórico do perfil é apagado junto.

### 5.5 `TituloDAO`

Igual à primeira versão, com cuidado para `duracao_minutos` NULL:
- `preencher(ps, t)`: método auxiliar que preenche os 9 `?` comuns ao INSERT e ao UPDATE. Para a duração: `if (t.getDuracaoMinutos() == null) { ps.setNull(6, Types.INTEGER); } else { ps.setInt(6, t.getDuracaoMinutos()); }`. `setInt` não aceita `null`, por isso `setNull` informando o tipo SQL.
- `listar`: `int duracao = rs.getInt("duracao_minutos"); t.setDuracaoMinutos(rs.wasNull() ? null : duracao);`. `getInt` devolve 0 quando é NULL; `wasNull()` diz se o último valor lido era NULL.
- `buscarPorId(id)`: `SELECT ... WHERE id_titulo = ?` com `ps.setInt(1, id)`; devolve o `Titulo` ou `null` se não existir (`return rs.next() ? montar(rs) : null;`). É o mesmo padrão do `select ... where` do exemplo do professor. O `HistoricoController` o usa para saber a duração.
- `montar(rs)`: converte a linha atual do `ResultSet` em `Titulo`; `listar` e `buscarPorId` o reutilizam.

### 5.6 `HistoricoVisualizacaoDAO` (CRUD principal + consultas do relatório)

- **`SELECT_COM_NOMES` (constante privada):** a parte comum das duas consultas com INNER JOIN (escrita uma vez só):
  ```sql
  SELECT h.id_visualizacao, h.id_perfil, h.id_titulo, h.data_inicio,
         h.data_ultima_visualizacao, h.minuto_parada, h.concluido, h.dispositivo,
         p.nome AS nome_perfil, t.nome AS nome_titulo,
         c.email AS email_conta, pl.nome AS nome_plano
  FROM historico_visualizacao h
  INNER JOIN perfil p ON p.id_perfil = h.id_perfil
  INNER JOIN titulo t ON t.id_titulo = h.id_titulo
  INNER JOIN conta c ON c.id_conta = p.id_conta
  INNER JOIN plano pl ON pl.id_plano = c.id_plano
  ```
  Os apelidos `nome_perfil`, `nome_titulo` e `nome_plano` são necessários porque perfil, título e plano têm uma coluna `nome`. O JOIN com `conta` e `plano` mostra, para cada visualização, **de qual conta e de qual plano** é o perfil (perfil → conta → plano).
- `inserir`, `atualizar`, `eliminar`: iguais à primeira versão. Gravam as 2 FKs, as datas com `Timestamp.valueOf`, minuto, concluído e dispositivo (os nomes não são gravados).
- **`listarComNomes()`**: `SELECT_COM_NOMES + "ORDER BY h.data_ultima_visualizacao DESC"` (mais recentes primeiro). É o **Read** do CRUD principal e a **consulta obrigatória com JOIN**.
- **`listarPorPeriodo(dataInicial, dataFinal, idPerfil)` (novo, do relatório):**
  - `String sql = SELECT_COM_NOMES + "WHERE DATE(h.data_inicio) BETWEEN ? AND ? ";`: `DATE(...)` tira a hora do DATETIME; `BETWEEN` inclui as duas pontas.
  - `if (idPerfil > 0) { sql += "AND h.id_perfil = ? "; }`: filtro **opcional**: só entra se um perfil foi escolhido (0 = "Todos").
  - `sql += "ORDER BY h.data_inicio";`: ordem cronológica.
  - `ps.setDate(1, Date.valueOf(dataInicial)); ps.setDate(2, Date.valueOf(dataFinal)); if (idPerfil > 0) { ps.setInt(3, idPerfil); }`: o terceiro `?` só existe se o filtro foi acrescentado.
- **`montar(ResultSet rs)`** (privado): converte a linha atual em `HistoricoVisualizacao`, incluindo `setNomePerfil(rs.getString("nome_perfil"))` e `setNomeTitulo(...)`. Usado pelas duas consultas.

---

## 6. Pacote `controller`

O Controller é o "meio de campo":

```
View --(Strings digitadas)--> Controller --(objeto Model)--> DAO --> MySQL
```

Sempre nesta ordem: (1) recebe `String`; (2) valida e converte com o `Conversor`; (3) cria o Model e chama o DAO; (4) devolve uma **mensagem** (sucesso ou erro).

### 6.1 `PlanoController` (referência)

- `private final PlanoDAO dao = new PlanoDAO();`: o Controller **tem um** DAO (agregação); `final` = a referência não muda.
- `listar()`: `try { return dao.listar(); } catch (SQLException e) { throw new RuntimeException("Erro ao listar planos: " + ..., e); }`. Como `listar` devolve uma **lista** (não uma mensagem), o erro sobe como `RuntimeException` e a `TelaCrud.recarregar()` mostra.
- `inserir(String nome, String valorMensal, String qualidadeMaxima, String telas, String permiteDownload)`: `dao.inserir(montar(...)); return "Plano inserido com sucesso!";` (não recebe descrição: ela é gerada).
  - `catch (IllegalArgumentException e) { return e.getMessage(); }`: erro de **validação** (Conversor), antes do banco.
  - `catch (SQLException e) { return "Erro no banco de dados: " + e.getMessage(); }`: erro do **banco**, inclusive MySQL desligado (na versão principal a `ConnectionFactory` lança `SQLException`).
- `atualizar(String id, ...)`: `Conversor.paraId(id)` (nada selecionado → mensagem), `montar(...)`, `p.setIdPlano(idPlano)`, `dao.atualizar(p)`.
- `eliminar(String id)`: se a FK impedir, devolve "Não foi possível remover (o plano pode estar em uso por uma conta)".
- `gerarDescricao(String telas, String qualidadeMaxima)` (público): **regra de negócio**. Converte `telas` em `int` (se for inválido ou menor que 1, devolve `""`), escolhe o texto do número (`"Uma tela"`, `"Duas telas"`, `"Três telas"`, `"Quatro telas"` ou `n + " telas"`) e devolve `telasTexto + ", qualidade " + qualidadeMaxima`. É chamado pela tela (para mostrar ao vivo) e por `montar(...)` (para gravar).
- `montar(...)` (privado): valida `telas >= 1` ("O plano precisa ter pelo menos 1 tela simultânea.") e cria `new Plano(Conversor.obrigatorio(nome, "Nome"), gerarDescricao(telas, qualidadeMaxima), Conversor.paraDecimal(...), qualidadeMaxima, quantidadeTelas, Conversor.paraBoolean(...), Conversor.paraData(...))`.

### 6.2 `ContaController`, `PerfilController`, `TituloController`

Mesmo molde. Diferenças:

- **ContaController:** tem também um `PlanoDAO` para `opcoesPlanos()`, que devolve `Conversor.opcao(id, nome)` de cada plano. Regra de negócio em `montar`: país com 2 letras (`paisValido.length() != 2` → erro) e `toUpperCase()`. `Conversor.extrairId(plano, "Plano")` tira a FK do combo. Erro de banco no `inserir` avisa "e-mail já cadastrado?" (coluna UNIQUE).
- **PerfilController:** `ContaDAO` para `opcoesContas()` (`"id - email"`). Regra: PIN opcional, mas se informado `pinValido.matches("\\d{4}")` (expressão regular: exatamente 4 dígitos).
- **TituloController:** `Conversor.paraIntOuNulo(duracao, ...)`: duração vazia vira `null` (séries).

### 6.3 `HistoricoController` (CRUD principal)

- Três DAOs: `HistoricoVisualizacaoDAO`, `PerfilDAO` e `TituloDAO` (`PerfilDAO` e `TituloDAO` alimentam os combos; o `TituloDAO` também busca a duração do título escolhido com `buscarPorId`).
- `listarComNomes()`, `opcoesPerfis()`, `opcoesTitulos()` (formato `"id - nome"`).
- `inserir`, `atualizar`, `eliminar`: mesmo molde.
- `montar(...)`: converte as datas e o minuto e aplica **regras de negócio**:
  - Início e última visualização **não são digitados**: o `HistoricoVisualizacaoDAO` grava `NOW()` no INSERT (as duas) e só a última no UPDATE.
  - `if (minuto < 0)` → "O minuto de parada não pode ser negativo."
  - `duracaoDoTitulo(idTitulo)` chama `TituloDAO.buscarPorId` (`SELECT ... WHERE id_titulo = ?`) e devolve a duração (`null` para séries). Com duração: minuto maior que ela é recusado; **Concluído = Sim só com o minuto igual à duração**, e o minuto igual à duração só com Concluído = Sim.
  - `Conversor.extrairId(perfil, "Perfil")` e `extrairId(titulo, "Título")`: as FKs.

### 6.4 `RelatorioController` (novo: relatório da rotina principal)

- `public static final String TODOS = "Todos";`: texto da opção "todos os perfis" (constante para não digitar a mesma string em dois lugares).
- `opcoesPerfis()`: lista com `"Todos"` + `"id - nome"` de cada perfil.
- `gerar(String dataInicial, String dataFinal, String perfil)`:
  - converte as duas datas com `Conversor.paraData`;
  - **regra:** `if (fim.isBefore(inicio))` → "A data final não pode ser anterior à data inicial.";
  - `int idPerfil = TODOS.equals(perfil) ? 0 : Conversor.extrairId(perfil, "Perfil");`: 0 significa "todos";
  - `return dao.listarPorPeriodo(inicio, fim, idPerfil);` (SQLException vira RuntimeException, como nos `listar`).
- `resumo(List<HistoricoVisualizacao> lista)`: **calcula os totais** percorrendo a lista: `if (h.isConcluido()) { concluidas++; }` e `minutos += h.getMinutoParada();`. Devolve "Total de visualizações: 6 | Concluídas: 3 | Em andamento: 3 | Minutos assistidos: 422". Fica no Controller porque é regra de negócio, não desenho de tela.
- `salvarTxt(caminho, lista, ...)`: monta o texto do relatório com `StringBuilder` (junta pedaços de texto de forma eficiente) e grava com `Files.write(Path.of(caminho), texto.getBytes(UTF_8))`. Devolve a mensagem de sucesso ou de erro (`IOException`).

### 6.5 `ConexaoController` (novo)

- Construtor: `ConnectionFactory.carregarConfiguracao();` lê o `config.properties` para a tela já abrir preenchida, **só na primeira vez** (trava `configuracaoCarregada`). O menu também cria um `ConexaoController`; sem a trava ele releria o arquivo e poderia trocar a senha recém-digitada por uma antiga. `servidorSalvo()`, `portaSalva()`, `usuarioSalvo()`, `senhaSalva()` entregam esses valores à tela.
- `conectar(servidor, porta, usuario, senha)`:
  1. valida (`obrigatorio`, `paraInt`) e chama `ConnectionFactory.configurar(...)`;
  2. `CriadorBanco.criarSeNaoExistir();`: na primeira vez cria o banco com os dados de exemplo (isso também **testa** a conexão);
  3. `ConnectionFactory.salvarConfiguracao();` dentro de um `try`: se a pasta não permitir gravar, **não bloqueia o login**; o sistema abre e `getAviso()` devolve um aviso que a `TelaConexao` mostra ("não foi possível salvar a configuração... será preciso digitar a senha de novo");
  4. `return null;`: **null significa "deu certo"**; qualquer texto é a mensagem de erro. Erro de `SQLException` vira "Não foi possível conectar ao MySQL. Confira se ele está ligado e se o usuário/senha estão certos."
- `restaurarDadosExemplo()`: `CriadorBanco.recriarBanco()` e mensagem de sucesso/erro.

---

## 7. Pacote `view` (telas Swing) e o `Main`

A View só **captura dados** e **mostra resultados**. Nenhum botão tem SQL ou regra de negócio.

### 7.1 Conceitos de Swing usados

- **`JFrame`:** a janela.
- **`JTable` + `DefaultTableModel`:** o `JTable` só **desenha**; os dados vivem no `DefaultTableModel` ("planilha em memória"). `setRowCount(0)` apaga as linhas e `addRow(Object[])` adiciona uma.
- **`ActionListener`:** "o que fazer quando o botão for clicado", registrado com `botao.addActionListener(...)`. Usamos **lambdas**: `e -> fazerAlgo()` é a forma curta de `new ActionListener() { public void actionPerformed(ActionEvent e) { fazerAlgo(); } }`.
- **`JComboBox`:** lista suspensa (ENUMs, Sim/Nao, chaves estrangeiras).
- **`JPasswordField`:** caixa de texto que mostra `•••` (senha do MySQL).
- **`JFileChooser`:** janela "Salvar como" (relatório .txt).
- **Layouts:** `BorderLayout` (NORTH, CENTER, SOUTH), `GridLayout` (grade), `FlowLayout` (em fila).
- **`JOptionPane`:** janelinhas de mensagem e de confirmação.

### 7.2 `Main` (ponto de entrada)

- `public static void main(String[] args)`: é o que o `Netflix.jar` executa (o manifesto do jar diz `Main-Class: Main`).
- `SwingUtilities.invokeLater(() -> new TelaConexao().setVisible(true));`: cria a primeira tela na *Event Dispatch Thread*, a thread que o Swing exige para mexer na interface.

### 7.3 `TelaConexao` (nova)

- Campos `JTextField` para servidor, porta, usuário e `JPasswordField` para senha, já preenchidos com `controller.servidorSalvo()` etc.
- `GridLayout(0, 2, 5, 5)`: duas colunas (rótulo, campo).
- `getRootPane().setDefaultButton(btnConectar)`: apertar Enter clica em Conectar.
- `conectar()`: `String erro = controller.conectar(...)`; `new String(campoSenha.getPassword())` converte a senha (o `JPasswordField` devolve `char[]`). `if (erro == null) { Navegador.trocar(this, new TelaPrincipal()); }` abre o menu com fade e fecha esta tela; senão mostra o erro.

### 7.4 `TelaPrincipal` (menu)

- Seis `JButton`, numerados na ordem da apresentação: 1. Planos, 2. Contas, 3. Perfis, 4. Títulos, 5. Histórico de Visualização, e **Restaurar dados de exemplo**. O Relatório não tem botão no menu: é aberto pelo botão **Relatório** da tela de Histórico.
- `btnPlanos.addActionListener(e -> Navegador.ir(this, new TelaPlano()));`: cada clique **cria** a tela nova, mostra no mesmo lugar com fade-in e fecha a atual (ver `Navegador` abaixo). A tela se chama **Início** e tem no topo a `BarraNavegacao`, com o botão **Restaurar dados** no canto direito. (Na primeira versão isso usava `Supplier<JFrame>` e `TelaPlano::new`; agora é direto.)
- `restaurar()`: pede confirmação (`showConfirmDialog`) e chama `conexaoController.restaurarDadosExemplo()`.
- `setDefaultCloseOperation(EXIT_ON_CLOSE)`: fechar o menu encerra o programa.

### 7.4.1 `BarraNavegacao` e `Navegador` (navegação sem janelas empilhadas)

- **`BarraNavegacao`** (`extends JPanel`): a marca NETFLIX e um botão para cada tela (Início, Planos, Contas, Perfis, Títulos, Histórico). Aparece no topo de **todas** as telas (`TelaPrincipal`, `TelaCrud`, `TelaRelatorio`); o botão da tela atual fica desabilitado. Cada botão é uma linha: `e -> Navegador.ir(dona, new TelaPlano())`. Só monta a interface, sem SQL (MVC).
- **`Navegador.ir(atual, nova)`**: a tela nova abre na **mesma posição e tamanho** da atual (`setBounds`), é mostrada com fade-in e a atual é fechada (`dispose`). Nunca há duas janelas ao mesmo tempo. `Navegador.trocar` é usado só da `TelaConexao` para o menu (abre centralizado).
- O fade-in é um **véu** (`glass pane` do `JFrame`) com a cor de fundo da tela, pintado por cima do conteúdo; um `javax.swing.Timer` (16 ms) diminui a opacidade em 9 passos (~150 ms) até o véu sumir e a tela voltar a receber cliques.
- Por que não `Window.setOpacity`? Ele lança `IllegalComponentStateException` em janela com barra de título do Windows.
- Como só existe uma janela por vez, **fechar no X encerra o programa** (`EXIT_ON_CLOSE`) em todas as telas.
- A versão principal tem a **mesma lógica de telas** da versão visual; a diferença é só o visual (a versão visual usa `Tema`, capas, cartões e botões desenhados).

### 7.5 `TelaCrud` (tela base, `abstract`)

Padrão **Template Method**: a classe base define o esqueleto (layout, botões, fluxo) e as filhas preenchem os "buracos" (métodos abstratos). Isso é **herança** e **polimorfismo**: quando a base chama `inserir()`, executa a versão da tela filha aberta.

**Atributos**
- `protected final DefaultTableModel modelo;` e `protected final JTable tabela;`: `protected` = as filhas acessam.
- `private final Map<String, JComponent> campos = new LinkedHashMap<>();`: liga cada rótulo ("Nome") ao seu campo. `LinkedHashMap` mantém a ordem; `JComponent` é a superclasse de `JTextField` e `JComboBox`.
- `private final JPanel painelForm = new JPanel(new GridLayout(0, 2, 5, 5));`: formulário em 2 colunas.

**Construtor `protected TelaCrud(String titulo, String[] colunas)`**
- `super(titulo);`: chama o construtor de `JFrame` (título da janela).
- `modelo = new DefaultTableModel(colunas, 0) { @Override public boolean isCellEditable(...) { return false; } };`: cria o modelo; as chaves criam uma **classe anônima** que sobrescreve `isCellEditable` (tabela só de leitura).
- `tabela = new JTable(modelo); tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);`.

**Métodos que as filhas implementam**
- Abstratos: `carregarTabela()`, `String inserir()`, `String atualizar(String id)`, `String eliminar(String id)`.
- Ganchos opcionais (vêm vazios): `carregarCombos()` (**novo**: preenche os combos de FK) e `definirPadroes()` (valores iniciais).

**Campos do formulário**
- `adicionarCampo(rotulo, componente)`: guarda no mapa e coloca rótulo e campo no painel.
- `preencherCombo(JComboBox<String> combo, List<String> opcoes)` (**novo**): `combo.removeAllItems()` e `addItem` para cada opção.
- `valor(rotulo)`: lê um campo como texto. `c instanceof JComboBox` descobre o tipo real; combo → `getSelectedItem().toString()`; caixa de texto → `((JTextField) c).getText()` (o *cast* trata o componente como caixa de texto).
- `definirValor(String rotulo, String texto)` e `definirValor(JComponent c, String texto)`: mesmo nome, parâmetros diferentes = **sobrecarga**. No combo: `setSelectedItem(texto)` seleciona o item igual ao texto (ex.: `"3 - Premium"`).

**`iniciar()`** (chamado no **final** do construtor da filha, porque no construtor da base o `controller` da filha ainda não existe):
- `btnInserir.addActionListener(e -> mostrarResultado(inserir()));`: chama o `inserir()` da filha (que chama o Controller) e mostra a mensagem.
- `btnAtualizar.addActionListener(e -> mostrarResultado(atualizar(idSelecionado())));`.
- `btnEliminar.addActionListener(e -> confirmarEliminar());` e `btnLimpar.addActionListener(e -> limpar());`.
- `tabela.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && tabela.getSelectedRow() >= 0) { aoSelecionar(); } });`: ao clicar numa linha, copia para o formulário. `getValueIsAdjusting()` é `true` enquanto a seleção ainda muda.
- `add(painelForm, BorderLayout.NORTH); add(new JScrollPane(tabela), BorderLayout.CENTER); add(painelBotoes, BorderLayout.SOUTH);`: formulário em cima, tabela no meio (com rolagem), botões embaixo.
- `setDefaultCloseOperation(DISPOSE_ON_CLOSE)`: fecha só esta janela.
- `recarregar(true);`: carrega combos e tabela e deixa o formulário com os valores iniciais.

**Demais métodos (privados)**
- `aoSelecionar()`: percorre os campos e copia as células da linha: `modelo.getValueAt(linha, coluna++)`, começando na coluna 1 (a 0 é o ID). **Convenção:** colunas da tabela = ID + um campo por coluna, na mesma ordem. Como a tabela mostra `"3 - Premium"` e o combo tem `"3 - Premium"`, funciona para FK também.
- `confirmarEliminar()`: se nenhuma linha está selecionada, chama `mostrarResultado(eliminar(""))` direto (o Controller avisa "Selecione um registro") sem perguntar nada; senão `showConfirmDialog` e, só com **Sim**, `mostrarResultado(eliminar(idSelecionado()))`.
- `mostrarResultado(String mensagem)`: mostra a mensagem num `JOptionPane` e chama `recarregar(deuCerto)`, onde `deuCerto = mensagem.contains("sucesso")`. Assim o formulário só é limpo quando a ação funcionou.
- `recarregar(boolean limparFormulario)`: `modelo.setRowCount(0);` e, dentro de `try`, `carregarCombos(); carregarTabela();`. `catch (RuntimeException e)` mostra o erro (ex.: MySQL desligado). Se `limparFormulario` for `true`, termina com `limpar()`. Se for `false` (depois de um erro), recarregar os combos os volta ao primeiro item e a tabela perde a seleção; por isso o método guarda a escolha de cada combo e o ID da linha selecionada e restaura os dois no fim. O atributo `recarregandoTabela` impede que a re-seleção da linha sobrescreva o formulário com os valores da tabela.
- `limpar()`: caixas de texto ficam vazias, combos voltam ao primeiro item, `tabela.clearSelection()` e `definirPadroes()`.
- `idSelecionado()`: ID (coluna 0) da linha selecionada, ou `""` (o Controller avisa "Selecione um registro").

### 7.6 As telas filhas (`TelaPlano`, `TelaConta`, `TelaPerfil`, `TelaTitulo`, `TelaHistorico`)

Todas têm a mesma estrutura:
- `extends TelaCrud`: **herança**; ganham layout, botões e fluxo prontos.
- `private final XController controller = new XController();`.
- Combos de FK como atributos: `private final JComboBox<String> comboPlano = new JComboBox<>();` (começam vazios e são preenchidos em `carregarCombos()`).
- Construtor: `super("Título", new String[]{colunas...});`, um `adicionarCampo(...)` por campo e `iniciar();` por último.
- `carregarCombos()`: `preencherCombo(comboPlano, controller.opcoesPlanos());`.
- `carregarTabela()`: `for (Plano p : controller.listar()) { modelo.addRow(new Object[]{...}); }`. **É aqui que o `DefaultTableModel` é preenchido.** Colunas de FK usam `Conversor.opcao(id, nome)`.
- `inserir()` / `atualizar(id)` / `eliminar(id)`: só `return controller.inserir(valor("Nome"), ...);`. Nenhuma lógica.
- `definirPadroes()`: ex.: `definirValor("País", "BR");`.

| Tela | Combos | Detalhes |
|---|---|---|
| `TelaPlano` | Qualidade máxima, Permite download | valor mensal digitado como texto; **descrição automática** (campo só leitura, ver abaixo) |
| `TelaConta` | **Plano (FK)**, Status | coluna Plano mostra "3 - Premium" (JOIN); padrões: país BR, data/hora atual |
| `TelaPerfil` | **Conta (FK)**, Perfil infantil | coluna Conta mostra "1 - ana.silva@email.com" (JOIN); PIN opcional |
| `TelaTitulo` | Tipo, Classificação, Disponível | duração pode ficar vazia (séries) |
| `TelaHistorico` | **Perfil (FK)**, **Título (FK)**, Concluído, Dispositivo | **CRUD principal**; tabela com o INNER JOIN: "1 - Ana", "1 - Stranger Things" |

**`TelaPlano`: descrição automática.** A descrição não é digitada. A tela guarda os dois combos, o de telas simultâneas (`comboTelas`, com 1, 2 e 4) e o de qualidade (`comboQualidade`), em atributos e liga os dois a `atualizarDescricao()` com `addActionListener(e -> atualizarDescricao())` (também dispara quando o código seleciona o valor ao clicar numa linha da tabela). `atualizarDescricao()` chama `controller.gerarDescricao(...)` e coloca o resultado no campo "Descrição (automática)", que é `setEditable(false)`. A regra de como o texto é montado fica no Controller (MVC).

> **O que sumiu da `TelaHistorico` na versão principal:** as duas colunas escondidas com os IDs e a sobrescrita de `aoSelecionar()`. Elas existiam porque a tabela mostrava só "Ana" e o combo tinha "1 - Ana". Agora os dois mostram "1 - Ana", então o `aoSelecionar()` padrão da `TelaCrud` resolve.

### 7.7 `TelaRelatorio` (nova: relatório da rotina principal)

Não herda de `TelaCrud` porque **não é um cadastro**: tem filtros, uma tabela e totais.

- Campos: `campoDataInicial` (padrão `01/01/2025`), `campoDataFinal` (padrão hoje), `comboPerfil` ("Todos" + perfis) e `labelResumo` (totais embaixo).
- `modelo = new DefaultTableModel(new String[]{"ID", "Início", "Perfil", "Título", "Minuto de parada", "Concluído", "Dispositivo"}, 0) {...}`: tabela só de leitura.
- `private List<HistoricoVisualizacao> resultado`: guarda o último relatório, usado pelo botão **Salvar .txt**.
- Botões: `btnGerar.addActionListener(e -> gerar());` e `btnSalvar.addActionListener(e -> salvar());`.
- No construtor, preenche o combo de perfis e já chama `gerar()` (abre mostrando o relatório). O `try/catch (RuntimeException e)` mostra o erro se o banco estiver fora do ar.
- `gerar()`: `resultado = controller.gerar(dataInicial, dataFinal, perfil);` (validação e erro de banco caem no `catch`); depois `modelo.setRowCount(0)`, um `addRow` por visualização (nomes vindos do JOIN) e `labelResumo.setText(controller.resumo(resultado))`.
- `salvar()`: `JFileChooser escolher = new JFileChooser(); escolher.setSelectedFile(new File("relatorio_visualizacoes.txt"));`; se o usuário confirmar (`APPROVE_OPTION`), chama `controller.salvarTxt(...)` e mostra a mensagem.

---

## 8. Integração Java ↔ MySQL (o que o professor mais cobra)

### 8.1 As cinco peças da integração

```
1. Driver     mysql-connector-j-9.3.0.jar (dentro do Netflix.jar)   o "tradutor" Java <-> MySQL
2. Conexão    ConnectionFactory.getConnection()  ->  objeto Connection
3. Comando    String sql com "?"  ->  PreparedStatement (con.prepareStatement(sql))
4. Execução   executeUpdate() (INSERT/UPDATE/DELETE)  ou  executeQuery() (SELECT -> ResultSet)
5. Conversão  objeto -> colunas (ps.setX)   e   linha -> objeto (rs.getX + setX do model)
```

### 8.2 A conexão em detalhe

| Pedaço da URL | Significado |
|---|---|
| `jdbc:mysql://` | protocolo: JDBC falando com MySQL (o driver "reconhece" esse prefixo) |
| `localhost` | o servidor está neste computador (configurável na tela de conexão) |
| `3306` | porta padrão do MySQL |
| `/netflix` | banco a usar (equivale ao `USE netflix;`) |
| `?useSSL=false...` | parâmetros extras |

- `DriverManager.getConnection(URL, USUARIO, SENHA)` procura entre os drivers carregados um que entenda a URL e abre a conexão.
- Cada DAO abre uma conexão **por operação** e fecha ao terminar (try-with-resources). Simples e seguro para um trabalho acadêmico; sistemas grandes usam um *pool* de conexões.

### 8.3 "set" para gravar, "get" para ler

| Coluna | Atributo / tipo Java | Gravar (`PreparedStatement`) | Ler (`ResultSet`) |
|---|---|---|---|
| `plano.valor_mensal` | `valorMensal` / BigDecimal | `setBigDecimal` | `getBigDecimal` |
| `plano.permite_download` | `permiteDownload` / boolean | `setBoolean` | `getBoolean` |
| `conta.id_plano` (FK) | `idPlano` / int | `setInt` | `getInt` |
| `conta.telefone` | `telefone` / String (null) | `setString` (null → NULL) | `getString` (NULL → null) |
| `titulo.duracao_minutos` | `duracaoMinutos` / Integer (null) | `setInt` ou `setNull(i, Types.INTEGER)` | `getInt` + `wasNull()` |
| (JOIN) `plano.nome AS nome_plano` | `nomePlano` / String | não grava | `getString("nome_plano")` |
| (JOIN) `perfil.nome AS nome_perfil` | `nomePerfil` / String | não grava | `getString("nome_perfil")` |
| (JOIN) `conta.email AS email_conta` | `emailConta` / String | não grava | `getString("email_conta")` |
| (JOIN) `plano.nome AS nome_plano` | `nomePlano` / String | não grava | `getString("nome_plano")` |

No `PreparedStatement` o primeiro parâmetro é a **posição do `?`** (começa em 1); no `ResultSet` pode ser o **nome da coluna**.

### 8.4 Exemplo rastreado: INSERIR um plano

O usuário digita Nome `Familia`, Valor `29,90`, Qualidade `HD`, Telas `2` (escolhida na lista 1, 2 ou 4), Download `Sim` e clica em **Inserir**. (A Descrição não é digitada: a tela mostra "Duas telas, qualidade HD", gerada pelo Controller.)

1. **View:** `mostrarResultado(inserir())`; `TelaPlano.inserir()` chama `controller.inserir("Familia", "29,90", "HD", "2", "Sim")`.
2. **Controller (`montar`):** `"Familia"` → `obrigatorio`; `"3"` → `paraInt` (e precisa ser ≥ 1) e, com a qualidade, vira a descrição `gerarDescricao("3", "HD")` = "Duas telas, qualidade HD"; `"29,90"` → `paraDecimal` = 29.90; `"Sim"` → `true`; data → `LocalDate`.
3. **DAO:** `INSERT INTO plano (...) VALUES (?, ?, ?, ?, ?, ?, ?)` com `setString(1,"Familia")`, `setString(2,"Duas telas, qualidade HD")`, `setBigDecimal(3, 29.90)`, `setString(4,"HD")`, `setInt(5,3)`, `setBoolean(6,true)` → 1, `setDate(7, ...)`.
4. **Execução:** `executeUpdate()`; o MySQL gera `id_plano = 4` (`AUTO_INCREMENT`); o código não precisa desse número de volta.
5. **Volta:** "Plano inserido com sucesso!" → `JOptionPane` → `recarregar()`.

### 8.5 Exemplo rastreado: o INNER JOIN do histórico

No banco a linha guarda só números: `id_visualizacao=1, id_perfil=1, id_titulo=1, minuto_parada=55`. O `listarComNomes()` devolve:

| id_visualizacao | id_perfil | id_titulo | minuto_parada | nome_perfil | nome_titulo | email_conta | nome_plano |
|---|---|---|---|---|---|---|---|
| 1 | 1 | 1 | 55 | Ana | Stranger Things | ana.silva@email.com | Premium |

O DAO cria o objeto com `setNomePerfil("Ana")` e `setNomeTitulo("Stranger Things")`; a tela mostra **"1 - Ana"** e **"1 - Stranger Things"**.

### 8.6 Exemplo rastreado: o RELATÓRIO

Filtro: 01/06/2025 a 05/06/2025, perfil "Todos".
1. `TelaRelatorio.gerar()` → `RelatorioController.gerar("01/06/2025", "05/06/2025", "Todos")`.
2. Controller: converte as datas, confere que a final não é anterior à inicial, `idPerfil = 0`.
3. DAO: `SELECT_COM_NOMES + "WHERE DATE(h.data_inicio) BETWEEN ? AND ? ORDER BY h.data_inicio"` (sem o filtro de perfil, porque é 0).
4. Resultado: 3 visualizações (Ana/Stranger Things, Ana/Ainda Estou Aqui, Pedro/Round 6).
5. `resumo(...)`: "Total de visualizações: 3 | Concluídas: 1 | Em andamento: 2 | Minutos assistidos: 237".

### 8.7 Quando o banco recusa: o caminho do erro até a tela

```
MySQL recusa (ou está desligado) -> SQLException
  -> o DAO não trata: "throws SQLException" repassa
  -> o Controller captura (catch) e devolve uma String com a mensagem
  -> a View mostra a String em um JOptionPane
```

| O que o usuário faz | Quem recusa | Mensagem |
|---|---|---|
| Remover um plano que tem contas | MySQL (FK) | "Não foi possível remover (o plano pode estar em uso...)" |
| Conta com e-mail repetido | MySQL (UNIQUE) | "Erro no banco de dados (e-mail já cadastrado?)..." |
| Nome vazio | Controller (`Conversor.obrigatorio`), antes do banco | "O campo 'Nome' é obrigatório." |
| `abc` no Ano | Controller (`Conversor.paraInt`) | "O campo 'Ano lançamento' deve ser um número inteiro." |
| Data final antes da inicial no relatório | `RelatorioController.gerar` | "A data final não pode ser anterior à data inicial." |
| MySQL desligado ou senha errada ao abrir | `ConexaoController.conectar` | "Não foi possível conectar ao MySQL..." |

Boa prática: validar no Controller **antes** de ir ao banco e, mesmo assim, deixar o banco como última defesa (PK, FK, UNIQUE, NOT NULL, ENUM).

### 8.8 Comparação com o exemplo da disciplina (`DAO` / `ManterAluno`)

| Exemplo da aula | No nosso projeto | Diferença |
|---|---|---|
| `class DAO` com `abrirBanco()` | `ConnectionFactory.getConnection()` | a conexão virou classe própria e é devolvida |
| `fecharBanco()` fecha só o `pst` | try-with-resources | fecha conexão, statement e resultset sozinho |
| `ManterAluno extends DAO` | `PlanoDAO` etc. sem herança | cada DAO pega sua conexão |
| `Class.forName("com.mysql.jdbc.Driver")` | driver carregado sozinho (JDBC 4) | a classe antiga não existe no conector 9.x |
| `inserir`, `deletarAluno`, `editarAluno`, `PesquisarTudo` | `inserir`, `eliminar`, `atualizar`, `listar` | iguais: PreparedStatement com `?` |
| `JOptionPane` dentro do DAO | mensagem devolvida pelo Controller | o DAO não mostra janelas (MVC) |
| botão chama `ManterAluno` direto | botão chama o Controller | a proposta proíbe lógica nos eventos do botão |
| `DefaultTableModel m = (DefaultTableModel) jTable.getModel(); m.addRow(...)` | `modelo.addRow(new Object[]{...})` | igual |

---

## 9. Conceitos de POO (dos slides do professor) aplicados ao projeto

### 9.1 Mapa conceito → código

| Conceito | Definição do curso | Onde aparece |
|---|---|---|
| **Abstração** | ficar só com o essencial | a Netflix real reduzida a 5 entidades com os atributos que o sistema usa |
| **Objeto** | identidade, estado, comportamento | `Plano p = new Plano(...)`: referência/idPlano, valores dos atributos, métodos |
| **Classe** | "gabarito" de objetos | `Plano`, `Conta`, `Perfil`, `Titulo`, `HistoricoVisualizacao` |
| **Instanciação** | `new` cria um objeto | `new Plano()` no DAO, `new PlanoController()` na tela, `new TelaPlano()` no menu |
| **Mensagem** | um objeto pede um serviço a outro | `controller.inserir(nome, ...)`: a tela pede, o Controller sabe fazer |
| **Visibilidade** | private / protected / public | atributos `private` nos models; `protected` em `TelaCrud` (`modelo`, `tabela`, `adicionarCampo`) |
| **Encapsulamento** | atributos só mudam pelos métodos | atributos `private` + `getX`/`setX` |
| **Herança** | subclasse herda (é-um) | `TelaPlano extends TelaCrud extends JFrame` |
| **Generalização / especialização** | a superclasse concentra o comum | `TelaCrud` é o geral; cada tela especializa com seus campos |
| **Polimorfismo** | mesma mensagem, comportamentos diferentes | `TelaCrud` chama `inserir()` e roda a versão da tela aberta (9.2) |
| **Late binding** | método decidido em tempo de execução | a chamada `inserir()` em `TelaCrud` só é resolvida quando a tela roda |
| **Classe abstrata** | não se instancia; obriga as filhas a implementar | `abstract class TelaCrud` com 4 métodos abstratos |
| **Sobrescrita (@Override)** | a subclasse redefine um método herdado | `carregarTabela()`, `carregarCombos()`, `definirPadroes()` nas telas; `isCellEditable` |
| **Sobrecarga** | mesmo nome, parâmetros diferentes | construtores vazio e completo dos models; `definirValor(String, String)` e `definirValor(JComponent, String)` |
| **Associação e multiplicidade** | ligação com cardinalidade | Plano 1..* Conta; Conta 1..* Perfil; Perfil *..* Título via `HistoricoVisualizacao` |
| **Agregação ("tem-um")** | uma classe tem outra como parte | `TelaPlano` tem um `PlanoController`, que tem um `PlanoDAO` |
| **Classe associativa** | resolve N:N guardando dados da relação | `HistoricoVisualizacao` (minuto, dispositivo, datas) |
| **Membros estáticos** | pertencem à classe | `ConnectionFactory.getConnection()`, `CriadorBanco`, `Conversor`, `RelatorioController.TODOS` |

### 9.2 Polimorfismo: três exemplos no projeto

1. **Métodos abstratos de `TelaCrud` (o mais claro).** A base chama `inserir()`, `atualizar(id)`, `eliminar(id)`, `carregarTabela()` e `carregarCombos()` sem saber qual tela está aberta. Na `TelaPlano` eles chamam `PlanoController`; na `TelaTitulo`, `TituloController`. É como o `sacar()` de `ContaCorrente` e `ContaPoupanca` da aula.
2. **`Map<String, JComponent> campos`.** O mapa guarda `JTextField` e `JComboBox` como `JComponent` (superclasse comum). Em `valor()` usamos `c instanceof JComboBox` para descobrir o tipo real, como o slide usa `func instanceof Gerente`.
3. **Variáveis do tipo da superclasse.** `JComponent componente` em `adicionarCampo(String rotulo, JComponent componente)` aceita qualquer componente específico: "tratar um Gerente como Funcionário".

### 9.3 Herança: a cadeia completa

```
Object <- Component <- Container <- Window <- Frame <- JFrame <- TelaCrud (abstract) <- TelaPlano
                                                                                   <- TelaConta
                                                                                   <- TelaPerfil
                                                                                   <- TelaTitulo
                                                                                   <- TelaHistorico
JFrame <- TelaPrincipal, TelaConexao, TelaRelatorio   (direto, não são cadastros)
```

- `super(titulo, colunas)` no construtor da filha chama o construtor de `TelaCrud`, que chama `super(titulo)` de `JFrame`.

---

## 10. Como gerar a build (`Netflix.jar`)

Só é preciso se alguém **mudar o código**. O jar pronto já está em `Netflix.jar`.

- **Windows:** dois cliques em `Netflix/build.bat`.
- **Linux/macOS:** `bash Netflix/build.sh`.

O que o script faz (cada passo está comentado nele):

1. Lista os `.java` de `src/` em `build/fontes.txt`.
2. Compila com `javac -encoding UTF-8 --release 11 -cp lib/mysql-connector-j-9.3.0.jar -d build/classes @build/fontes.txt` (`--release 11` = roda em qualquer Java 11 ou mais novo).
3. Copia `sql/netflix.sql` para dentro das classes (o `CriadorBanco` lê de lá).
4. Extrai o driver do MySQL para dentro (`jar xf`) e apaga o `MANIFEST.MF` e o `INDEX.LIST` dele (senão confundem o nosso jar).
5. Cria o manifesto `Main-Class: Main` e monta o jar: `jar cfm ../Netflix.jar build/manifest.txt -C build/classes .`.

Resultado: um único arquivo (~2,6 MB) com nossas classes, o driver e o script SQL.

---

## 11. Passo a passo para rodar e testar

### 11.1 Pré-requisitos (em cada computador)

- [ ] **Java 11 ou superior** (recomendado Java 17: https://adoptium.net). Confira com `java -version`.
- [ ] **MySQL Server** instalado e **ligado** (porta 3306). Saber o usuário (normalmente `root`) e a senha definidos na instalação.
- [ ] Não precisa de Workbench, nem de JDK, nem de editar código.

### 11.2 Abrir o sistema

1. Dê dois cliques em `Netflix.jar` (ou em `Abrir Netflix.bat`, que mostra mensagens de erro se algo faltar).
2. Na **tela de conexão**, confira servidor `localhost`, porta `3306`, usuário `root`, digite a **senha do seu MySQL** e clique em **Conectar**.
3. Na primeira vez o sistema cria o banco `netflix` com os dados de exemplo. Os dados ficam salvos em `config.properties` e na próxima vez a tela já vem preenchida.
4. Abre o **menu principal**.

> Antes da apresentação, clique em **Restaurar dados de exemplo** para voltar ao estado inicial.

### 11.3 Roteiro de testes (na ordem do menu)

**1. Planos**
1. **Listar:** ao abrir, aparecem Basico, Padrao e Premium.
2. **Inserir:** Nome `Familia`, Valor `29,90`, Qualidade `HD`, Telas `2`, Download `Sim` → "Plano inserido com sucesso!".
3. **Alterar:** clique na linha Familia, mude o valor para `34,90` → Alterar.
4. **Remover:** selecione Familia → Remover → Sim.
5. **FK:** tente eliminar **Premium** (tem conta) → "Não foi possível remover...".

**2. Contas**
1. Coluna **Plano** mostra "3 - Premium" (INNER JOIN), não só "3".
2. **Inserir:** escolha um plano, E-mail `teste@email.com`, Senha `123` → sucesso.
3. **E-mail único:** insira o mesmo e-mail de novo → erro (UNIQUE).
4. **País:** digite `BRA` → "O país deve ter 2 letras".
5. **Combo atualiza:** insira um plano na tela 1 com a tela 2 aberta; depois de qualquer ação na tela 2 o plano novo aparece no combo.

**3. Perfis**
1. Coluna **Conta** mostra "1 - ana.silva@email.com".
2. **Inserir** com PIN vazio → sucesso. PIN `12` → "O PIN deve ter exatamente 4 dígitos."

**4. Títulos**
1. Filme com Duração `120` → sucesso. Série com Duração **vazia** → sucesso e célula vazia (NULL).
2. Ano `abc` → "deve ser um número inteiro".

**5. Histórico de Visualização (CRUD principal + JOIN)**
1. Colunas Perfil e Título mostram **"1 - Ana"** e **"1 - Stranger Things"** (INNER JOIN).
2. **Inserir:** Perfil `3 - Kids`, Título `3 - Our Planet`, Minuto `30`, Dispositivo `Tablet`.
3. **Alterar:** clique na linha; os combos se posicionam sozinhos. Mude Minuto para `50` (a duração do título) e Concluído `Sim`.
4. **Datas automáticas:** Início e Última visualização não são digitadas; aparecem na tabela já preenchidas pelo banco (data e hora de agora).
5. **Remover** a linha criada.
6. **CASCADE:** em Perfis, elimine **Pedro**; volte ao Histórico: as linhas dele sumiram.

**6. Relatório** (botão **Relatório** da tela de Histórico)
1. Abre com o período 01/01/2025 até hoje: as 6 visualizações e os totais.
2. Período `01/06/2025` a `05/06/2025` → 3 linhas.
3. Perfil `1 - Ana` → só as visualizações da Ana.
4. Data final antes da inicial → erro de validação.
5. **Salvar .txt** → escolha onde salvar e abra o arquivo.

### 11.4 Resumo do que cada teste comprova

| Teste | O que prova |
|---|---|
| Conectar e o menu abrir | JDBC + criação automática do banco |
| Listar ao abrir | `SELECT` + `ResultSet` → objetos → `DefaultTableModel` |
| Inserir / Alterar / Remover | `PreparedStatement` com INSERT, UPDATE e DELETE |
| "3 - Premium" na tabela de Contas | INNER JOIN conta + plano |
| Remover plano em uso | integridade referencial (FK) |
| E-mail repetido | restrição `UNIQUE` |
| País com 3 letras, PIN com 2 dígitos, datas trocadas | regras de negócio no Controller |
| Série sem duração | tratamento de NULL (`Integer`, `setNull`, `wasNull`) |
| Histórico com nomes | INNER JOIN com 5 tabelas (histórico, perfil, título, conta e plano); a tabela mostra também as colunas Conta e Plano |
| Remover perfil e o histórico sumir | `ON DELETE CASCADE` |
| Relatório por período / perfil + totais + .txt | relatório da rotina principal |

### 11.5 Problemas comuns

| Sintoma | Causa e solução |
|---|---|
| Dois cliques no jar não fazem nada | Java não instalado ou o `.jar` não está associado ao Java. Use o `Abrir Netflix.bat` para ver a mensagem |
| "Não foi possível conectar ao MySQL" | MySQL desligado (Serviços do Windows → MySQL80 → Iniciar) ou senha errada |
| Quero voltar aos dados originais | Menu → **Restaurar dados de exemplo** |
| Quero trocar a senha salva | A tela de conexão aparece sempre ao abrir; ou apague o `config.properties` |
| Erro de data | Só o Relatório pede data: `dd/mm/aaaa` (ex.: 31/12/2026) |
| Mudei o código e nada mudou | Rode `Netflix/build.bat` para gerar o jar de novo |

---

## 12. Perguntas prováveis do professor (com respostas)

Cada aluno responde **2 perguntas de 0,5 ponto**, individualmente, sobre **o código desenvolvido**. **Como responder bem:** (1) diga o que a classe/método faz; (2) aponte o trecho; (3) explique **por que**; (4) nomeie o conceito de POO.

### 12.1 Fluxo geral e execução

| Pergunta | Resposta |
|---|---|
| Explique o caminho do clique em **Inserir** até o banco. | `btnInserir` → `mostrarResultado(inserir())` (`TelaCrud`) → `TelaPlano.inserir()` lê os campos com `valor(...)` → `PlanoController.inserir(...)` converte com o `Conversor` e monta um `Plano` → `PlanoDAO.inserir(plano)` pega a conexão da `ConnectionFactory`, prepara o INSERT e executa → volta a mensagem → `JOptionPane` e `recarregar()`. |
| Por que a tela não chama o `PlanoDAO` direto? | MVC: a tela só cuida da interface; validação e regras no Controller; SQL no DAO. Trocar o banco ou a tela não quebra o resto. |
| Onde fica o `main`? | Em `Main.java`: `SwingUtilities.invokeLater(() -> new TelaConexao().setVisible(true))`. O manifesto do jar aponta `Main-Class: Main`. |
| Qual é o CRUD principal e por quê? | `historico_visualizacao` (`TelaHistorico`, `HistoricoController`, `HistoricoVisualizacaoDAO`): é a rotina central (o que cada perfil assiste) e relaciona perfil e título por duas FKs. |
| Qual é o relatório e por que ele é da rotina principal? | "Visualizações por período (e por perfil)": `TelaRelatorio` → `RelatorioController.gerar` → `HistoricoVisualizacaoDAO.listarPorPeriodo`. Trabalha com o histórico (CRUD principal), com JOIN, filtros e totais. |
| Como o banco é criado? | `ConexaoController.conectar` chama `CriadorBanco.criarSeNaoExistir()`, que consulta `information_schema.schemata` e, se o banco não existe, lê o `netflix.sql` de dentro do jar e executa comando por comando (`split(";")` + `Statement.execute`). |
| O que o `build.bat` faz? | Compila `src/` com o driver no classpath, copia o `netflix.sql`, extrai o driver para dentro e monta o `Netflix.jar` com `Main-Class: Main`. |

### 12.2 Banco de dados

| Pergunta | Resposta |
|---|---|
| Por que o script começa com `DROP DATABASE IF EXISTS`? | Para poder rodar várias vezes (botão Restaurar): apaga o banco antigo e recria do zero. `IF EXISTS` evita erro na primeira vez. |
| Por que a ordem plano, conta, perfil, titulo, historico? | Uma tabela só pode referenciar (FK) outra que já existe. Os INSERT seguem a mesma ordem. |
| Quais são as PKs e FKs? | 5 PKs (`id_plano`, `id_conta`, `id_perfil`, `id_titulo`, `id_visualizacao`) e 4 FKs: `conta.id_plano`, `perfil.id_conta`, `historico_visualizacao.id_perfil` e `.id_titulo`. |
| Qual tabela é associativa? | `historico_visualizacao`: resolve o N:N entre perfil e título e guarda dados da sessão. |
| O que acontece ao apagar o plano Premium? | A FK sem CASCADE impede (há conta usando); a `SQLException` vira "Não foi possível remover...". |
| O que acontece ao apagar o perfil Pedro? | `ON DELETE CASCADE` apaga junto o histórico dele. |
| Por que CASCADE só no histórico? | Histórico sem perfil/título não faz sentido; nos outros preferimos **bloquear** para não apagar dados sem querer. |
| Por que `DECIMAL(6,2)` e `BigDecimal`? | Dinheiro precisa de decimal exato; FLOAT/double arredondam. |
| Por que `TINYINT(1)`? | O MySQL não tem booleano; guarda 0/1 e vira `boolean` no Java. |
| Por que `ENUM`? | Só aceita valores de uma lista; os combos oferecem exatamente os mesmos valores. |
| O que é `ENGINE=InnoDB`? | O motor que suporta FKs e transações. |

### 12.3 `util`

| Pergunta | Resposta |
|---|---|
| Por que existe a `ConnectionFactory`? | Concentra os dados de conexão em um lugar; os DAOs só chamam `getConnection()`. |
| Onde fica a senha do MySQL na versão principal? | Não fica no código: a `TelaConexao` pede, a `ConnectionFactory` guarda nos atributos estáticos e grava no `config.properties` (`Properties.store`). Na próxima vez é lida com `Properties.load`. |
| Por que o construtor da `ConnectionFactory` é `private`? | Só tem métodos `static`; impede `new ConnectionFactory()`. |
| Onde o driver é carregado, se não tem `Class.forName`? | Desde o JDBC 4 o `DriverManager` carrega sozinho o driver que está no classpath (arquivo `META-INF/services/java.sql.Driver` do conector, que vai dentro do jar). |
| Por que `getConnection()` declara `throws SQLException`? | O erro de conexão segue o mesmo caminho de qualquer erro de banco: DAO repassa, Controller captura e devolve a mensagem. |
| Por que o `CriadorBanco` usa `Statement` e não `PreparedStatement`? | O script não tem valores vindos do usuário (sem risco de SQL Injection) e não tem `?`. |
| Por que o `split(";")`? | O JDBC executa um comando por vez; o script tem 13 comandos separados por `;`. |
| O que faz `Conversor.opcao(3, "Premium")`? | Devolve `"3 - Premium"`, usado no combo e na tabela. `extrairId` faz o inverso (pega o 3 para gravar a FK). |
| Por que `"Sim".equals(texto)`? | Se `texto` for `null` não dá `NullPointerException`. |
| Por que `paraIntOuNulo` devolve `Integer`? | Precisa poder devolver `null` (duração vazia de série). |

### 12.4 `model`

| Pergunta | Resposta |
|---|---|
| Por que os atributos são `private`? | Encapsulamento: acesso só por `getX`/`setX`. |
| Por que dois construtores? | O vazio é usado pelo DAO ao ler do banco; o completo pelo Controller. É sobrecarga. |
| Por que o construtor completo não recebe o id? | O id é gerado pelo `AUTO_INCREMENT`; quem cria o número é o banco, e o código não precisa dele de volta. |
| Para que serve o `this`? | Diferencia o atributo (`this.nome`) do parâmetro (`nome`). |
| O que são `nomePlano`, `emailConta`, `nomePerfil`, `nomeTitulo`? | Campos só de exibição, preenchidos pelos SELECT com JOIN. Não existem nas tabelas e não são gravados. |
| Por que `Conta` guarda `int idPlano` e não um `Plano`? | Espelha a tabela (só a FK). O nome vem pelo JOIN quando a tela precisa. |

### 12.5 `dao`

| Pergunta | Resposta |
|---|---|
| Explique `PlanoDAO.inserir` linha a linha. | INSERT com 7 `?`; abre `Connection` e `PreparedStatement` no `try`; preenche cada `?` na ordem; `executeUpdate()`. |
| Como a descrição do plano é gerada automaticamente? | O Controller tem `gerarDescricao(telas, qualidade)`: converte o número de telas em palavras ("Uma tela", "Duas telas", "Três telas", "Quatro telas"; acima disso "5 telas") e junta com ", qualidade " + a qualidade. Ex.: `("2", "Full HD")` → "Duas telas, qualidade Full HD". |
| Por que a descrição é gerada no Controller e não na tela? | É regra de negócio (como o texto é montado). A tela só **pede** o texto ao Controller (`controller.gerarDescricao(...)`) para mostrar ao vivo; ao salvar, o próprio Controller gera de novo em `montar(...)`, então a descrição gravada nunca fica diferente das telas e da qualidade. |
| Como a descrição aparece ao vivo na tela de planos? | `comboQualidade.addActionListener(...)` e `comboTelas.addActionListener(...)` chamam `atualizarDescricao()` sempre que a qualidade ou as telas mudam (inclusive quando o código seleciona o item ao clicar numa linha). O campo de descrição é `setEditable(false)`. |
| E se o UPDATE/DELETE ficar sem `WHERE`? | Alteraria/apagaria **todos** os registros. |
| Por que `PreparedStatement`? | Previne SQL Injection: comando e dados vão separados. |
| O que é o try-with-resources? | Fecha `ps` e `con` automaticamente ao sair do bloco, mesmo com erro. |
| O que faz `while (rs.next())`? | Avança o cursor uma linha por vez; em cada volta cria um objeto. |
| Explique o JOIN de `listarComNomes()`. | `FROM historico_visualizacao h INNER JOIN perfil p ON p.id_perfil = h.id_perfil INNER JOIN titulo t ON t.id_titulo = h.id_titulo INNER JOIN conta c ON c.id_conta = p.id_conta INNER JOIN plano pl ON pl.id_plano = c.id_plano`, com `p.nome AS nome_perfil, t.nome AS nome_titulo, c.email AS email_conta, pl.nome AS nome_plano`, ordenado pela última visualização. |
| Por que `INNER JOIN` e não `LEFT JOIN`? | Só traz linhas com correspondência; as FKs garantem que perfil e título existem, então nenhuma se perde. |
| Como funciona o filtro opcional do relatório? | `if (idPerfil > 0) sql += "AND h.id_perfil = ? ";` e só nesse caso `ps.setInt(3, idPerfil)`. 0 significa "Todos". |
| O que faz `DATE(h.data_inicio) BETWEEN ? AND ?`? | `DATE()` tira a hora; `BETWEEN` inclui as duas datas. |
| Para que serve a constante `SELECT_COM_NOMES`? | O SELECT com JOIN é igual nas duas consultas (`listarComNomes` e `listarPorPeriodo`); escrevemos uma vez só e cada método acrescenta o final (`WHERE`/`ORDER BY`). |
| Por que `setNull(6, Types.INTEGER)` na duração? | `setInt` não aceita `null`; `setNull` grava NULL informando o tipo. |
| O que é `rs.wasNull()`? | `getInt` devolve 0 para NULL; `wasNull()` diz se era NULL de verdade. |
| Por que o DAO declara `throws SQLException`? | Só acessa dados; o Controller decide a mensagem (MVC). |

### 12.6 `controller`

| Pergunta | Resposta |
|---|---|
| Por que os métodos devolvem `String`? | A View só precisa do texto para mostrar. |
| Por que dois `catch`? | `IllegalArgumentException` = validação (antes do banco); `SQLException` = erro do banco (inclui MySQL desligado). |
| Onde estão as regras de negócio? | `ContaController.montar` (país 2 letras), `PerfilController.montar` (PIN 4 dígitos), `HistoricoController.montar` (datas e minuto), `RelatorioController.gerar` (período válido) e `resumo` (totais). |
| Por que os totais do relatório ficam no Controller? | São regra de negócio (contar concluídas, somar minutos). A tela só mostra. |
| O que `ConexaoController.conectar` devolve? | `null` se deu certo; senão a mensagem de erro. A `TelaConexao` testa `if (erro == null)`. |
| Por que `listar()` lança `RuntimeException`? | Devolve uma lista, não uma mensagem; a `TelaCrud.recarregar()` captura e mostra. |

### 12.7 `view`

| Pergunta | Resposta |
|---|---|
| Por que `TelaCrud` é `abstract`? | É só o esqueleto; obriga as filhas a implementar `carregarTabela`, `inserir`, `atualizar`, `eliminar`. |
| Onde estão herança e polimorfismo? | `TelaPlano extends TelaCrud extends JFrame`; a `TelaCrud` chama `inserir()` e roda a versão da tela aberta. |
| O que é o Template Method? | A base define o passo a passo e deixa "buracos" (abstratos e ganchos) para as filhas. |
| Por que `iniciar()` não está no construtor da `TelaCrud`? | No construtor da base o `controller` da filha ainda não existe. |
| Como a tabela é preenchida? | `carregarTabela()` da filha pega a lista do Controller e faz `modelo.addRow(new Object[]{...})`. |
| Como os botões estão ligados? | `btnInserir.addActionListener(e -> mostrarResultado(inserir()));`: a lambda é o `ActionListener`; o clique só chama o método. |
| Como clicar numa linha preenche o formulário, inclusive os combos de FK? | `aoSelecionar()` copia as células para os campos. Como tabela e combo usam o mesmo texto `"3 - Premium"`, `setSelectedItem` acha o item. |
| Por que os combos de FK são atributos e começam vazios? | São preenchidos em `carregarCombos()`, chamado em todo `recarregar()`; assim sempre mostram os dados atuais do banco. |
| Por que `TelaRelatorio` não herda de `TelaCrud`? | Não é um cadastro (não tem inserir/atualizar/eliminar); herda direto de `JFrame`. |
| Para que serve o `JFileChooser`? | Abre a janela "Salvar como" para escolher onde gravar o `.txt`. |
| `DISPOSE_ON_CLOSE` x `EXIT_ON_CLOSE`? | O primeiro fecha só a janela (telas de CRUD); o segundo encerra o programa (menu e tela de conexão). |
| Por que `SwingUtilities.invokeLater`? | O Swing exige criar a interface na Event Dispatch Thread. |
| Como adicionar um 6º CRUD? | Model, DAO, Controller, uma tela `extends TelaCrud` com os 4 métodos e um botão no menu. |

### 12.8 "Mostre no código" (prepare estes trechos)

| Pedido | Abra |
|---|---|
| Um CRUD completo no DAO | `PlanoDAO` |
| O INNER JOIN | `HistoricoVisualizacaoDAO.SELECT_COM_NOMES` / `listarComNomes()` (e `ContaDAO.listar()`) |
| O relatório | `TelaRelatorio.gerar()`, `RelatorioController.gerar()` e `resumo()`, `HistoricoVisualizacaoDAO.listarPorPeriodo()` |
| Onde a conexão é feita | `ConnectionFactory.getConnection()` |
| Como o banco é criado | `CriadorBanco.recriarBanco()` |
| Proteção contra SQL Injection | qualquer DAO: `?` no SQL e `ps.setString(...)` |
| Tratamento de NULL | `TituloDAO.preencher` (`setNull`) e `TituloDAO.listar` (`wasNull`) |
| Herança e polimorfismo | `TelaPlano extends TelaCrud`; métodos abstratos da `TelaCrud` |
| Encapsulamento | atributos `private` + `getX`/`setX` em qualquer model |
| Sobrecarga e sobrescrita | construtores do `Plano`; `definirValor` (sobrecarga); `carregarTabela` nas filhas (sobrescrita) |
| Onde a tabela é preenchida | `carregarTabela()` e `modelo.addRow(...)` |
| Os botões e eventos | `TelaCrud.iniciar()` (`addActionListener`) |
| Regras de negócio | `HistoricoController.montar`, `ContaController.montar`, `PerfilController.montar`, `RelatorioController.gerar` |
| PK, FK e relacionamentos | `sql/netflix.sql` |

> **Dica para a defesa:** treine abrir `HistoricoVisualizacaoDAO`, `PlanoDAO`, `TelaCrud`, `TelaPlano` e `RelatorioController` e explicar linha por linha com as seções 5, 6, 7 e 8. Responda sempre ligando o conceito (ex.: polimorfismo) a um exemplo concreto do projeto.

---

## 13. Correções feitas depois da revisão do código (30/10/2026)

Uma revisão "adversarial" (que tenta quebrar o sistema) achou 9 falhas (a de valores sem sentido tinha duas partes: o valor do plano e o minuto de parada, por isso a tabela tem 10 linhas). Todas foram reproduzidas com testes contra um MySQL de verdade, corrigidas e testadas de novo.

| # | O que estava errado | Como ficou | Onde |
|---|---|---|---|
| 1 | Qualquer erro (ex.: data inválida) **apagava o formulário inteiro** | O formulário só é limpo quando deu certo. Depois de um erro ficam o texto digitado, a escolha dos combos e a linha selecionada | `TelaCrud.mostrarResultado`, `recarregar(boolean)` |
| 2 | Se o `config.properties` não pudesse ser gravado (pasta protegida), **ninguém conseguia entrar** | Entra normalmente e mostra um aviso. O `ConexaoController` só lê o arquivo na 1ª vez, para o menu não trocar a senha nova pela antiga | `ConexaoController.conectar`, `TelaConexao` |
| 3 | "Salvar .txt" juntava as linhas de um relatório com as datas de outro | Guarda os filtros usados na última geração e salva com eles | `TelaRelatorio.gerar`, `salvar` |
| 4 | Num computador com outro fuso horário os horários apareciam deslocados (20:00 virava 23:00) | `connectionTimeZone=LOCAL` na URL: o horário digitado é o mostrado em qualquer fuso | `ConnectionFactory.montarUrl` |
| 5 | Um banco `netflix` já existente mas **vazio** nunca recebia as tabelas | Se o banco não existe ou está sem tabelas, é criado. Banco com tabelas continua sendo reaproveitado (nunca apagado) | `CriadorBanco.criarSeNaoExistir`, `bancoSemTabelas` |
| 6 | Plano com valor negativo ou zero era aceito | "O valor mensal deve ser maior que zero." | `PlanoController.montar` |
| 7 | Remover/atualizar um registro que já não existe dizia "sucesso" | Os DAOs devolvem `true/false` (`executeUpdate() > 0`) e o Controller avisa "Registro não encontrado" | os 5 DAOs e controllers, `Conversor.REGISTRO_NAO_ENCONTRADO` |
| 8 | Minuto de parada maior que a duração do título era aceito | "O minuto de parada (X) não pode passar da duração do título (Y min)." Séries (sem duração) aceitam qualquer minuto. Concluído e minuto também precisam concordar: "Sim" exige o último minuto e o último minuto exige "Sim" | `HistoricoController.montar` |
| 9 | Erro de conta vinha sempre com a dica "(e-mail já cadastrado?)" | Código 1062 → "Já existe uma conta com esse e-mail."; 1452 → "O plano escolhido não existe mais."; outros erros mostram a mensagem do banco | `ContaController.mensagemDeErro` |
| 10 | "Remover" sem linha selecionada perguntava "Remover o registro?" antes de avisar | Avisa "Selecione um registro" direto | `TelaCrud.confirmarEliminar` |

**Como foi testado:** 70 verificações das camadas (todas passaram), as 5 telas, o menu e o relatório, mais 39 verificações novas, uma ou mais para cada correção, incluindo cliques simulados. A do fuso horário rodou em três fusos: São Paulo, UTC e Tóquio.

