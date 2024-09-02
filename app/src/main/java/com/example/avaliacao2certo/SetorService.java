package com.example.avaliacao2certo;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SetorService extends IntentService {

    public static final String ACTION_LISTAR = "com.example.setores.action.LISTAR";
    public static final String ACTION_CADASTRAR = "com.example.setores.action.CADASTRAR";
    public static final String RESULTADO_LISTA_SETORES = "com.example.setores.RESULTADO_LISTA_SETORES";
    public static final String ACTION_LISTAR_POR_ID = "com.example.setores.action.ACTION_LISTAR_POR_ID";
    public static final String ACTION_ATUALIZAR = "com.example.setores.action.ATUALIZAR";
    public static final String ACTION_EXCLUIR = "com.example.setores.action.EXCLUIR";

    static final String URL_WS = "http://argo.td.utfpr.edu.br/clients/ws/setor";

    Gson gson;

    public SetorService() {
        super("SetorService");
        gson = new GsonBuilder().create();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null)
            return;
        switch (intent.getAction()) {
            case ACTION_CADASTRAR: cadastrar(intent);
                break;
            case ACTION_ATUALIZAR: atualizar(intent);
                break;
            case ACTION_LISTAR_POR_ID: listarPorId(intent);
                break;
            case ACTION_LISTAR: listar(intent);
                break;
            case ACTION_EXCLUIR: excluir(intent);
                break;
        }
    }

    private void cadastrar(Intent intent) {
        try {
            Setor set = (Setor) intent.getSerializableExtra("setor");
            String strSetor = gson.toJson(set);

            URL url = new URL(URL_WS);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("content-type", "application/json");
            con.setDoOutput(true);
            con.connect();

            try (OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream())) {
                writer.write(strSetor);
                writer.flush();
            }

            if (con.getResponseCode() == 200) {
                Log.d("POST", "Cadastro OK");
            } else {
                Log.e("POST", "Erro no cadastro. Código: " + con.getResponseCode());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void excluir(Intent intent) {
        try {
            int id = intent.getIntExtra("id", -1);
            if (id == -1) {
                throw new IllegalArgumentException("ID inválido ou não fornecido");
            }

            URL url = new URL(URL_WS + "/" + id);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("DELETE");
            con.setRequestProperty("content-type", "application/json");
            con.connect();

            if (con.getResponseCode() == 200) {
                Log.d("DELETE", "Exclusão OK");
            } else {
                Log.d("DELETE", "Falha na exclusão. Código de resposta: " + con.getResponseCode());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void listar(Intent intent) {
        try {
            URL url = new URL(URL_WS);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect();
            if (con.getResponseCode() == 200) {
                BufferedReader ent = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                StringBuilder bld = new StringBuilder(1000);
                String linha;
                while ((linha = ent.readLine()) != null) {
                    bld.append(linha);
                }
                Setor[] setores = gson.fromJson(bld.toString(), Setor[].class);
                Intent it = new Intent(RESULTADO_LISTA_SETORES);
                it.putExtra("setores", setores);
                sendBroadcast(it);
            } else {
                Log.e("GET", "Erro na listagem. Código: " + con.getResponseCode());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void listarPorId(Intent intent) {
        try {
            int id = intent.getIntExtra("id", -1);
            if (id == -1) {
                throw new IllegalArgumentException("ID inválido ou não fornecido");
            }

            URL url = new URL(URL_WS + "/" + id);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect();

            if (con.getResponseCode() == 200) {
                BufferedReader ent = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                StringBuilder bld = new StringBuilder(1000);
                String linha;
                while ((linha = ent.readLine()) != null) {
                    bld.append(linha);
                }

                Setor setor = gson.fromJson(bld.toString(), Setor.class);

                Intent it = new Intent(RESULTADO_LISTA_SETORES);
                it.putExtra("setor", setor);
                sendBroadcast(it);
            } else {
                Log.e("GET_BY_ID", "Erro ao listar por ID. Código: " + con.getResponseCode());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void atualizar(Intent intent) {
        try {
            Setor set = (Setor) intent.getSerializableExtra("setor");
            String strSetor = gson.toJson(set);

            URL url = new URL(URL_WS + "/" + set.getId());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("PUT");
            con.setRequestProperty("content-type", "application/json");
            con.setDoOutput(true);
            con.connect();

            try (OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream())) {
                writer.write(strSetor);
                writer.flush();
            }

            if (con.getResponseCode() == 200) {
                Log.d("PUT", "Atualização OK");
            } else {
                Log.e("PUT", "Erro na atualização. Código: " + con.getResponseCode());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}