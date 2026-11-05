package controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dao.HistoricoVisualizacaoDAO;
import dao.PerfilDAO;
import dao.TituloDAO;
import model.HistoricoVisualizacao;
import model.Perfil;
import model.Titulo;
import util.Conversor;

/**
 * CONTROLLER do CRUD principal (Historico de Visualizacao).
 * Aqui ha regras de negocio de verdade (ver montar()).
 */
public class HistoricoController {

    private final HistoricoVisualizacaoDAO dao = new HistoricoVisualizacaoDAO();
    private final PerfilDAO perfilDAO = new PerfilDAO();
    private final TituloDAO tituloDAO = new TituloDAO();

    /** Lista com NOMES (resultado do INNER JOIN do DAO) para a tabela da tela. */
    public List<HistoricoVisualizacao> listarComNomes() {
        try {
            return dao.listarComNomes();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar histórico: " + e.getMessage(), e);
        }
    }

    /** Duracao em minutos do titulo escolhido (null se for serie ou se o titulo nao existir). */
    private Integer duracaoDoTitulo(int idTitulo) throws SQLException {
        Titulo t = tituloDAO.buscarPorId(idTitulo);
        return t == null ? null : t.getDuracaoMinutos();
    }

    /** Opcoes do combo de perfis ("id - nome"). */
    public List<String> opcoesPerfis() {
        try {
            List<String> opcoes = new ArrayList<>();
            for (Perfil p : perfilDAO.listar()) {
                opcoes.add(Conversor.opcao(p.getIdPerfil(), p.getNome()));
            }
            return opcoes;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao carregar perfis: " + e.getMessage(), e);
        }
    }

    public List<String> opcoesTitulos() {
        try {
            List<String> opcoes = new ArrayList<>();
            for (Titulo t : tituloDAO.listar()) {
                opcoes.add(Conversor.opcao(t.getIdTitulo(), t.getNome()));
            }
            return opcoes;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao carregar títulos: " + e.getMessage(), e);
        }
    }

    public String inserir(String perfil, String titulo, String minutoParada, String concluido, String dispositivo) {
        try {
            dao.inserir(montar(perfil, titulo, minutoParada, concluido, dispositivo));
            return "Visualização registrada com sucesso!";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (SQLException e) {
            return "Erro no banco de dados: " + e.getMessage();
        }
    }

    public String atualizar(String id, String perfil, String titulo, String minutoParada, String concluido, String dispositivo) {
        try {
            int idVisualizacao = Conversor.paraId(id);
            HistoricoVisualizacao h = montar(perfil, titulo, minutoParada, concluido, dispositivo);
            h.setIdVisualizacao(idVisualizacao);
            if (!dao.atualizar(h)) {
                return Conversor.REGISTRO_NAO_ENCONTRADO;
            }
            return "Visualização atualizada com sucesso!";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (SQLException e) {
            return "Erro no banco de dados: " + e.getMessage();
        }
    }

    public String eliminar(String id) {
        try {
            if (!dao.eliminar(Conversor.paraId(id))) {
                return Conversor.REGISTRO_NAO_ENCONTRADO;
            }
            return "Registro removido com sucesso!";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (SQLException e) {
            return "Erro no banco de dados: " + e.getMessage();
        }
    }

    private HistoricoVisualizacao montar(String perfil, String titulo, String minutoParada, String concluido, String dispositivo)
            throws SQLException {
        int minuto = Conversor.paraInt(minutoParada, "Minuto de parada");
        int idTitulo = Conversor.extrairId(titulo, "Título");

        // Regras de negocio (ficam no Controller, nunca na View):
        if (minuto < 0) {
            throw new IllegalArgumentException("O minuto de parada não pode ser negativo.");
        }
        boolean terminou = Conversor.paraBoolean(concluido);
        Integer duracao = duracaoDoTitulo(idTitulo); // null para series (a duracao fica nos episodios)
        if (duracao != null) {
            if (minuto > duracao) {
                throw new IllegalArgumentException("O minuto de parada (" + minuto
                        + ") não pode passar da duração do título (" + duracao + " min).");
            }
            // Concluido e minuto precisam concordar: so terminou quem parou no ultimo minuto.
            if (terminou && minuto != duracao) {
                throw new IllegalArgumentException("Para marcar como concluído, o minuto de parada deve ser "
                        + duracao + " (a duração do título).");
            }
            if (!terminou && minuto == duracao) {
                throw new IllegalArgumentException("O minuto de parada chegou ao fim do título ("
                        + duracao + " min): marque Concluído como Sim.");
            }
        }
        return new HistoricoVisualizacao(
                Conversor.extrairId(perfil, "Perfil"),
                idTitulo,
                minuto,
                terminou,
                dispositivo);
    }
}
