<p align="center">
  <img src="assets/netflix-logo.png" alt="Logo da Netflix" width="300">
</p>

<h1 align="center">Sistema de Gerenciamento Netflix</h1>

<p align="center">
  Sistema desktop em <b>Java</b> com <b>Swing</b>, <b>MySQL</b> e <b>JDBC</b>, organizado em <b>MVC</b>.<br>
  Trabalho final de Programação Orientada a Objetos · Católica de Brasília
</p>

<p align="center">
  <img alt="Java 11+" src="https://img.shields.io/badge/Java-11%2B-orange">
  <img alt="Swing" src="https://img.shields.io/badge/Interface-Swing%20%2F%20JFrame-blue">
  <img alt="MySQL 8" src="https://img.shields.io/badge/Banco-MySQL%208-00758f">
  <img alt="JDBC" src="https://img.shields.io/badge/Acesso-JDBC-lightgrey">
  <img alt="MVC" src="https://img.shields.io/badge/Arquitetura-MVC%20%2B%20DAO-success">
</p>

> **Projeto exclusivamente acadêmico e sem fins lucrativos.** Foi feito para a disciplina de POO. Nomes, marcas, pôsteres e imagens (como "Netflix" e as capas dos títulos) pertencem aos seus respectivos donos e são usados apenas para ilustrar o trabalho; não há venda, anúncio nem qualquer uso comercial.

---

## Sumário

1. [A proposta do professor](#1-a-proposta-do-professor)
2. [A nossa proposta](#2-a-nossa-proposta)
3. [Como o sistema funciona](#3-como-o-sistema-funciona)
4. [Arquitetura](#4-arquitetura)
5. [Banco de dados](#5-banco-de-dados)
6. [Tecnologias](#6-tecnologias)
7. [Como executar](#7-como-executar)
8. [Estrutura do repositório](#8-estrutura-do-repositório)
9. [Extras: contexto e versão com front-end](#9-extras-contexto-e-versão-com-front-end)
10. [Declaração de Uso de Inteligência Artificial](#10-declaração-de-uso-de-inteligência-artificial)
11. [Autores](#11-autores)

---

## 1. A proposta do professor

A atividade pede um **sistema desktop completo em Java**, com uma finalidade real, e não apenas telas de cadastro. Cada grupo escolhe um tema (clínica, oficina, hotel, biblioteca...) e define uma **rotina principal**, a funcionalidade central do sistema. O enunciado completo está em [`extras/contexto/Proposta`](extras/contexto/Proposta/).

**Estrutura mínima obrigatória**

| Item | O que se espera |
|---|---|
| **4 CRUDs simples** | Entidades básicas, cada uma com inserir, listar, alterar e excluir |
| **1 CRUD principal** | Ligado à finalidade do sistema, relacionando dados de outras tabelas por chaves estrangeiras |
| **1 consulta com JOIN** | Mostrar informações compreensíveis (nomes), e não só códigos de chave estrangeira |
| **1 relatório** | Obrigatoriamente ligado ao CRUD principal (por exemplo, por período) |
| **Tecnologias** | Java, POO, Swing/JFrame, MySQL, JDBC e MVC |
| **Banco de dados** | Tabelas, chaves primárias e estrangeiras, relacionamentos, dados de demonstração e o script SQL de criação |

**Regras importantes**
- A lógica **não pode ficar concentrada nos eventos dos botões** do JFrame: é preciso separar Model, View e Controller.
- O grupo deve demonstrar o fluxo completo: **Cadastro → Relacionamento → Rotina principal → Consulta com JOIN → Relatório**.

**Avaliação (2,5 pontos)**

| Parte | Pontos |
|---|---|
| 4 CRUDs simples funcionando | 0,40 |
| CRUD da rotina principal | 0,40 |
| Banco MySQL, relacionamentos e JOIN | 0,25 |
| POO e padrão MVC | 0,20 |
| Relatório da rotina principal | 0,15 |
| Organização e funcionamento geral | 0,10 |
| **Apresentação individual** (2 perguntas de 0,5 ponto por aluno) | **1,0**, já incluído nos 2,5 |

---

## 2. A nossa proposta

**Tema:** gerenciamento de um recorte de uma plataforma de streaming, inspirado na Netflix.

**Rotina principal:** o **Histórico de Visualização**, que registra *qual perfil assistiu a qual título, quando, até que minuto, em qual dispositivo e se concluiu*. É ele que liga as demais entidades e dá sentido ao sistema.

| Exigência da proposta | Como atendemos |
|---|---|
| 4 CRUDs simples | **Planos**, **Contas**, **Perfis** e **Títulos** |
| CRUD principal | **Histórico de Visualização**, com chaves estrangeiras para Perfil e Título |
| Consulta com JOIN | `INNER JOIN` de **5 tabelas** (histórico, perfil, título, conta e plano), mostrando nomes, e-mail e plano |
| Relatório da rotina principal | **Visualizações por período**, com filtro por perfil, totais e exportação para `.txt` |
| Java + POO | Herança, classe abstrata, polimorfismo, encapsulamento e sobrescrita |
| Swing / JFrame | Todas as telas herdam de `JFrame` (pacote `view`) |
| MVC | Pacotes `model`, `view` e `controller`, mais `dao` para o SQL |
| MySQL + JDBC + script | `Netflix/sql/netflix.sql`, com PK, FK e dados de exemplo, executado pelo próprio sistema |

---

## 3. Como o sistema funciona

### Fluxo de uso

```
Conexão com o MySQL → Início (menu) → Cadastros → Histórico (rotina principal) → Relatório
```

1. **Conexão:** ao abrir, o usuário informa servidor, porta, usuário e senha do MySQL. Se o banco `netflix` não existir, o sistema **o cria sozinho**, com tabelas e dados de exemplo. Os dados de conexão ficam salvos em `config.properties`, e a tela já abre preenchida nas próximas vezes.
2. **Início:** menu com um botão para cada tela e a opção **Restaurar dados**. Uma barra de navegação no topo aparece em todas as telas, e a troca entre elas acontece na mesma janela, com um fade curto.
3. **Cadastros:** Planos, Contas, Perfis e Títulos, todos com a mesma tela: formulário, tabela e os botões **Inserir**, **Alterar**, **Remover** e **Limpar**. Clicar numa linha da tabela leva os dados para o formulário.
4. **Histórico de Visualização:** registra o que cada perfil assistiu. A tabela exibe nomes (perfil, título, conta e plano) em vez de códigos, graças ao JOIN.
5. **Relatório:** aberto pelo botão **Relatório** da tela de Histórico (ao lado de Inserir, Alterar, Remover e Limpar), onde o usuário escolhe data inicial, data final e, se quiser, um perfil; o sistema lista as visualizações do período, mostra o total e permite **salvar em `.txt`**.

### Telas

| Tela | Função |
|---|---|
| Conexão | Conecta ao MySQL, cria o banco na primeira vez e salva a configuração |
| Início | Menu principal, restauração dos dados de exemplo |
| Planos | CRUD de planos (nome, valor, qualidade, telas simultâneas, download). A descrição é gerada automaticamente |
| Contas | CRUD de contas; cada conta pertence a um plano |
| Perfis | CRUD de perfis; cada perfil pertence a uma conta (com perfil infantil e PIN) |
| Títulos | CRUD do catálogo (filmes, séries, documentários e stand-ups) |
| Histórico | CRUD principal: perfil, título, datas, minuto de parada, conclusão e dispositivo |
| Relatório | Aberto pelo botão **Relatório** do Histórico. Visualizações por período e por perfil, com resumo e exportação |

### Regras de negócio e validações

As regras ficam no **Controller** (e na conversão de textos em `util/Conversor`), nunca nos botões. Quando algo está errado, o Controller devolve uma mensagem clara e o formulário **não é apagado**.

- Valor mensal do plano maior que zero e pelo menos 1 tela simultânea.
- E-mail da conta único, país com 2 letras e plano existente.
- PIN do perfil com exatamente 4 dígitos.
- Datas em formato brasileiro (`dd/mm/aaaa`); só o relatório pede data. As datas de cadastro e as do histórico (início e última visualização) são gravadas pelo próprio MySQL.
- O minuto de parada não pode ser negativo nem passar da duração do título (séries não têm duração e ficam com `NULL`).
- No relatório, a data final não pode ser anterior à inicial.

### Integridade dos dados

- **Plano com contas** e **conta com perfis** não podem ser eliminados: o MySQL recusa (erro 1451) e o sistema explica o motivo.
- **Perfil** ou **título** eliminados levam o **histórico** junto (`ON DELETE CASCADE`), e a mensagem avisa.
- Cada ação mostra uma mensagem de resultado, e erros de conexão ou de banco são tratados sem derrubar o programa.

---

## 4. Arquitetura

O projeto segue o **MVC**, com uma camada **DAO** para isolar o SQL:

```
View (telas Swing)  →  Controller (valida e aplica regras)  →  DAO (SQL com PreparedStatement)  →  MySQL
                     ←  mensagem de texto  ←                 ←  objetos do Model  ←
```

| Pacote | Responsabilidade | Classes |
|---|---|---|
| `model` | Dados do domínio, com atributos privados, getters e setters | `Plano`, `Conta`, `Perfil`, `Titulo`, `HistoricoVisualizacao` |
| `view` | Interface Swing/JFrame. Só captura dados e mostra resultados | `TelaConexao`, `TelaPrincipal`, `TelaCrud` (abstrata), `TelaPlano`, `TelaConta`, `TelaPerfil`, `TelaTitulo`, `TelaHistorico`, `TelaRelatorio`, `BarraNavegacao`, `Navegador` |
| `controller` | Regras de negócio e validação; devolve mensagens para a tela | `PlanoController`, `ContaController`, `PerfilController`, `TituloController`, `HistoricoController`, `RelatorioController`, `ConexaoController` |
| `dao` | Acesso ao banco com `PreparedStatement` e `try-with-resources` | `PlanoDAO`, `ContaDAO`, `PerfilDAO`, `TituloDAO`, `HistoricoVisualizacaoDAO` |
| `util` | Apoio: conexão, conversões e criação do banco | `ConnectionFactory`, `Conversor`, `CriadorBanco` |

**Conceitos de POO aplicados**
- **Herança e classe abstrata:** `TelaCrud` define o layout e o fluxo comuns, e as cinco telas de cadastro herdam dela e só informam seus campos e chamam o seu Controller (padrão *Template Method*).
- **Polimorfismo:** o botão da `TelaCrud` chama `inserir()` sem saber qual tela está aberta; o Java executa o método da filha.
- **Encapsulamento:** atributos privados nos modelos, acessados por getters e setters.
- **Separação de responsabilidades:** nenhum botão tem SQL nem regra de negócio.

---

## 5. Banco de dados

Script: [`Netflix/sql/netflix.sql`](Netflix/sql/netflix.sql). Modelo lógico do grupo em [`Banco de dados/`](Banco%20de%20dados/).

```
plano 1 ──── N conta 1 ──── N perfil N ──── N titulo
                                  └── historico_visualizacao ──┘
```

| Tabela | Chave primária | Chaves estrangeiras |
|---|---|---|
| `plano` | `id_plano` | — |
| `conta` | `id_conta` | `id_plano` → `plano` |
| `perfil` | `id_perfil` | `id_conta` → `conta` |
| `titulo` | `id_titulo` | — |
| `historico_visualizacao` | `id_visualizacao` | `id_perfil` → `perfil` e `id_titulo` → `titulo` (ambas com `ON DELETE CASCADE`) |

O histórico é a **tabela associativa** entre perfil e título (relação N:N). O script também traz os **dados de exemplo**: planos Básico, Padrão e Premium, contas, perfis, títulos e registros de visualização. A consulta com JOIN está em `HistoricoVisualizacaoDAO`:

```sql
SELECT h.*, p.nome AS nome_perfil, t.nome AS nome_titulo,
       c.email AS email_conta, pl.nome AS nome_plano
FROM historico_visualizacao h
INNER JOIN perfil p  ON p.id_perfil = h.id_perfil
INNER JOIN titulo t  ON t.id_titulo = h.id_titulo
INNER JOIN conta c   ON c.id_conta  = p.id_conta
INNER JOIN plano pl  ON pl.id_plano = c.id_plano;
```

---

## 6. Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 11 ou superior | Linguagem (testado com Java 17) |
| Swing / JFrame | Interface gráfica (`JTable` com `DefaultTableModel`) |
| Swing UI Designer (IntelliJ) | Editor visual das telas: cada tela tem um arquivo `.form` ao lado do `.java` (`forms_rt.jar` em `Netflix/lib`) |
| MySQL 8 | Banco de dados relacional |
| JDBC (`mysql-connector-j 9.3.0`) | Comunicação entre Java e MySQL |
| MVC + DAO | Arquitetura em camadas |

---

## 7. Como executar

**Você precisa de:**
1. **Java 11 ou mais novo** (recomendado o 17, em [adoptium.net](https://adoptium.net)).
2. **MySQL Server** instalado e ligado, e a **senha do usuário `root`**.

Não é preciso Workbench, IDE nem compilar nada.

**Passo a passo**
1. Baixe o repositório (**Code → Download ZIP**) e descompacte.
2. Dê dois cliques em **`Netflix.jar`**. Se nada acontecer, use **`Abrir Netflix.bat`**, que mostra o erro.
3. Na tela **Conectar ao MySQL**, digite a senha e clique em **Conectar**. Na primeira vez o banco é criado sozinho, com dados de exemplo.
4. Navegue pelo menu. Para voltar os dados ao estado inicial, use **Restaurar dados**.

**Mudou o código?** Rode `Netflix/build.bat` (Windows) ou `bash Netflix/build.sh` (Linux/macOS). Precisa do **JDK**. O script compila, copia o SQL, o driver e o `forms_rt` para dentro do `.jar` e gera o `Netflix.jar` na raiz.

**Editando o visual das telas:** no IntelliJ, abra o `.form` de uma tela (ex.: `Netflix/src/view/TelaConta.form`) para mexer em textos, itens dos combos e posição dos campos de forma visual. Ative uma vez em *Settings → Editor → GUI Designer → Generate GUI into: **Java source code***: assim o IntelliJ atualiza o código dentro do `.java` e o `build.bat` continua funcionando sem a IDE. Não edite o método `$$$setupUI$$$` na mão.

**Abrindo em uma IDE (IntelliJ, NetBeans, Eclipse):** adicione `Netflix/lib/mysql-connector-j-9.3.0.jar` e `Netflix/lib/forms_rt.jar` às bibliotecas do projeto e execute a classe `Main`. Sem o driver, a conexão falha com *No suitable driver found*, mesmo com a senha correta.

| Sintoma | Solução |
|---|---|
| Dois cliques no `.jar` não abrem nada | Use `Abrir Netflix.bat`; normalmente falta instalar o Java |
| "Não foi possível conectar ao MySQL" | Ligue o MySQL (Serviços do Windows → `MySQL80` → Iniciar) e confira a senha |
| *No suitable driver found* na IDE | Adicione o `.jar` de `Netflix/lib` às bibliotecas |
| Dados bagunçados | Início → **Restaurar dados** |

---

## 8. Estrutura do repositório

```
README.md
Netflix.jar              Build pronta (dois cliques)
Abrir Netflix.bat        Abre o jar e mostra erros, se houver
Netflix/
  src/                   Main e pacotes model, view (.java + .form), controller, dao e util
  sql/netflix.sql        Script do banco (tabelas, PK, FK e dados de exemplo)
  lib/                   Driver JDBC do MySQL e runtime do Swing UI Designer (forms_rt)
  build.bat, build.sh    Geram o Netflix.jar de novo
Banco de dados/          Modelo lógico (.mwb e .pdf)
assets/                  Imagem usada neste README
extras/                  Material de apoio (ver abaixo)
```

---

## 9. Extras: contexto e versão com front-end

A pasta [`extras/`](extras/) reúne o material de apoio, que não é necessário para rodar o sistema. Ela tem duas partes: a pasta `contexto/`, com tudo o que explica e documenta o trabalho, e a versão alternativa com front-end.

```
extras/
  contexto/
    Proposta/        Enunciado do professor (PDF)
    Banco de Dados/  Modelo lógico completo do banco (PDF)
    Documentacao/    Documentação técnica do código (.md e .docx)
    Obsidian/        Vault de estudo do código (.zip)
    Roteiro/         Roteiro da apresentação (.md e .docx)
  Versão com front-end.zip
```

| Local | Arquivo | Conteúdo |
|---|---|---|
| [`extras/contexto/Proposta/`](extras/contexto/Proposta/) | [`Proposta - Trabalho Pratico POO Final.pdf`](extras/contexto/Proposta/Proposta%20-%20Trabalho%20Pratico%20POO%20Final.pdf) | **O que o professor pediu.** Enunciado oficial, com a estrutura mínima obrigatória e os critérios de avaliação. Está resumido na seção 1 |
| [`extras/contexto/Banco de Dados/`](extras/contexto/Banco%20de%20Dados/) | [`modelo_logico_netflix.pdf`](extras/contexto/Banco%20de%20Dados/modelo_logico_netflix.pdf) | **Como o banco se organiza.** Diagrama do modelo lógico completo da Netflix, com 13 entidades, suas chaves primárias e estrangeiras e os relacionamentos 1:1, 1:N e N:N. O sistema entregue implementa um recorte dele (Plano, Conta, Perfil, Título e Histórico de Visualização), cujo script está em `Netflix/sql/netflix.sql` |
| [`extras/contexto/Documentacao/`](extras/contexto/Documentacao/) | [`DOCUMENTACAO.md`](extras/contexto/Documentacao/DOCUMENTACAO.md) e [`DOCUMENTACAO.docx`](extras/contexto/Documentacao/DOCUMENTACAO.docx) | **Como o código funciona.** Explica cada pacote e classe (banco, `util`, `model`, `dao`, `controller` e `view`), a integração entre Java e MySQL, os conceitos de POO e o roteiro de testes. O `.md` é a versão completa de estudo, com as perguntas prováveis do professor. O `.docx` é uma versão formatada, com capa e sumário, própria para enviar |
| [`extras/contexto/Obsidian/`](extras/contexto/Obsidian/) | [`Obsidian-NetflixPOO.zip`](extras/contexto/Obsidian/Obsidian-NetflixPOO.zip) | **Notas de estudo.** Vault do Obsidian com as notas do código em formato navegável. Descompacte e abra a pasta como vault (*Open folder as vault*) |
| [`extras/contexto/Roteiro/`](extras/contexto/Roteiro/) | [`ROTEIRO-APRESENTACAO.md`](extras/contexto/Roteiro/ROTEIRO-APRESENTACAO.md) e [`ROTEIRO-APRESENTACAO.docx`](extras/contexto/Roteiro/ROTEIRO-APRESENTACAO.docx) | **Como apresentar.** Divide a apresentação entre as três pessoas do grupo (Plano e Conta, Perfil e Título, Histórico e Relatório), com resposta de abertura, classes e linhas de código, perguntas prováveis e um mapa para localizar cada peça no código. O `.docx` é a mesma versão para leitura |
| [`extras/`](extras/) | [`Versão com front-end.zip`](extras/Vers%C3%A3o%20com%20front-end.zip) | A mesma aplicação e a mesma lógica (model, DAO, controller e util), com a **interface personalizada**: tema escuro, tela Início com destaque e capas dos títulos, e abas de cadastro com visão **Visual** (cartões e avatares) ou **Tabela**. Descompacte e abra o `Netflix.jar`; os pôsteres ficam em `capas/`, e se uma capa faltar ela é desenhada em código |

---

## 10. Declaração de Uso de Inteligência Artificial

Ferramentas de inteligência artificial (Claude, da Anthropic) foram utilizadas neste projeto exclusivamente como apoio à organização e ao gerenciamento do repositório no GitHub, incluindo a estruturação de pastas, a padronização da documentação e a redação de mensagens de commit.

A concepção do sistema, o desenvolvimento do código-fonte e as decisões técnicas e de projeto são de autoria dos integrantes do grupo, listados na seção de Autores. Todo o conteúdo publicado neste repositório foi revisado pela equipe, que se responsabiliza integralmente por ele.

---

## 11. Autores:

- **LUÍS GUILHERME LEBEDEFF ROCHA MOTA** - [lebedeff-dev](https://github.com/lebedeff-dev)
- **RENAN DE CARVALHO SÉRGIO** - [RenanDev2006](https://github.com/RenanDev2006)
- **JÔNATAS ALVES OLIVEIRA** - [Jot4nas](https://github.com/Jot4nas)
