package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Tela base de CRUD (TEMPLATE METHOD): monta o layout padrao (formulario em cima,
 * tabela no meio, botoes embaixo) e liga os botoes. Cada tela filha so informa
 * QUAIS campos tem e CHAMA O SEU CONTROLLER nos metodos abstratos.
 * Heranca evita repetir o mesmo codigo de layout nas 5 telas.
 *
 * Regra do MVC: esta classe NAO tem regra de negocio nem acesso a banco.
 * Ela so le o que o usuario digitou, chama o Controller e mostra a mensagem devolvida.
 *
 * Convencao: as colunas da tabela sao [ID] + [um campo do formulario por coluna,
 * na mesma ordem]. Assim, ao clicar numa linha, copiamos as celulas para os campos.
 * Colunas EXTRAS so de exibicao (ex.: Conta e Plano no Historico) ficam no FIM da tabela:
 * como nao tem campo no formulario, sao ignoradas ao copiar a linha.
 */
public abstract class TelaCrud extends JFrame {

    /**
     * DefaultTableModel = a "planilha em memoria" que o JTable exibe.
     * O JTable apenas DESENHA o que esta no modelo. Para mudar a tabela, mudamos
     * o modelo: setRowCount(0) apaga as linhas e addRow(Object[]) adiciona uma
     * (cada posicao do vetor = uma coluna).
     */
    protected final DefaultTableModel modelo;
    protected final JTable tabela;

    private final Map<String, JComponent> campos = new LinkedHashMap<>(); // rotulo -> componente (mantem a ordem)
    private final JPanel painelForm = new JPanel(new GridLayout(0, 2, 5, 5));

    // true enquanto o programa (e nao o usuario) muda a selecao da tabela; evita que o
    // formulario seja sobrescrito com os valores da linha quando recarregamos a tabela.
    private boolean recarregandoTabela = false;

    protected TelaCrud(String titulo, String[] colunas) {
        super(titulo);

        // Modelo com os nomes das colunas e 0 linhas. Sobrescrevemos isCellEditable
        // para a tabela ser so de leitura (a edicao e feita pelo formulario).
        modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tabela = new JTable(modelo);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    // ---------- metodos que cada tela filha implementa ----------

    /** Busca a lista no Controller e preenche o 'modelo' (addRow para cada objeto). */
    protected abstract void carregarTabela();

    /** Le os campos, chama controller.inserir(...) e devolve a mensagem de resultado. */
    protected abstract String inserir();

    /** Idem para atualizar; 'id' e o ID da linha selecionada na tabela. */
    protected abstract String atualizar(String id);

    /** Idem para eliminar. */
    protected abstract String eliminar(String id);

    /** Gancho opcional: a tela filha pode acrescentar botoes proprios ao lado de Inserir, Alterar, Remover e Limpar. */
    protected void acrescentarBotoes(JPanel painelBotoes) {
    }

    /** Gancho opcional: preenche os combos de chave estrangeira (ex.: lista de planos). */
    protected void carregarCombos() {
    }

    /** Gancho opcional: valores iniciais dos campos (ex.: data de hoje). */
    protected void definirPadroes() {
    }

    // ---------- campos do formulario ----------

    protected void adicionarCampo(String rotulo, JComponent componente) {
        campos.put(rotulo, componente);
        painelForm.add(new JLabel(rotulo));
        painelForm.add(componente);
    }

    /** Troca os itens de um combo pelos itens da lista (usado nos combos de FK). */
    protected void preencherCombo(JComboBox<String> combo, List<String> opcoes) {
        combo.removeAllItems();
        for (String opcao : opcoes) {
            combo.addItem(opcao);
        }
    }

    /** Le um campo como texto (JTextField ou JComboBox). A View entrega tudo como String ao Controller. */
    protected String valor(String rotulo) {
        JComponent c = campos.get(rotulo);
        if (c instanceof JComboBox) {
            Object item = ((JComboBox<?>) c).getSelectedItem();
            return item == null ? "" : item.toString();
        }
        return ((JTextField) c).getText();
    }

    /** Coloca um texto no campo pelo rotulo (sobrecarga do metodo abaixo). */
    protected void definirValor(String rotulo, String texto) {
        definirValor(campos.get(rotulo), texto);
    }

    private void definirValor(JComponent c, String texto) {
        if (c instanceof JComboBox) {
            // Seleciona no combo o item igual ao texto (ex.: "3 - Premium").
            ((JComboBox<?>) c).setSelectedItem(texto);
        } else {
            ((JTextField) c).setText(texto);
        }
    }

    // ---------- montagem da tela ----------

    /**
     * Deve ser chamado no FINAL do construtor da tela filha (depois dos campos
     * criados). Nao fazemos isso no construtor da base porque os atributos da filha
     * (como o controller) ainda nao existiriam nesse momento.
     */
    protected void iniciar() {
        painelForm.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JButton btnInserir = new JButton("Inserir");
        JButton btnAtualizar = new JButton("Alterar");
        JButton btnEliminar = new JButton("Remover");
        JButton btnLimpar = new JButton("Limpar");

        /*
         * EVENTOS DOS BOTOES (ActionListener):
         * addActionListener registra "o que fazer quando o botao for clicado".
         * "e -> ..." e uma LAMBDA, forma curta de escrever:
         *     new ActionListener() { public void actionPerformed(ActionEvent e) { ... } }
         * O clique NAO tem logica: chama o metodo da tela filha (que chama o
         * Controller) e mostra a mensagem que voltou.
         */
        btnInserir.addActionListener(e -> mostrarResultado(inserir()));
        btnAtualizar.addActionListener(e -> mostrarResultado(atualizar(idSelecionado())));
        btnEliminar.addActionListener(e -> confirmarEliminar());
        btnLimpar.addActionListener(e -> limpar());

        // Clicar numa linha da tabela copia os valores dela para o formulario (para editar).
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !recarregandoTabela && tabela.getSelectedRow() >= 0) {
                aoSelecionar();
            }
        });

        JPanel painelBotoes = new JPanel(new FlowLayout());
        painelBotoes.add(btnInserir);
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnEliminar);
        painelBotoes.add(btnLimpar);
        acrescentarBotoes(painelBotoes);

        // Topo: a barra de navegacao (igual em todas as telas) e, embaixo dela, o formulario.
        JPanel topo = new JPanel(new BorderLayout());
        topo.add(new BarraNavegacao(this, nomeNaBarra(), null), BorderLayout.NORTH);
        topo.add(painelForm, BorderLayout.CENTER);
        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER); // JScrollPane da a barra de rolagem
        add(painelBotoes, BorderLayout.SOUTH);

        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE); // so ha uma janela por vez (o Navegador troca); fechar encerra o programa

        recarregar(true);
    }

    /** Nome do botao da barra que corresponde a esta tela (o titulo da janela e maior: "Histórico de Visualização"). */
    private String nomeNaBarra() {
        return getTitle().startsWith("Hist") ? "Histórico" : getTitle();
    }

    /** Copia as celulas da linha selecionada para os campos (coluna 0 = ID, nao tem campo). */
    private void aoSelecionar() {
        int linha = tabela.getSelectedRow();
        int coluna = 1;
        for (JComponent c : campos.values()) {
            Object celula = modelo.getValueAt(linha, coluna++);
            definirValor(c, celula == null ? "" : celula.toString());
        }
    }

    /** Pede confirmacao antes de apagar; so elimina se o usuario clicar em "Sim". */
    private void confirmarEliminar() {
        if (idSelecionado().isEmpty()) { // nada selecionado: o Controller devolve o aviso, sem perguntar nada antes
            mostrarResultado(eliminar(""));
            return;
        }
        int resposta = JOptionPane.showConfirmDialog(this, "Remover o registro selecionado?",
                "Confirmação", JOptionPane.YES_NO_OPTION);
        if (resposta == JOptionPane.YES_OPTION) {
            mostrarResultado(eliminar(idSelecionado()));
        }
    }

    /**
     * Depois de toda acao: mostra a mensagem do Controller e recarrega a tabela.
     * O formulario so e limpo quando deu certo (mensagem com "sucesso"). Se deu erro,
     * o usuario mantem o que digitou e so corrige o campo errado.
     */
    private void mostrarResultado(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem);
        boolean deuCerto = mensagem != null && mensagem.contains("sucesso");
        recarregar(deuCerto);
    }

    /**
     * Busca tudo de novo no banco: combos e tabela. Se o banco estiver fora do ar,
     * o Controller lanca RuntimeException e aqui mostramos a mensagem.
     *
     * limparFormulario = false (depois de um erro): recarregar os combos os volta para o
     * primeiro item e a tabela perde a selecao, entao guardamos a escolha de cada combo e
     * a linha selecionada e restauramos tudo no fim.
     */
    private void recarregar(boolean limparFormulario) {
        Map<JComboBox<?>, Object> escolhidos = new HashMap<>();
        String idSelecionadoAntes = idSelecionado();
        if (!limparFormulario) {
            for (JComponent c : campos.values()) {
                if (c instanceof JComboBox) {
                    escolhidos.put((JComboBox<?>) c, ((JComboBox<?>) c).getSelectedItem());
                }
            }
        }

        recarregandoTabela = true;
        try {
            modelo.setRowCount(0);
            try {
                carregarCombos();
                carregarTabela();
            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }

            if (limparFormulario) {
                limpar();
            } else {
                for (Map.Entry<JComboBox<?>, Object> e : escolhidos.entrySet()) {
                    if (e.getValue() != null) {
                        e.getKey().setSelectedItem(e.getValue());
                    }
                }
                for (int linha = 0; linha < modelo.getRowCount(); linha++) {
                    if (modelo.getValueAt(linha, 0).toString().equals(idSelecionadoAntes)) {
                        tabela.setRowSelectionInterval(linha, linha);
                        break;
                    }
                }
            }
        } finally {
            recarregandoTabela = false;
        }
    }

    private void limpar() {
        for (JComponent c : campos.values()) {
            if (c instanceof JComboBox) {
                JComboBox<?> combo = (JComboBox<?>) c;
                if (combo.getItemCount() > 0) {
                    combo.setSelectedIndex(0);
                }
            } else {
                ((JTextField) c).setText("");
            }
        }
        tabela.clearSelection();
        definirPadroes();
    }

    /** ID (coluna 0) da linha selecionada; "" se nada estiver selecionado (o Controller avisa). */
    private String idSelecionado() {
        int linha = tabela.getSelectedRow();
        return linha < 0 ? "" : modelo.getValueAt(linha, 0).toString();
    }
}
