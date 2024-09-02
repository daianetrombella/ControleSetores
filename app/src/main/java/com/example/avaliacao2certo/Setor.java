package com.example.avaliacao2certo;

import java.io.Serializable;
import java.util.Objects;

public class Setor implements Serializable {
    private int id;
    private String descricao;
    private double margem;

    public Setor(String descricao, double margem) {
        this.descricao = descricao;
        this.margem = margem;
    }

    public Setor() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getMargem() {
        return margem;
    }

    public void setMargem(double margem) {
        this.margem = margem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Setor)) return false;
        Setor setor = (Setor) o;
        return id == setor.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String toString() {
        return String.valueOf(id)+" - "+descricao+" - "+margem;
    }
}
