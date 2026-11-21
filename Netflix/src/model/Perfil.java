package model;


/** Perfil dentro de uma conta (tabela perfil). Uma conta tem varios perfis (id_conta). */
public class Perfil {

    private int idPerfil;
    private int idConta;             // FK -> conta.id_conta
    private String nome;
    private String avatar;
    private boolean perfilInfantil;  // perfil_infantil (TINYINT(1))
    private String pin;              // CHAR(4), opcional

    // So para exibir, nao existe na tabela. O PerfilDAO.listar() preenche via INNER JOIN com conta.
    private String emailConta;

    public Perfil() {
    }

    public Perfil(int idConta, String nome, String avatar, boolean perfilInfantil, String pin) {
        this.idConta = idConta;
        this.nome = nome;
        this.avatar = avatar;
        this.perfilInfantil = perfilInfantil;
        this.pin = pin;
    }

    public int getIdPerfil() { return idPerfil; }
    public void setIdPerfil(int idPerfil) { this.idPerfil = idPerfil; }

    public int getIdConta() { return idConta; }
    public void setIdConta(int idConta) { this.idConta = idConta; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }


    public boolean isPerfilInfantil() { return perfilInfantil; }
    public void setPerfilInfantil(boolean perfilInfantil) { this.perfilInfantil = perfilInfantil; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public String getEmailConta() { return emailConta; }
    public void setEmailConta(String emailConta) { this.emailConta = emailConta; }
}
