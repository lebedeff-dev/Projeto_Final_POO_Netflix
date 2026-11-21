package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Conta;
import util.ConnectionFactory;

/**
 * DAO de Conta, no molde do PlanoDAO. id_plano e FK: gravamos so o numero e o MySQL confere
 * se o plano existe.
 */
public class ContaDAO {

    public void inserir(Conta c) throws SQLException {
        String sql = "INSERT INTO conta (id_plano, email, senha_hash, telefone, pais, data_cadastro, status) "
                   + "VALUES (?, ?, ?, ?, ?, NOW(), ?)";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, c.getIdPlano());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getSenhaHash());
            ps.setString(4, c.getTelefone()); // null vira NULL no banco
            ps.setString(5, c.getPais());
            ps.setString(6, c.getStatus());
            ps.executeUpdate();
        }
    }

    public List<Conta> listar() throws SQLException {
        // INNER JOIN com plano para trazer o nome junto do id_plano.
        String sql = "SELECT c.id_conta, c.id_plano, c.email, c.senha_hash, c.telefone, c.pais, "
                   + "c.status, p.nome AS nome_plano "
                   + "FROM conta c INNER JOIN plano p ON p.id_plano = c.id_plano "
                   + "ORDER BY c.id_conta";
        List<Conta> lista = new ArrayList<>();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Conta c = new Conta();
                c.setIdConta(rs.getInt("id_conta"));
                c.setIdPlano(rs.getInt("id_plano"));
                c.setEmail(rs.getString("email"));
                c.setSenhaHash(rs.getString("senha_hash"));
                c.setTelefone(rs.getString("telefone")); // NULL vira null
                c.setPais(rs.getString("pais"));
                c.setStatus(rs.getString("status"));
                c.setNomePlano(rs.getString("nome_plano")); // coluna que veio do JOIN
                lista.add(c);
            }
        }
        return lista;
    }

    public boolean atualizar(Conta c) throws SQLException {
        String sql = "UPDATE conta SET id_plano = ?, email = ?, senha_hash = ?, telefone = ?, pais = ?, "
                   + "status = ? WHERE id_conta = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, c.getIdPlano());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getSenhaHash());
            ps.setString(4, c.getTelefone());
            ps.setString(5, c.getPais());
            ps.setString(6, c.getStatus());
            ps.setInt(7, c.getIdConta());
            return ps.executeUpdate() > 0; // false = nenhuma linha com esse id
        }
    }

    /** Lanca SQLException se a conta ainda tiver perfis (FK). */
    public boolean eliminar(int idConta) throws SQLException {
        String sql = "DELETE FROM conta WHERE id_conta = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idConta);
            return ps.executeUpdate() > 0; // false = nenhuma linha com esse id
        }
    }
}
