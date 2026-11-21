package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import model.HistoricoVisualizacao;
import util.ConnectionFactory;

/**
 * DAO do CRUD principal (tabela associativa entre Perfil e Titulo).
 * Conceitos de PreparedStatement e try-with-resources: veja o PlanoDAO.
 */
public class HistoricoVisualizacaoDAO {

    /**
     * SELECT das duas consultas (listarComNomes e listarPorPeriodo). O historico guarda so ids
     * (id_perfil, id_titulo), e a tela precisa de nomes. Cada INNER JOIN liga a linha do historico
     * a uma tabela pai, casando a PK dela com a FK (perfil e titulo direto, conta e plano pelo perfil).
     * As FKs garantem que o pai existe, entao nenhuma linha some. Perfil e titulo tem uma coluna
     * "nome" cada, por isso os apelidos nome_perfil e nome_titulo.
     */
    private static final String SELECT_COM_NOMES =
            "SELECT h.id_visualizacao, h.id_perfil, h.id_titulo, h.data_inicio, "
          + "h.data_ultima_visualizacao, h.minuto_parada, h.concluido, h.dispositivo, "
          + "p.nome AS nome_perfil, t.nome AS nome_titulo, "
          + "c.email AS email_conta, pl.nome AS nome_plano "
          + "FROM historico_visualizacao h "
          + "INNER JOIN perfil p ON p.id_perfil = h.id_perfil "
          + "INNER JOIN titulo t ON t.id_titulo = h.id_titulo "
          + "INNER JOIN conta c ON c.id_conta = p.id_conta "
          + "INNER JOIN plano pl ON pl.id_plano = c.id_plano ";

    /** Grava as duas FKs e os dados da sessao. As datas vem do NOW() do MySQL. */
    public void inserir(HistoricoVisualizacao h) throws SQLException {
        String sql = "INSERT INTO historico_visualizacao (id_perfil, id_titulo, data_inicio, "
                   + "data_ultima_visualizacao, minuto_parada, concluido, dispositivo) "
                   + "VALUES (?, ?, NOW(), NOW(), ?, ?, ?)";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, h.getIdPerfil());
            ps.setInt(2, h.getIdTitulo());
            ps.setInt(3, h.getMinutoParada());
            ps.setBoolean(4, h.isConcluido());
            ps.setString(5, h.getDispositivo());
            // nomePerfil e nomeTitulo nao sao gravados, pois nao existem na tabela.
            ps.executeUpdate();
        }
    }

    /** Todas as visualizacoes com nomes, das mais recentes para as mais antigas. */
    public List<HistoricoVisualizacao> listarComNomes() throws SQLException {
        String sql = SELECT_COM_NOMES + "ORDER BY h.data_ultima_visualizacao DESC";
        List<HistoricoVisualizacao> lista = new ArrayList<>();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(montar(rs));
            }
        }
        return lista;
    }

    /**
     * Consulta do relatorio: visualizacoes iniciadas entre duas datas, com nomes. Com idPerfil 0
     * traz todos os perfis. DATE(h.data_inicio) tira a hora do DATETIME e o BETWEEN inclui as
     * duas pontas.
     */
    public List<HistoricoVisualizacao> listarPorPeriodo(LocalDate dataInicial, LocalDate dataFinal, int idPerfil)
            throws SQLException {
        String sql = SELECT_COM_NOMES + "WHERE DATE(h.data_inicio) BETWEEN ? AND ? ";
        if (idPerfil > 0) {
            sql += "AND h.id_perfil = ? "; // filtro opcional
        }
        sql += "ORDER BY h.data_inicio";
        List<HistoricoVisualizacao> lista = new ArrayList<>();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(dataInicial));
            ps.setDate(2, Date.valueOf(dataFinal));
            if (idPerfil > 0) {
                ps.setInt(3, idPerfil);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(montar(rs));
                }
            }
        }
        return lista;
    }

    /** Altera a sessao do id. Sem o WHERE, todas as linhas mudariam. */
    public boolean atualizar(HistoricoVisualizacao h) throws SQLException {
        String sql = "UPDATE historico_visualizacao SET id_perfil = ?, id_titulo = ?, "
                   + "data_ultima_visualizacao = NOW(), minuto_parada = ?, concluido = ?, dispositivo = ? "
                   + "WHERE id_visualizacao = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, h.getIdPerfil());
            ps.setInt(2, h.getIdTitulo());
            ps.setInt(3, h.getMinutoParada());
            ps.setBoolean(4, h.isConcluido());
            ps.setString(5, h.getDispositivo());
            ps.setInt(6, h.getIdVisualizacao()); // id do WHERE
            return ps.executeUpdate() > 0; // false = nenhuma linha com esse id
        }
    }

    public boolean eliminar(int idVisualizacao) throws SQLException {
        String sql = "DELETE FROM historico_visualizacao WHERE id_visualizacao = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVisualizacao);
            return ps.executeUpdate() > 0; // false = nenhuma linha com esse id
        }
    }

    /** Converte a linha atual do ResultSet em objeto. As colunas do JOIN vao para os campos de exibicao. */
    private HistoricoVisualizacao montar(ResultSet rs) throws SQLException {
        HistoricoVisualizacao h = new HistoricoVisualizacao();
        h.setIdVisualizacao(rs.getInt("id_visualizacao"));
        h.setIdPerfil(rs.getInt("id_perfil"));
        h.setIdTitulo(rs.getInt("id_titulo"));
        h.setDataInicio(rs.getTimestamp("data_inicio").toLocalDateTime());
        h.setDataUltimaVisualizacao(rs.getTimestamp("data_ultima_visualizacao").toLocalDateTime());
        h.setMinutoParada(rs.getInt("minuto_parada"));
        h.setConcluido(rs.getBoolean("concluido"));
        h.setDispositivo(rs.getString("dispositivo"));

        // Colunas do JOIN, lidas pelo apelido do SELECT:
        h.setNomePerfil(rs.getString("nome_perfil"));
        h.setNomeTitulo(rs.getString("nome_titulo"));
        h.setEmailConta(rs.getString("email_conta"));
        h.setNomePlano(rs.getString("nome_plano"));
        return h;
    }
}
