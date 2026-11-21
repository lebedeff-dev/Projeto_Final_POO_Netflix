# Roteiro de apresentação: 3 pessoas, 2 partes cada

| Pessoa | Partes | Extras dela |
|---|---|---|
| **1** | Plano e Conta | `TelaCrud` (classe abstrata) e `Conversor` |
| **2** | Perfil e Título | Visão geral do banco e `CriadorBanco` |
| **3** | Histórico e Relatório | O INNER JOIN de 5 tabelas e o `ON DELETE CASCADE` |

- Os números de linha valem para o código depois da revisão dos comentários e os arquivos citados estão em `Netflix/src/`, exceto o script do banco (`Netflix/sql/netflix.sql`).
- Se alguém editar o código, as linhas mudam: confira antes de apresentar.
- O guia anterior (`ROTEIRO-ESTUDO.md`) usa outra divisão e fica como material de apoio.

---

## 0. Como o professor costuma conduzir

1. **Pergunta de abertura:** "No que você mais trabalhou no código?" Ele escolhe as perguntas seguintes a partir da sua resposta.
2. **"O que essa classe faz?"** Responda em 3 passos: a camada (Model, View, Controller, DAO ou util), a responsabilidade em uma frase e com quem ela conversa.
3. **"O que essas X linhas fazem?"** Diga o que o bloco faz, por que existe e o que acontece se for removido.
4. **"Ache pra mim X no código."** Use o mapa da seção 5 (Ctrl+N abre a classe, Ctrl+F12 lista os métodos).
5. **O banco de dados:** com outro grupo, ele perguntou sobre o banco e não sobre o DAO. Priorize as tabelas, as chaves e o script.

**Regras para a abertura**
- Fale só da parte que você realmente fez. Cada frase que você disser abre uma pergunta.
- Cite classes e decisões concretas. "Trabalhei no sistema todo" não serve.
- Prepare três níveis para a sua parte. O terceiro é o que separa quem fez de quem decorou:
  1. **O que faz:** o fluxo (tela, Controller, DAO, banco).
  2. **Como faz:** os blocos de linhas.
  3. **Por quê:** a decisão por trás do código.
- Ele pode pedir para ligar a sua parte ao resto. Saiba para onde aponta a sua FK e quem depende da sua tabela.

---

## 1. Parte comum: todos precisam saber

### O fluxo de qualquer CRUD

```
Botão (TelaCrud.java:166-169)
  -> inserir()/atualizar()/eliminar() da tela filha        (ex.: TelaConta.java:70-85)
  -> Controller valida e monta o Model                     (ex.: ContaController.java:40-110)
  -> DAO executa o SQL com PreparedStatement               (ex.: ContaDAO.java:19-91)
  -> MySQL
  <- volta uma mensagem de texto até mostrarResultado()    (TelaCrud.java:222-226)
```

### O banco de dados (`sql/netflix.sql`)

```
plano 1──N conta 1──N perfil N──┐
                                ├─ historico_visualizacao (tabela associativa)
                      titulo N──┘
```

| Tabela | Linhas no script | PK | FK |
|---|---|---|---|
| `plano` | 16-25 | `id_plano` | nenhuma |
| `conta` | 30-40 | `id_conta` | `id_plano` → `plano` (linha 39) |
| `perfil` | 45-54 | `id_perfil` | `id_conta` → `conta` (linha 53) |
| `titulo` | 59-70 | `id_titulo` | nenhuma |
| `historico_visualizacao` | 78-89 | `id_visualizacao` | `id_perfil` → `perfil` (87) e `id_titulo` → `titulo` (88), as duas com `ON DELETE CASCADE` |

Perguntas que valem para todos:
- **Por que 5 tabelas?** O comentário da linha 5 diz que é um recorte das 13 tabelas do modelo lógico (pasta `Banco de dados/`). Ficou a cadeia plano, conta, perfil, histórico e título, que basta para a rotina principal.
- **O que é PK e FK?** PK identifica a linha (aqui, `INT AUTO_INCREMENT`). FK aponta para a PK de outra tabela e garante que o registro pai existe.
- **O que é `InnoDB`?** O motor do MySQL que suporta chaves estrangeiras (`ENGINE=InnoDB` em cada tabela).
- **O script apaga e recria o banco** (linha 9, `DROP DATABASE IF EXISTS netflix`). É o que o botão **Restaurar dados** executa.
- **O que é `ENUM`?** Uma coluna que só aceita valores de uma lista fixa (linhas 21, 38, 63, 65 e 86).
- **O que é `NOT NULL` e `DEFAULT`?** `NOT NULL` obriga a ter valor, e `DEFAULT` é o valor usado quando nada é informado (por exemplo, o país `'BR'` na linha 36).
- **`DATE` ou `DATETIME`?** `DATE` guarda só o dia (plano, perfil e título) e `DATETIME` guarda dia e hora (conta e histórico).
- **Os erros do MySQL que o sistema trata:** `1062` é valor repetido em coluna `UNIQUE`, `1451` é remoção recusada por existirem filhos, e `1452` é FK apontando para um pai que não existe.

### Conceitos de POO e MVC

- **MVC:** a View só captura e mostra, o Controller valida e aplica as regras, o DAO fala SQL e o Model guarda os dados.
- **Encapsulamento:** atributos privados nos models, acessados por getters e setters.
- **Herança, classe abstrata e polimorfismo:** as 5 telas de cadastro herdam de `TelaCrud` (detalhes na Pessoa 1).
- **`PreparedStatement`:** SQL com `?` e valores enviados separados. Evita SQL injection (explicação em `PlanoDAO.java:17-18`).
- **`try-with-resources`:** fecha a conexão sozinho, mesmo com erro (`PlanoDAO.java:19-20`).

---

## 2. Pessoa 1: Plano e Conta

### Resposta de abertura (modelo de 30 segundos)

> "Trabalhei mais em **Plano e Conta**. Fiz o CRUD das duas: `PlanoController`, `PlanoDAO` e `TelaPlano`, e o mesmo para Conta. Cuidei das regras de validação, como o valor do plano maior que zero e a descrição automática, o país com 2 letras na conta, e de traduzir os erros do MySQL, como e-mail repetido e plano com contas. Também expliquei a `TelaCrud`, a classe abstrata que as cinco telas herdam."

Ajuste as frases ao que você de fato fez.

### Classes e linhas

| Classe | Camada | O que faz |
|---|---|---|
| `model/Plano` | Model | Dados do plano. `valorMensal` é `BigDecimal` para não ter erro de arredondamento com dinheiro. |
| `model/Conta` | Model | Dados da conta, com `idPlano` (FK). `nomePlano` é só de exibição, preenchido pelo JOIN. |
| `dao/PlanoDAO` | DAO | SQL de `plano`. |
| `dao/ContaDAO` | DAO | SQL de `conta`, com `INNER JOIN` em `plano` para mostrar o nome do plano. |
| `controller/PlanoController` | Controller | Valida e monta o plano e gera a descrição automática. |
| `controller/ContaController` | Controller | Valida e monta a conta e traduz erros do MySQL. |
| `view/TelaPlano`, `view/TelaConta` | View | Declaram os campos e chamam o Controller. |

**Plano**

| Onde | O que faz |
|---|---|
| `PlanoController.java:21-29` | `listar`. Erro de banco sobe como `RuntimeException` e a `TelaCrud` mostra a mensagem. |
| `PlanoController.java:31-41` | `inserir`. `IllegalArgumentException` é erro de validação e `SQLException` é erro do banco. |
| `PlanoController.java:43-58` | `atualizar`. Se o DAO devolver `false`, o id não existe mais e a mensagem avisa. |
| `PlanoController.java:60-76` | `eliminar`. O erro 1451 (linha 70) significa que há contas usando o plano. |
| `PlanoController.java:84-97` | `gerarDescricao`. Transforma telas e qualidade em texto: `("2", "Full HD")` vira "Duas telas, qualidade Full HD". Devolve `""` se o número for inválido. |
| `PlanoController.java:100-117` | `montar`. Linha 103: valor maior que zero (`signum() <= 0`). Linha 107: pelo menos 1 tela. Linha 112: a descrição é gerada, não digitada. |
| `PlanoDAO.java:26-42` | `inserir`. O id não entra (`AUTO_INCREMENT`) e `CURDATE()` grava a data de criação (linha 28). |
| `PlanoDAO.java:45-60` | `listar`. `executeQuery` devolve o `ResultSet`, e `rs.next()` avança uma linha por vez. |
| `PlanoDAO.java:63-80` | `atualizar`. O `WHERE id_plano = ?` (linha 76) é essencial: sem ele, todos os planos seriam alterados. |
| `PlanoDAO.java:83-91` | `eliminar`. Se houver contas ligadas, o MySQL recusa e lança `SQLException`. |
| `PlanoDAO.java:94-104` | `montarPlano`. Converte `snake_case` do banco em `camelCase` do Java, sem repetir código. |
| `TelaPlano.java:42-58` | Construtor. `super(...)` informa o título e as colunas, `adicionarCampo` registra os campos e `iniciar` monta a tela. |
| `TelaPlano.java:55-56, 61-64` | Os combos de qualidade e telas recalculam a descrição ao vivo. A tela só pede o texto ao Controller. |
| `TelaPlano.java:66-72` | `carregarTabela`: uma linha na tabela para cada plano. |
| `TelaPlano.java:74-89` | `inserir`, `atualizar` e `eliminar`: leem os campos com `valor("...")` e chamam o Controller. |

**Conta**

| Onde | O que faz |
|---|---|
| `ContaController.java:28-38` | `opcoesPlanos`. Alimenta o combo no formato "id - nome". |
| `ContaController.java:40-67` | `inserir` e `atualizar`. |
| `ContaController.java:69-84` | `eliminar`. O erro 1451 (linha 79) significa que a conta ainda tem perfis. |
| `ContaController.java:87-95` | `mensagemDeErro`. `1062` é e-mail repetido (coluna `UNIQUE`) e `1452` é plano inexistente. |
| `ContaController.java:97-110` | `montar`. Linhas 99-102: país com exatamente 2 letras (`CHAR(2)`). Linhas 103-109: converte o plano em id, exige e-mail e senha, troca telefone vazio por `null` e deixa o país em maiúsculas. O comentário da linha 106 avisa que em um sistema real aqui se geraria o hash. |
| `ContaDAO.java:19-34` | `inserir`. `NOW()` grava a data de cadastro (linha 21). `setString` aceita `null` e grava `NULL` (linha 29). |
| `ContaDAO.java:36-62` | `listar` com `INNER JOIN plano` (linhas 38-41). A coluna `nome_plano` vem do JOIN (linha 57). |
| `ContaDAO.java:64-80` | `atualizar`, com `WHERE id_conta = ?`. |
| `ContaDAO.java:82-91` | `eliminar`. O comentário da linha 82 explica que falha se a conta tiver perfis. |
| `TelaConta.java:55-58` | `carregarCombos`. Preenche o combo de planos a partir do Controller. |
| `TelaConta.java:60-68` | `carregarTabela`. A coluna Plano mostra "3 - Premium" (id e nome do JOIN). |
| `TelaConta.java:87-90` | `definirPadroes`. O país começa como `BR`. |

### Parte do banco

- **`plano` (script, linhas 16-25):**
  - `valor_mensal DECIMAL(6,2)`: exato, ao contrário de `FLOAT`. No Java o par é `BigDecimal`.
  - `qualidade_maxima ENUM` com padrão `'HD'`.
  - `telas_simultaneas TINYINT` e `permite_download TINYINT(1)` (booleano).
- **`conta` (linhas 30-40):**
  - `email UNIQUE` (linha 33) gera o erro 1062.
  - `pais CHAR(2)` com padrão `'BR'`.
  - `status ENUM('Ativa','Suspensa','Cancelada')`.
  - `fk_conta_plano` (linha 39) **sem** `ON DELETE CASCADE`: o MySQL recusa remover um plano com contas (erro 1451).

### Extras da Pessoa 1: `view/TelaCrud` e `util/Conversor`

**`view/TelaCrud`** (classe abstrata, base das 5 telas de cadastro)

| Onde | O que faz |
|---|---|
| `TelaCrud.java:22-36` | O comentário da classe explica o Template Method: a base monta o layout e liga os botões, e cada filha informa os campos. |
| `TelaCrud.java:37` | `public abstract class TelaCrud extends JFrame`: herança e classe abstrata. |
| `TelaCrud.java:69-82` | Construtor. Cria o `DefaultTableModel` (a "planilha em memória") e o torna só de leitura com `isCellEditable` (linhas 74-79). |
| `TelaCrud.java:87-96` | Os 4 métodos `abstract` (`carregarTabela`, `inserir`, `atualizar`, `eliminar`). Cada tela filha é obrigada a implementá-los. |
| `TelaCrud.java:99-108` | Ganchos opcionais: `acrescentarBotoes`, `carregarCombos` e `definirPadroes`. A filha só sobrescreve se precisar. |
| `TelaCrud.java:116-136` | `adicionarCampo`, `preencherCombo` e `valor`. A View entrega tudo como `String` ao Controller. |
| `TelaCrud.java:160-188` | `iniciar`. Liga os botões e a seleção da tabela e termina com `recarregar(true)`. |
| `TelaCrud.java:166-169` | Os botões só chamam métodos. A lambda `e -> ...` é a forma curta de um `ActionListener`. |
| `TelaCrud.java:172-176` e `196-203` | Clicar numa linha copia os valores para o formulário (`aoSelecionar`). |
| `TelaCrud.java:206-216` | `confirmarEliminar`. Pede confirmação com `JOptionPane` antes de apagar. |
| `TelaCrud.java:222-226` | `mostrarResultado`. O formulário só é limpo se a mensagem contiver "sucesso". Em caso de erro, o usuário mantém o que digitou. |
| `TelaCrud.java:236-275` | `recarregar`. Busca tudo de novo e, depois de um erro, restaura os combos e a linha selecionada. |

**Polimorfismo:** o botão chama `inserir()` sem saber qual tela está aberta, e o Java executa o método da filha (por exemplo `TelaPerfil.java:67-70`).

**`util/Conversor`** (a View entrega tudo como `String`; o `Conversor` converte e valida)
- `obrigatorio` (30-35) e `vazioParaNulo` (38-40): campo obrigatório e campo opcional.
- `paraInt` (42-48), `paraIntOuNulo` (51-53) e `paraDecimal` (55-61): trocam a exceção do Java por uma mensagem clara.
- `paraData` (63-69): formato `dd/MM/uuuu` com `STRICT` (linhas 17-18), que recusa datas inexistentes como 31/02.
- `opcao` (84-86) e `extrairId` (89-94): montam e desmontam o texto "3 - Premium" dos combos.
- `paraId` (97-102): "Selecione um registro na tabela primeiro."

### Perguntas por quê

- **Por que `BigDecimal` e não `double`?** `double` erra arredondamentos, o que não é aceitável com dinheiro.
- **Por que a descrição do plano é gerada?** Para nunca ficar diferente das telas e da qualidade. A regra fica no Controller, e a tela só pede o texto.
- **Por que o país é validado com 2 letras?** A coluna é `CHAR(2)`.
- **Por que a Conta não deixa remover quando tem perfis?** A FK sem `CASCADE` faz o MySQL recusar (erro 1451), e o Controller explica o motivo.
- **Por que `PreparedStatement`?** Separa o comando dos dados e evita SQL injection.
- **Por que a View não tem regra de negócio?** Para seguir o MVC: a regra fica no Controller e pode ser reaproveitada.
- **Para onde aponta a FK de Conta? Quem depende dela?** Aponta para `plano`, e o Perfil depende dela.
- **Por que existe a `TelaCrud`?** Para não repetir o layout e os botões nas 5 telas. Cada filha só informa os campos e chama o Controller.
- **O que é polimorfismo aqui?** O botão chama `inserir()` sem saber qual tela está aberta, e o Java executa o método da filha.
- **Por que a `TelaCrud` é abstrata?** Porque não faz sentido criar uma tela de cadastro "genérica": ela só existe para as filhas herdarem, e os métodos `abstract` obrigam cada filha a implementá-los.
- **Por que a View entrega `String` ao Controller?** O `Conversor` converte e valida depois, e a View fica sem regra de negócio.

---

## 3. Pessoa 2: Perfil e Título

### Resposta de abertura (modelo de 30 segundos)

> "Trabalhei mais em **Perfil e Título**. Fiz o CRUD das duas: `PerfilController`, `PerfilDAO` e `TelaPerfil`, e o mesmo para Título. Cuidei da validação do PIN com 4 dígitos, do tratamento da duração que pode ser nula nas séries e da visão geral do banco: o recorte de 13 para 5 tabelas, o script e o `CriadorBanco`."

Ajuste as frases ao que você de fato fez.

### Classes e linhas

| Classe | Camada | O que faz |
|---|---|---|
| `model/Perfil` | Model | Dados do perfil, com `idConta` (FK). `emailConta` é só de exibição. |
| `model/Titulo` | Model | Dados do título. `duracaoMinutos` é `Integer` (objeto) porque pode ser `NULL` em séries. |
| `dao/PerfilDAO` | DAO | SQL de `perfil`, com `INNER JOIN` em `conta`. |
| `dao/TituloDAO` | DAO | SQL de `titulo`, com cuidado especial na duração `NULL`. |
| `controller/PerfilController` | Controller | Valida o PIN e monta o perfil. |
| `controller/TituloController` | Controller | Converte e monta o título. |
| `view/TelaPerfil`, `view/TelaTitulo` | View | Declaram os campos e chamam o Controller. |

**Perfil**

| Onde | O que faz |
|---|---|
| `PerfilController.java:28-38` | `opcoesContas`. Alimenta o combo no formato "id - email". |
| `PerfilController.java:40-65` | `inserir` e `atualizar`. |
| `PerfilController.java:67-78` | `eliminar`. A mensagem avisa que o histórico do perfil também foi apagado (linha 72). |
| `PerfilController.java:80-91` | `montar`. Linha 82: a regex `\\d{4}` aceita só 4 dígitos. O PIN é opcional, e vazio vira `null` (linha 81). |
| `PerfilDAO.java:16-30` | `inserir`. `CURDATE()` grava a data de criação (linha 18). O PIN aceita `null` (linha 27). |
| `PerfilDAO.java:32-57` | `listar` com `INNER JOIN conta` (linhas 34-37). A coluna `email_conta` vem do JOIN (linha 52). |
| `PerfilDAO.java:59-74` | `atualizar`, com `WHERE id_perfil = ?`. |
| `PerfilDAO.java:76-85` | `eliminar`. O comentário da linha 76 explica o `ON DELETE CASCADE` no histórico. |
| `TelaPerfil.java:41-50` | Construtor. Registra os campos na mesma ordem das colunas da tabela. |
| `TelaPerfil.java:52-55` | `carregarCombos`. Preenche o combo de contas. |
| `TelaPerfil.java:57-65` | `carregarTabela`. A coluna Conta mostra "1 - ana.silva@email.com" (id e e-mail do JOIN). |

**Título**

| Onde | O que faz |
|---|---|
| `TituloController.java:15-21` | `listar`. |
| `TituloController.java:23-50` | `inserir` e `atualizar`. |
| `TituloController.java:52-63` | `eliminar`. A mensagem avisa que o histórico do título também foi apagado (linha 57). |
| `TituloController.java:65-76` | `montar`. Linha 73: `paraIntOuNulo` transforma a duração vazia em `null` (séries). |
| `TituloDAO.java:17-28` | `inserir`. Usa `preencher` para não repetir os `ps.set...`. |
| `TituloDAO.java:30-45` | `listar`. |
| `TituloDAO.java:47-61` | `buscarPorId`. Devolve `null` se não existir. O `HistoricoController` o usa para validar o minuto de parada. |
| `TituloDAO.java:63-80` | `montar`. Linhas 73-75: `getInt` devolve 0 para `NULL`, então `wasNull()` descobre se era `NULL` de verdade. |
| `TituloDAO.java:82-94` | `atualizar`. O `WHERE` é o décimo `?` (linha 91). |
| `TituloDAO.java:96-105` | `eliminar`. O comentário da linha 96 explica o `ON DELETE CASCADE`. |
| `TituloDAO.java:107-124` | `preencher`. Linhas 115-120: `Integer` nulo não pode ir em `setInt` (daria `NullPointerException`), então usa `setNull(6, Types.INTEGER)`. |
| `TelaTitulo.java:44-57` | Construtor. |
| `TelaTitulo.java:59-66` | `carregarTabela`. |

### Parte do banco

- **`perfil` (script, linhas 45-54):**
  - `fk_perfil_conta` (linha 53), sem `CASCADE`: não dá para remover uma conta que tem perfis.
  - `perfil_infantil TINYINT(1)`.
  - `pin CHAR(4)`, sem `NOT NULL`: é opcional.
- **`titulo` (linhas 59-70):**
  - `tipo ENUM('Filme','Serie','Documentario','Stand-up')` e `classificacao_indicativa ENUM('L','10','12','14','16','18')`.
  - `duracao_minutos INT`, sem `NOT NULL` (linha 66): séries ficam com `NULL`.
  - `disponivel TINYINT(1)` com padrão `1`.
- **As duas pontas do N:N:** Perfil e Título são o que o Histórico liga. Se qualquer um dos dois for removido, o histórico dele some junto (`ON DELETE CASCADE`).

### Extra da Pessoa 2: visão geral do banco e `CriadorBanco`

**Visão geral** (cada pessoa explica as tabelas da própria parte; aqui fica o conjunto)

- **Recorte:** o comentário da linha 5 do script diz que são 5 tabelas escolhidas entre as 13 do modelo lógico (pasta `Banco de dados/`). Ficou a cadeia plano, conta, perfil, histórico e título, que basta para a rotina principal.
- **Cabeçalho do script (linhas 1-11):** explica que o sistema roda o script sozinho na primeira execução e no botão **Restaurar dados**. A linha 9 (`DROP DATABASE IF EXISTS netflix`) apaga e recria o banco, e a linha 10 cria com `utf8mb4` (acentos e emojis).
- **Motor:** `ENGINE=InnoDB` em todas as tabelas (suporta chaves estrangeiras).
- **Relações:** `plano 1—N conta 1—N perfil`, e o histórico é a tabela associativa N:N entre `perfil` e `titulo`.
- **Onde há `CASCADE`:** só nas duas FKs do histórico (linhas 87-88). Em plano→conta e conta→perfil o MySQL recusa a remoção (erro 1451).
- **Dados de exemplo:** linhas 94-126 (3 planos, 3 contas, 5 perfis, 6 títulos e 6 visualizações).
- **Teste rápido do JOIN:** linhas 128-132, em comentário, com 3 tabelas.

**`util/CriadorBanco`**
- `criarSeNaoExistir` (26-30): cria o banco se ele não existe ou está sem tabelas. Um banco que já tem tabelas nunca é apagado aqui.
- `bancoSemTabelas` (33-41) e `bancoExiste` (44-52): consultam o `information_schema`.
- `recriarBanco` (55-68): o JDBC executa um comando por vez, então o script é cortado em cada `;` (linha 62) e executado em sequência. Usa `Statement` (não `PreparedStatement`) porque o script não tem valores digitados pelo usuário.
- `lerScript` (80-96): lê o `netflix.sql` de dentro do `.jar` e remove os comentários `--`.

### Perguntas por quê

- **Por que `Integer` na duração e não `int`?** `int` não aceita `null`, e série não tem duração.
- **Por que `setNull` no DAO?** `setInt` com `Integer` nulo daria `NullPointerException`.
- **Por que `wasNull()`?** `getInt` devolve 0 quando o banco tem `NULL`, e sem `wasNull` não dá para distinguir.
- **Por que o PIN é validado no Controller e não na tela?** Regra de negócio não fica na View (MVC).
- **O que acontece ao remover um perfil?** O histórico dele é apagado junto (`ON DELETE CASCADE`), e a mensagem avisa.
- **Para onde aponta a FK de Perfil? Quem depende dele?** Aponta para `conta`, e o Histórico depende dela.
- **Por que só 5 tabelas?** É um recorte das 13 do modelo lógico. Ficou a cadeia que sustenta a rotina principal.
- **O que acontece quando o sistema abre pela primeira vez?** O `CriadorBanco` vê que o banco não existe (ou está vazio) e roda o `netflix.sql`. Um banco que já tem tabelas nunca é apagado ali.
- **Por que o `CriadorBanco` corta o script em cada `;`?** O JDBC executa um comando por vez.
- **Por que `Statement` e não `PreparedStatement` no `CriadorBanco`?** O script não tem valores digitados pelo usuário, então não há risco de SQL injection.
- **Qual a diferença entre `CASCADE` e o comportamento padrão?** Com `CASCADE`, remover o pai remove os filhos. Sem ele, o MySQL recusa a remoção (erro 1451).

---

## 4. Pessoa 3: Histórico e Relatório

### Resposta de abertura (modelo de 30 segundos)

> "Trabalhei mais em **Histórico e Relatório**, que é a rotina principal. Fiz o CRUD do histórico, com a consulta INNER JOIN de 5 tabelas que mostra nomes em vez de códigos, as regras do minuto de parada e da conclusão, e o relatório por período com filtro por perfil, totais e exportação para `.txt`."

Ajuste as frases ao que você de fato fez.

### Classes e linhas

| Classe | Camada | O que faz |
|---|---|---|
| `model/HistoricoVisualizacao` | Model | Uma sessão assistida, com `idPerfil` e `idTitulo` (FKs) e campos só de exibição vindos do JOIN. |
| `dao/HistoricoVisualizacaoDAO` | DAO | SQL do histórico e as consultas com `INNER JOIN`. |
| `controller/HistoricoController` | Controller | Regras do minuto de parada e da conclusão. |
| `controller/RelatorioController` | Controller | Valida as datas, calcula os totais e grava o `.txt`. |
| `view/TelaHistorico` | View | CRUD principal, com o botão **Relatório**. |
| `view/TelaRelatorio` | View | Tela do relatório. Herda direto de `JFrame` (não é um cadastro). |

**Histórico**

| Onde | O que faz |
|---|---|
| `HistoricoVisualizacaoDAO.java:21-37` | `SELECT_COM_NOMES`: o **INNER JOIN de 5 tabelas**. O comentário explica o `ON`, os apelidos e o porquê dos `AS`. |
| `HistoricoVisualizacaoDAO.java:40-56` | `inserir`. `NOW()` grava as duas datas (linha 43). `nomePerfil` e `nomeTitulo` não são gravados porque não existem na tabela (linha 53). |
| `HistoricoVisualizacaoDAO.java:59-72` | `listarComNomes`: o SELECT com JOIN, ordenado da visualização mais recente para a mais antiga. |
| `HistoricoVisualizacaoDAO.java:107-123` | `atualizar`. Só a data da última visualização muda (`NOW()`). O `WHERE` (linha 110) é essencial. |
| `HistoricoVisualizacaoDAO.java:125-133` | `eliminar`. |
| `HistoricoVisualizacaoDAO.java:136-153` | `montar`. Linhas 141-142: `getTimestamp(...).toLocalDateTime()`. Linhas 147-151: as colunas do JOIN são lidas pelo apelido do SELECT. |
| `HistoricoController.java:32-35` | `duracaoDoTitulo`. Usa `TituloDAO.buscarPorId` e devolve `null` para série ou título inexistente. |
| `HistoricoController.java:38-60` | `opcoesPerfis` e `opcoesTitulos`. Alimentam os combos. |
| `HistoricoController.java:62-100` | `inserir`, `atualizar` e `eliminar`. |
| `HistoricoController.java:102-134` | `montar`, com as regras de negócio. Linha 108: o minuto não pode ser negativo. Linhas 113-117: não pode passar da duração do título. Linhas 119-122: só marca como concluído quem parou no último minuto. Linhas 123-126: quem chegou ao fim precisa estar como concluído. Tudo isso só vale se a duração não for `null` (séries ficam de fora). |
| `TelaHistorico.java:47-57` | Construtor. As colunas Início, Última visualização, Conta e Plano são só de exibição e ficam no fim da tabela. |
| `TelaHistorico.java:59-63` | `carregarCombos`. Preenche os combos de perfil e título. |
| `TelaHistorico.java:66-77` | `carregarTabela`. Cada objeto da lista do JOIN vira uma linha, com "1 - Ana" e "1 - Stranger Things". |
| `TelaHistorico.java:94-100` | `acrescentarBotoes`. Cria o botão **Relatório** (linha 97), que abre a `TelaRelatorio` pelo `Navegador` (linha 98). |
| `TelaHistorico.java:102-105` | `definirPadroes`. O minuto de parada começa em `0`. |

**Relatório**

| Onde | O que faz |
|---|---|
| `HistoricoVisualizacaoDAO.java:79-104` | `listarPorPeriodo`. `DATE(h.data_inicio)` tira a hora, e `BETWEEN ? AND ?` inclui as duas pontas (linha 81). O filtro por perfil só entra no SQL se `idPerfil > 0` (linhas 82-84 e 93-95). Zero significa "todos". |
| `RelatorioController.java:30-42` | `opcoesPerfis`. "Todos" mais "id - nome" de cada perfil. |
| `RelatorioController.java:47-60` | `gerar`. Converte as datas, rejeita data final anterior à inicial (linhas 50-52) e transforma "Todos" em `0`. |
| `RelatorioController.java:63-76` | `resumo`. Total, concluídas, em andamento (total menos concluídas) e minutos assistidos. |
| `RelatorioController.java:79-103` | `salvarTxt`. Monta o texto com `StringBuilder` e grava em UTF-8 com `Files.write`. |
| `TelaRelatorio.java:54-60` | O modelo da tabela, só de leitura. |
| `TelaRelatorio.java:62-69` | Guarda o último resultado e os filtros usados. O `.txt` usa esses valores (não o que está digitado agora), senão teria linhas de um período e cabeçalho de outro. |
| `TelaRelatorio.java:71-97` | Construtor. A data final começa como hoje (linha 75), liga os botões (79-80) e já abre mostrando o relatório do período padrão (93). |
| `TelaRelatorio.java:100-119` | `gerar`. Pede ao Controller, captura `RuntimeException` para mostrar o erro, guarda o filtro e preenche a tabela e o resumo. |
| `TelaRelatorio.java:122-130` | `salvar`. Abre o `JFileChooser` e entrega o caminho ao Controller. |

### Parte do banco

- **`historico_visualizacao` (script, linhas 78-89):** a **tabela associativa N:N** entre `perfil` e `titulo`.
  - Um perfil assiste a vários títulos e um título é assistido por vários perfis. Por isso fica uma tabela no meio, com as duas FKs e os dados da relação (datas, minuto, dispositivo e conclusão).
  - `data_inicio` e `data_ultima_visualizacao` são `DATETIME`.
  - `dispositivo ENUM('TV','Celular','Notebook','Tablet','Console')`.
  - **`ON DELETE CASCADE`** nas duas FKs (linhas 87-88): se o perfil ou o título for removido, o histórico dele some junto. O comentário das linhas 75-76 explica o motivo: não sobrar registro apontando para o nada.
  - Plano e Conta **não** têm CASCADE: apagar um plano não deve levar as contas embora.
- **O INNER JOIN de 5 tabelas:**
  - O histórico guarda só ids, e o usuário quer ler nomes.
  - Perfil e título vêm direto do histórico. Conta e plano chegam pelo perfil e pela conta.
  - O INNER JOIN só devolve linhas que têm correspondência nas duas tabelas. As FKs garantem que ninguém fica de fora.
  - O script traz um teste rápido com 3 tabelas nas linhas 128-132.
- **Dados de exemplo:** 6 visualizações nas linhas 120-126.

### Perguntas por quê

- **Por que o histórico é uma tabela associativa?** Por causa da relação N:N entre Perfil e Título.
- **Qual a diferença entre `listarComNomes` e `listarPorPeriodo`?** As duas usam o mesmo SELECT. A segunda acrescenta o `WHERE` por período e, opcionalmente, por perfil.
- **Por que a regra do minuto fica no Controller e não no botão?** Para a View não ter lógica de negócio (MVC).
- **Por que a regra só vale quando a duração não é `null`?** Série não tem duração, então não há o que comparar.
- **Por que o relatório está ligado ao Histórico?** O professor exige que o relatório seja da rotina principal.
- **Por que `DATE(h.data_inicio)`?** Para comparar só a data, sem a hora do `DATETIME`.
- **Por que o `.txt` usa os filtros guardados?** Para o arquivo ser coerente com o resultado que está na tela.
- **O que é INNER JOIN e por que não um SELECT simples?** O SELECT simples traria só ids. O JOIN junta as tabelas pelas FKs para mostrar nomes.

---

## 5. Mapa de localização (para a dinâmica de "ache pra mim")

| Peça | Onde |
|---|---|
| O INNER JOIN de 5 tabelas | `dao/HistoricoVisualizacaoDAO.java:28-37` |
| INNER JOIN simples (Conta e Perfil) | `dao/ContaDAO.java:38-41`, `dao/PerfilDAO.java:34-37` |
| Classe abstrata | `view/TelaCrud.java:37` |
| Métodos abstratos (Template Method) | `view/TelaCrud.java:87-96` |
| Ganchos opcionais | `view/TelaCrud.java:99-108` |
| Herança (`extends TelaCrud`) | `view/TelaConta.java:23` (e as outras 4 telas) |
| Polimorfismo (`@Override`) | `view/TelaConta.java:70-85` |
| Botões chamando o Controller | `view/TelaCrud.java:166-169` |
| Mensagem de resultado e limpeza do formulário | `view/TelaCrud.java:222-226` |
| Regra do valor do plano | `controller/PlanoController.java:103` |
| Descrição automática do plano | `controller/PlanoController.java:84-97` |
| Regra do país da conta | `controller/ContaController.java:99-102` |
| Regra do PIN | `controller/PerfilController.java:82` |
| Duração nula (Título) | `controller/TituloController.java:73`, `dao/TituloDAO.java:73-75` e `115-120` |
| Regra do minuto de parada | `controller/HistoricoController.java:108-126` |
| Erro 1451 | `controller/ContaController.java:79`, `controller/PlanoController.java:70` |
| Erros 1062 e 1452 | `controller/ContaController.java:88-93` |
| `PreparedStatement` | qualquer DAO, por exemplo `dao/PlanoDAO.java:30-31` |
| `try-with-resources` | `dao/PlanoDAO.java:30-31` |
| Explicação de `PreparedStatement` e `try-with-resources` | `dao/PlanoDAO.java:13-22` |
| `WHERE` do UPDATE | `dao/PlanoDAO.java:76`, `dao/HistoricoVisualizacaoDAO.java:110` |
| Relatório por período | `dao/HistoricoVisualizacaoDAO.java:79-104` |
| Totais do relatório | `controller/RelatorioController.java:63-76` |
| Exportação para `.txt` | `controller/RelatorioController.java:79-103` |
| Botão **Relatório** no Histórico | `view/TelaHistorico.java:94-100` |
| Criação do banco | `util/CriadorBanco.java:26-30` e `55-68` |
| Conversão e validação de texto | `util/Conversor.java:30-69` |
| Texto "id - nome" dos combos | `util/Conversor.java:84-94` |
| Script do banco | `Netflix/sql/netflix.sql` |
| `ON DELETE CASCADE` | `Netflix/sql/netflix.sql:87-88` |
| `UNIQUE` do e-mail | `Netflix/sql/netflix.sql:33` |
| Dados de exemplo | `Netflix/sql/netflix.sql:94-126` |

## 6. Ordem da demonstração

**Conexão → Plano (P1) → Conta (P1) → Perfil (P2) → Título (P2) → Histórico (P3) → Relatório (P3)**

- Respeita as chaves estrangeiras: o plano vem antes da conta, e a conta antes do perfil.
- Cada pessoa fala uma vez, em sequência, sem voltar.
- Na hora de trocar de pessoa, a tela muda.

Todos precisam saber o projeto inteiro. A nota individual vem de 2 perguntas por aluno, e o professor pode escolher qualquer parte.
