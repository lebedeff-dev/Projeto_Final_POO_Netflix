package controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dao.ContaDAO;
import dao.PerfilDAO;
import model.Conta;
import model.Perfil;
import util.Conversor;

/** Controller de Perfil, no padrao do PlanoController. */
public class PerfilController {

    private final PerfilDAO dao = new PerfilDAO();
    private final ContaDAO contaDAO = new ContaDAO(); // para o combo de contas

    public List<Perfil> listar() {
        try {
            return dao.listar();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar perfis: " + e.getMessage(), e);
        }
    }

    /** Itens do combo de contas, no formato "id - email". */
    public List<String> opcoesContas() {
        try {
            List<String> opcoes = new ArrayList<>();
            for (Conta c : contaDAO.listar()) {
                opcoes.add(Conversor.opcao(c.getIdConta(), c.getEmail()));
            }
            return opcoes;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao carregar contas: " + e.getMessage(), e);
        }
    }

    public String inserir(String conta, String nome, String avatar, String infantil, String pin) {
        try {
            dao.inserir(montar(conta, nome, avatar, infantil, pin));
            return "Perfil inserido com sucesso!";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (SQLException e) {
            return "Erro no banco de dados: " + e.getMessage();
        }
    }

    public String atualizar(String id, String conta, String nome, String avatar, String infantil, String pin) {
        try {
            int idPerfil = Conversor.paraId(id);
            Perfil p = montar(conta, nome, avatar, infantil, pin);
            p.setIdPerfil(idPerfil);
            if (!dao.atualizar(p)) {
                return Conversor.REGISTRO_NAO_ENCONTRADO;
            }
            return "Perfil atualizado com sucesso!";
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
            return "Perfil removido com sucesso (o histórico dele também foi apagado).";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (SQLException e) {
            return "Erro no banco de dados: " + e.getMessage();
        }
    }

    private Perfil montar(String conta, String nome, String avatar, String infantil, String pin) {
        String pinValido = Conversor.vazioParaNulo(pin);
        if (pinValido != null && !pinValido.matches("\\d{4}")) { // PIN e opcional, mas precisa ter 4 digitos
            throw new IllegalArgumentException("O PIN deve ter exatamente 4 dígitos.");
        }
        return new Perfil(
                Conversor.extrairId(conta, "Conta"),
                Conversor.obrigatorio(nome, "Nome"),
                Conversor.vazioParaNulo(avatar),
                Conversor.paraBoolean(infantil),
                pinValido);
    }
}
