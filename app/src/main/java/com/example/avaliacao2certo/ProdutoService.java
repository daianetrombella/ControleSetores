package com.example.avaliacao2certo;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProdutoService extends IntentService {

    public static final String ACTION_LISTAR = "com.example.produtos.action.LISTAR";
    public static final String ACTION_CADASTRAR = "com.example.produtos.action.CADASTRAR";
    public static final String ACTION_ATUALIZAR = "com.example.produtos.action.ATUALIZAR";
    public static final String RESULTADO_LISTA_PRODUTOS = "com.example.produtos.RESULTADO_LISTA_PRODUTOS";
    public static final String ACTION_LISTAR_POR_ID  = "com.example.produtos.action.LISTAR_POR_ID"; // Corrigido o namespace da ação
    public static final String ACTION_EXCLUIR = "com.example.produtos.action.EXCLUIR"; // Nova ação para exclusão

    static final String URL_WS = "http://argo.td.utfpr.edu.br/clients/ws/produto";

    Gson gson;

    public ProdutoService() {
        super("ProdutoService");
        gson = new GsonBuilder().create();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null)
            return;
        switch (intent.getAction()) {
            case ACTION_CADASTRAR:
                cadastrar(intent);
                break;
            case ACTION_LISTAR:
                listar(intent);
                break;
            case ACTION_ATUALIZAR:
                atualizar(intent);
                break;
            case ACTION_LISTAR_POR_ID:
                listarPorId(intent);
                break;
            case ACTION_EXCLUIR:  // Adicionando novo caso
                excluir(intent);
                break;
        }
    }

    private void cadastrar(Intent intent) {
        try {
            Produto produto = (Produto) intent.getSerializableExtra("produto");
            String strProduto = gson.toJson(produto);

            URL url = new URL(URL_WS);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("content-type", "application/json");
            con.connect();
            PrintWriter writer = new PrintWriter(con.getOutputStream());
            writer.println(strProduto);
            writer.flush();
            if (con.getResponseCode() == 200) {
                Log.d("POST", "OK");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void excluir(Intent intent) {
        try {
            // Obtendo o ID do produto a ser excluído
            int id = intent.getIntExtra("id", -1);
            if (id == -1) {
                throw new IllegalArgumentException("ID inválido ou não fornecido");
            }

            // Constrói a URL com o ID para a requisição DELETE
            URL url = new URL(URL_WS + "/" + id);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("DELETE");
            con.connect();

            // Verifica o código de resposta
            if (con.getResponseCode() == 200) {
                Log.d("DELETE", "OK");
            } else {
                Log.e("DELETE", "Erro ao excluir produto: " + con.getResponseCode());
                Intent it = new Intent(RESULTADO_LISTA_PRODUTOS);
                it.putExtra("erro", "Erro ao excluir produto: " + con.getResponseCode());
                sendBroadcast(it);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Intent it = new Intent(RESULTADO_LISTA_PRODUTOS);
            it.putExtra("erro", "Erro: " + ex.getMessage());
            sendBroadcast(it);
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
                do {
                    linha = ent.readLine();
                    if (linha != null) {
                        bld.append(linha);
                    }
                } while (linha != null);
                Produto[] produtos = gson.fromJson(bld.toString(), Produto[].class);
                Intent it = new Intent(RESULTADO_LISTA_PRODUTOS);
                it.putExtra("produtos", produtos);
                sendBroadcast(it);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void listarPorId(Intent intent) {
        try {
            // Obtendo o ID a partir do Intent
            int id = intent.getIntExtra("id", -1);
            if (id == -1) {
                throw new IllegalArgumentException("ID inválido ou não fornecido");
            }

            // Constrói a URL com o ID
            URL url = new URL(URL_WS + "/" + id);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect();

            if (con.getResponseCode() == 200) {
                BufferedReader ent = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                StringBuilder bld = new StringBuilder(1000);
                String linha;
                do {
                    linha = ent.readLine();
                    if (linha != null) {
                        bld.append(linha);
                    }
                } while (linha != null);

                // Usando Gson para converter a resposta JSON em um objeto Produto
                Produto produto = gson.fromJson(bld.toString(), Produto.class);

                // Criando um Intent para enviar o resultado como um broadcast
                Intent it = new Intent(RESULTADO_LISTA_PRODUTOS);
                if (produto != null) {
                    // Aqui, o resultado precisa ser enviado como um array para ser compatível com a interface do receiver
                    it.putExtra("produtos", new Produto[]{produto});  // Enviando o produto encontrado como array
                } else {
                    it.putExtra("erro", "Produto não encontrado");
                }
                sendBroadcast(it);
            } else {
                // Tratamento caso a resposta não seja 200 (OK)
                Intent it = new Intent(RESULTADO_LISTA_PRODUTOS);
                it.putExtra("erro", "Erro na conexão com o servidor: " + con.getResponseCode());
                sendBroadcast(it);
            }
        } catch(Exception ex) {
            // Tratamento de exceção e envio de mensagem de erro
            ex.printStackTrace();
            Intent it = new Intent(RESULTADO_LISTA_PRODUTOS);
            it.putExtra("erro", "Erro: " + ex.getMessage());
            sendBroadcast(it);
        }
    }

    private void atualizar(Intent intent) {
        try {
            Produto produto = (Produto) intent.getSerializableExtra("produto");
            String strProduto = gson.toJson(produto);

            URL url = new URL(URL_WS + "/" + produto.getId());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("PUT");
            con.setRequestProperty("content-type", "application/json");
            con.connect();
            PrintWriter writer = new PrintWriter(con.getOutputStream());
            writer.println(strProduto);
            writer.flush();
            if (con.getResponseCode() == 200) {
                Log.d("PUT", "OK");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}