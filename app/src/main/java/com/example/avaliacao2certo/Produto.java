package com.example.avaliacao2certo;

import java.io.Serializable;

public class Produto implements Serializable {
    private int id;
    private String descricao;
    private double preco;
    private double estoque;
    private Setor setor;  // Referência ao setor

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public double getEstoque() { return estoque; }
    public void setEstoque(double estoque) { this.estoque = estoque; }

    public Setor getSetor() { return setor; }
    public void setSetor(Setor setor) { this.setor = setor; }

    public String toString() {
        return String.valueOf(id)+" - "+descricao+" - "+ preco + " - " + setor;
    }

}