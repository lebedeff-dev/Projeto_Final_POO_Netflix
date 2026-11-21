package controller;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import dao.PlanoDAO;
import model.Plano;
import util.Conversor;

/**
 * Controller de Plano. Recebe os textos da View, valida e converte com o Conversor, monta o
 * Plano, chama o DAO e devolve uma mensagem de resultado. A View nao conhece o banco e o DAO
 * nao conhece as telas. Os outros controllers seguem o mesmo padrao.
 */
public class PlanoController {

    private final PlanoDAO dao = new PlanoDAO();

    /** Lista para a tabela da tela. */
    public List<Plano> listar() {
        try {
            return dao.listar();
        } catch (SQLException e) {
            // listar devolve lista, nao mensagem, entao o erro sobe como RuntimeException
            // e a TelaCrud mostra.
            throw new RuntimeException("Erro ao listar planos: " + e.getMessage(), e);
        }
    }

    public String inserir(String nome, String valorMensal, String qualidadeMaxima,
                          String telas, String permiteDownload) {
        try {
            dao.inserir(montar(nome, valorMensal, qualidadeMaxima, telas, permiteDownload));
            return "Plano inserido com sucesso!";
        } catch (IllegalArgumentException e) { // validacao (Conversor)
            return e.getMessage();
        } catch (SQLException e) {             // banco, inclusive MySQL desligado
            return "Erro no banco de dados: " + e.getMessage();
        }
    }

    public String atualizar(String id, String nome, String valorMensal, String qualidadeMaxima,
                            String telas, String permiteDownload) {
        try {
            int idPlano = Conversor.paraId(id);
            Plano p = montar(nome, valorMensal, qualidadeMaxima, telas, permiteDownload);
            p.setIdPlano(idPlano);
            if (!dao.atualizar(p)) {
                return Conversor.REGISTRO_NAO_ENCONTRADO;
            }
            return "Plano atualizado com sucesso!";
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
            return "Plano removido com sucesso!";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (SQLException e) {
            // 1451: o MySQL recusou porque ha contas usando este plano (FK).
            if (e.getErrorCode() == 1451) {
                return "Não é possível remover este plano: há contas que usam ele. "
                        + "Troque o plano dessas contas (ou remova-as) antes.";
            }
            return "Erro no banco de dados: " + e.getMessage();
        }
    }

    /**
     * A descricao do plano nao e digitada: sai do numero de telas e da qualidade. Exemplo:
     * ("2", "Full HD") vira "Duas telas, qualidade Full HD". A tela chama este metodo para mostrar
     * a descricao ao vivo, e o montar() chama de novo ao salvar, entao a descricao gravada sempre
     * bate com os campos. Devolve "" se o numero de telas for invalido.
     */
    public String gerarDescricao(String telas, String qualidadeMaxima) {
        int quantidade;
        try {
            quantidade = Integer.parseInt(telas.trim());
        } catch (NumberFormatException e) {
            return "";
        }
        if (quantidade < 1) {
            return "";
        }
        String[] porExtenso = {"", "Uma tela", "Duas telas", "Três telas", "Quatro telas"};
        String telasTexto = quantidade < porExtenso.length ? porExtenso[quantidade] : quantidade + " telas";
        return telasTexto + ", qualidade " + qualidadeMaxima;
    }

    /** Monta o Plano a partir dos textos. inserir e atualizar usam. */
    private Plano montar(String nome, String valorMensal, String qualidadeMaxima,
                         String telas, String permiteDownload) {
        BigDecimal valor = Conversor.paraDecimal(valorMensal, "Valor mensal");
        if (valor.signum() <= 0) { // o valor precisa ser maior que zero
            throw new IllegalArgumentException("O valor mensal deve ser maior que zero.");
        }
        int quantidadeTelas = Conversor.paraInt(telas, "Telas simultâneas");
        if (quantidadeTelas < 1) { // pelo menos 1 tela
            throw new IllegalArgumentException("O plano precisa ter pelo menos 1 tela simultânea.");
        }
        return new Plano(
                Conversor.obrigatorio(nome, "Nome"),
                gerarDescricao(telas, qualidadeMaxima), // descricao automatica
                valor,
                qualidadeMaxima,
                quantidadeTelas,
                Conversor.paraBoolean(permiteDownload));
    }
}
