package controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dao.HistoricoVisualizacaoDAO;
import dao.PerfilDAO;
import model.HistoricoVisualizacao;
import model.Perfil;
import util.Conversor;

/**
 * Controller do relatorio de visualizacoes por periodo, com filtro opcional por perfil.
 * Usa o INNER JOIN do DAO e calcula os totais.
 */
public class RelatorioController {

    public static final String TODOS = "Todos";

    private final HistoricoVisualizacaoDAO dao = new HistoricoVisualizacaoDAO();
    private final PerfilDAO perfilDAO = new PerfilDAO();

    /** Itens do filtro de perfil: "Todos" e "id - nome" de cada perfil. */
    public List<String> opcoesPerfis() {
        try {
            List<String> opcoes = new ArrayList<>();
            opcoes.add(TODOS);
            for (Perfil p : perfilDAO.listar()) {
                opcoes.add(Conversor.opcao(p.getIdPerfil(), p.getNome()));
            }
            return opcoes;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao carregar perfis: " + e.getMessage(), e);
        }
    }

    /**
     * Valida o filtro e busca as visualizacoes do periodo. A validacao lanca
     * IllegalArgumentException e o banco lanca RuntimeException; a tela captura as duas.
     */
    public List<HistoricoVisualizacao> gerar(String dataInicial, String dataFinal, String perfil) {
        LocalDate inicio = Conversor.paraData(dataInicial, "Data inicial");
        LocalDate fim = Conversor.paraData(dataFinal, "Data final");
        if (fim.isBefore(inicio)) { // periodo invalido
            throw new IllegalArgumentException("A data final não pode ser anterior à data inicial.");
        }
        int idPerfil = TODOS.equals(perfil) ? 0 : Conversor.extrairId(perfil, "Perfil"); // 0 = todos

        try {
            return dao.listarPorPeriodo(inicio, fim, idPerfil);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao gerar o relatório: " + e.getMessage(), e);
        }
    }

    /** Totais mostrados abaixo da tabela. */
    public String resumo(List<HistoricoVisualizacao> lista) {
        int concluidas = 0;
        int minutos = 0;
        for (HistoricoVisualizacao h : lista) {
            if (h.isConcluido()) {
                concluidas++;
            }
            minutos += h.getMinutoParada();
        }
        return "Total de visualizações: " + lista.size()
                + "   |   Concluídas: " + concluidas
                + "   |   Em andamento: " + (lista.size() - concluidas)
                + "   |   Minutos assistidos: " + minutos;
    }

    /** Grava o relatorio em .txt e devolve a mensagem para a tela. */
    public String salvarTxt(String caminho, List<HistoricoVisualizacao> lista,
                            String dataInicial, String dataFinal, String perfil) {
        StringBuilder texto = new StringBuilder();
        texto.append("RELATÓRIO DE VISUALIZAÇÕES POR PERÍODO\n");
        texto.append("Período: ").append(dataInicial).append(" a ").append(dataFinal)
             .append("   Perfil: ").append(perfil).append("\n\n");
        for (HistoricoVisualizacao h : lista) {
            texto.append(Conversor.formatarDataHora(h.getDataInicio())).append(" | ")
                 .append(h.getNomePerfil()).append(" (").append(h.getEmailConta())
                 .append(", plano ").append(h.getNomePlano()).append(") | ")
                 .append(h.getNomeTitulo()).append(" | parou no minuto ")
                 .append(h.getMinutoParada()).append(" | concluído: ")
                 .append(Conversor.simNao(h.isConcluido())).append(" | ")
                 .append(h.getDispositivo()).append("\n");
        }
        texto.append("\n").append(resumo(lista)).append("\n");

        try {
            Files.write(Path.of(caminho), texto.toString().getBytes(StandardCharsets.UTF_8));
            return "Relatório salvo em: " + caminho;
        } catch (IOException e) {
            return "Não foi possível salvar o arquivo: " + e.getMessage();
        }
    }
}
