package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Plano;
import util.ConnectionFactory;

/**
 * DAO de Plano: o unico lugar com SQL da tabela plano. O Controller passa e recebe objetos Plano.
 *
 * Os outros DAOs seguem o mesmo molde, entao estas notas valem para todos:
 * - PreparedStatement: o SQL usa "?" e os valores vao separados (ps.setString, ps.setInt).
 *   O que o usuario digita nunca vira comando, o que evita SQL injection.
 * - try-with-resources: fecha Connection, PreparedStatement e ResultSet ao sair do bloco,
 *   mesmo se der erro. Sem isso a conexao ficaria aberta.
 * - Os metodos lancam SQLException, e o Controller escolhe a mensagem. O DAO nao mostra janelas.
 */
public class PlanoDAO {

    /** O id nao entra: o banco gera (AUTO_INCREMENT). */
    public void inserir(Plano p) throws SQLException {
        String sql = "INSERT INTO plano (nome, descricao, valor_mensal, qualidade_maxima, "
                   + "telas_simultaneas, permite_download, data_criacao) VALUES (?, ?, ?, ?, ?, ?, CURDATE())";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNome());
            ps.setString(2, p.getDescricao());
            ps.setBigDecimal(3, p.getValorMensal());
            ps.setString(4, p.getQualidadeMaxima());
            ps.setInt(5, p.getTelasSimultaneas());
            ps.setBoolean(6, p.isPermiteDownload());

            ps.executeUpdate(); // INSERT, UPDATE e DELETE usam executeUpdate
        }
    }

    /** Devolve todos os planos. */
    public List<Plano> listar() throws SQLException {
        String sql = "SELECT id_plano, nome, descricao, valor_mensal, qualidade_maxima, "
                   + "telas_simultaneas, permite_download FROM plano ORDER BY id_plano";
        List<Plano> lista = new ArrayList<>();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) { // SELECT usa executeQuery e devolve um ResultSet

            // rs.next() avanca uma linha e devolve false no fim.
            while (rs.next()) {
                lista.add(montarPlano(rs));
            }
        }
        return lista;
    }

    /** Altera o plano do id informado. */
    public boolean atualizar(Plano p) throws SQLException {
        String sql = "UPDATE plano SET nome = ?, descricao = ?, valor_mensal = ?, qualidade_maxima = ?, "
                   + "telas_simultaneas = ?, permite_download = ? WHERE id_plano = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNome());
            ps.setString(2, p.getDescricao());
            ps.setBigDecimal(3, p.getValorMensal());
            ps.setString(4, p.getQualidadeMaxima());
            ps.setInt(5, p.getTelasSimultaneas());
            ps.setBoolean(6, p.isPermiteDownload());
            ps.setInt(7, p.getIdPlano()); // sem o WHERE, todos os planos seriam alterados

            return ps.executeUpdate() > 0; // false = nenhuma linha com esse id
        }
    }

    /** Se o plano tiver contas, o MySQL recusa por causa da FK e o Controller mostra a mensagem. */
    public boolean eliminar(int idPlano) throws SQLException {
        String sql = "DELETE FROM plano WHERE id_plano = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPlano);
            return ps.executeUpdate() > 0; // false = nenhuma linha com esse id
        }
    }

    /** Converte a linha atual do ResultSet em Plano (snake_case do banco para camelCase do Java). */
    private Plano montarPlano(ResultSet rs) throws SQLException {
        Plano p = new Plano();
        p.setIdPlano(rs.getInt("id_plano"));
        p.setNome(rs.getString("nome"));
        p.setDescricao(rs.getString("descricao"));
        p.setValorMensal(rs.getBigDecimal("valor_mensal"));
        p.setQualidadeMaxima(rs.getString("qualidade_maxima"));
        p.setTelasSimultaneas(rs.getInt("telas_simultaneas"));
        p.setPermiteDownload(rs.getBoolean("permite_download"));
        return p;
    }
}
