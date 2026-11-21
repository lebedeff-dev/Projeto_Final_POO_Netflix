package model;

import java.math.BigDecimal;

/**
 * Plano de assinatura (tabela plano). So guarda dados, sem acesso a banco nem regra de negocio.
 * O DAO faz a conversao de camelCase para snake_case (valorMensal vira valor_mensal).
 */
public class Plano {

    private int idPlano;
    private String nome;
    private String descricao;
    private BigDecimal valorMensal;   // DECIMAL; double erra o arredondamento de dinheiro
    private String qualidadeMaxima;   // ENUM('SD','HD','Full HD','4K UHD')
    private int telasSimultaneas;
    private boolean permiteDownload;  // TINYINT(1) = boolean

    // Vazio: o DAO usa ao ler do ResultSet.
    public Plano() {
    }

    // Completo, sem id (o banco gera com AUTO_INCREMENT).
    public Plano(String nome, String descricao, BigDecimal valorMensal, String qualidadeMaxima,
                 int telasSimultaneas, boolean permiteDownload) {
        this.nome = nome;
        this.descricao = descricao;
        this.valorMensal = valorMensal;
        this.qualidadeMaxima = qualidadeMaxima;
        this.telasSimultaneas = telasSimultaneas;
        this.permiteDownload = permiteDownload;
    }

    public int getIdPlano() { return idPlano; }
    public void setIdPlano(int idPlano) { this.idPlano = idPlano; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValorMensal() { return valorMensal; }
    public void setValorMensal(BigDecimal valorMensal) { this.valorMensal = valorMensal; }

    public String getQualidadeMaxima() { return qualidadeMaxima; }
    public void setQualidadeMaxima(String qualidadeMaxima) { this.qualidadeMaxima = qualidadeMaxima; }

    public int getTelasSimultaneas() { return telasSimultaneas; }
    public void setTelasSimultaneas(int telasSimultaneas) { this.telasSimultaneas = telasSimultaneas; }

    public boolean isPermiteDownload() { return permiteDownload; }
    public void setPermiteDownload(boolean permiteDownload) { this.permiteDownload = permiteDownload; }
}
