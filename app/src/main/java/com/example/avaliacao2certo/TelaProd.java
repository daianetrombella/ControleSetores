package com.example.avaliacao2certo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.LinkedList;

public class TelaProd extends AppCompatActivity {
    class ProdutoServiceObserver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ProdutoService.RESULTADO_LISTA_PRODUTOS.equals(intent.getAction())) {
                Produto[] prods = (Produto[]) intent.getSerializableExtra("produtos");
                produtos.clear(); // Refaz a lista do adapter.
                if (prods != null && prods.length > 0) {
                    produtos.addAll(Arrays.asList(prods));
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    public class SetorServiceObserver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                Log.d("TelaProd", "Recebendo Intent: " + intent.getAction());

                if (SetorService.RESULTADO_LISTA_SETORES.equals(intent.getAction())) {
                    if (intent.hasExtra("setor")) {
                        // Caso tenha vindo um único setor
                        Setor setor = (Setor) intent.getSerializableExtra("setor");
                        if (setor != null) {
                            setores.clear(); // Limpa a lista para mostrar apenas o setor buscado
                            setores.add(setor);
                            Log.d("TelaProd", "Setor recebido: " + setor.getDescricao());
                        } else {
                            Log.e("TelaProd", "Setor recebido é nulo.");
                        }
                    } else if (intent.hasExtra("setores")) {
                        // Caso tenha vindo uma lista de setores
                        Setor[] sets = (Setor[]) intent.getSerializableExtra("setores");
                        if (sets != null && sets.length > 0) {
                            setores.clear(); // Limpa a lista para adicionar novos setores
                            setores.addAll(Arrays.asList(sets));
                            for (Setor s : setores) {
                                Log.d("TelaProd", "Setor recebido: " + s.getDescricao());
                            }
                        } else {
                            Log.e("TelaProd", "Lista de setores recebida é nula ou vazia.");
                        }
                    } else {
                        Log.e("TelaProd", "Nenhum setor ou lista de setores recebida no Intent.");
                    }
                } else {
                    Log.e("TelaProd", "Ação não reconhecida: " + intent.getAction());
                }
            } else {
                Log.e("TelaProd", "Intent recebido é nulo ou não possui ação.");
            }
        }
    }

    private LinkedList<Produto> produtos;
    private EditText edDescricao, edPreco, edEstoque, edSetor, edBuscar;
    private ListView lista;
    private ArrayAdapter<Produto> adapter;
    private int selecionado = -1;
    private boolean editando = false;
    private LinkedList<Setor> setores;
    private Setor setor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_produto);

        produtos = new LinkedList<>();
        setores = new LinkedList<>();
        edDescricao = findViewById(R.id.txt_descr_prod);
        edPreco = findViewById(R.id.txt_preco);
        edEstoque = findViewById(R.id.txt_estoque);
        edSetor = findViewById(R.id.txt_descr_setor);
        edBuscar = findViewById(R.id.txt_busca_id);
        lista = findViewById(R.id.lista_produtos);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, produtos);
        lista.setAdapter(adapter);

        lista.setOnItemClickListener((adapterView, view, pos, id) -> {
            selecionado = pos;
            adapter.notifyDataSetChanged();
        });

        lista.setOnItemLongClickListener((adapterView, view, pos, id) -> {
            Produto a = produtos.get(pos);
            String descricao = a.getDescricao();
            try {
                Intent it = new Intent(Intent.ACTION_VIEW);
                it.setData(Uri.parse("nome:" + descricao)); // Corrigido para ACTION_VIEW e uma URI válida
                startActivity(it);
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(TelaProd.this, "Erro ao abrir descrição do produto.", Toast.LENGTH_SHORT).show();
            }
            return true; // Alterado para true para evitar o clique normal após o longo clique
        });

        registerReceiver(new ProdutoServiceObserver(),
                new IntentFilter(ProdutoService.RESULTADO_LISTA_PRODUTOS));
        registerReceiver(new SetorServiceObserver(),
                new IntentFilter(SetorService.RESULTADO_LISTA_SETORES));

        buscarProdutos();
        buscarSetores();
    }

    protected void buscarProdutos() {
        Intent it = new Intent(this, ProdutoService.class);
        it.setAction(ProdutoService.ACTION_LISTAR);
        startService(it);
    }

    public void confirmarProduto(View v) {
        if (setores.isEmpty()) {
            Toast.makeText(this, "Setores ainda não carregados. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Produto p = new Produto();
            p.setDescricao(edDescricao.getText().toString());
            p.setPreco(Double.parseDouble(edPreco.getText().toString()));
            p.setEstoque(Double.parseDouble(edEstoque.getText().toString()));

            String descricaoSetor = edSetor.getText().toString().trim();
            boolean setorEncontrado = false;

            for (Setor s : setores) {
                if (s.getDescricao().equalsIgnoreCase(descricaoSetor)) {
                    setorEncontrado = true;
                    setor = s;
                    break;
                }
            }

            if (!setorEncontrado) {
                Toast.makeText(this, "Setor não encontrado.", Toast.LENGTH_SHORT).show();
                return;
            }

            p.setSetor(setor);

            Intent it = new Intent(this, ProdutoService.class);
            it.setAction(ProdutoService.ACTION_CADASTRAR);
            it.putExtra("produto", p);
            startService(it);

            // Limpar os campos após a confirmação
            edDescricao.setText("");
            edSetor.setText("");
            edEstoque.setText("");
            edPreco.setText("");

            buscarProdutos(); // Atualiza a lista de produtos
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, insira valores válidos.", Toast.LENGTH_SHORT).show();
        }
    }

    public void excluir(View v) {
        if (selecionado == -1) {
            Toast.makeText(this, "Selecione um produto para excluir", Toast.LENGTH_SHORT).show();
            return;
        }

        Produto produto = produtos.get(selecionado);
        int idDoProduto = produto.getId(); // Obtendo o ID do produto selecionado

        Intent intent = new Intent(this, ProdutoService.class);
        intent.setAction(ProdutoService.ACTION_EXCLUIR);
        intent.putExtra("id", idDoProduto);
        startService(intent);

        // Atualizar a lista após a exclusão
        buscarProdutos(); // Chama diretamente a função para atualizar a lista

        // Limpar a seleção e atualizar a interface
        selecionado = -1;
        adapter.notifyDataSetChanged();
    }

    public void editar(View v) {
        if (selecionado == -1) {
            Toast.makeText(this, "Selecione o produto a editar", Toast.LENGTH_SHORT).show();
            return;
        }

        Produto p = produtos.get(selecionado);
        edDescricao.setText(p.getDescricao());
        edPreco.setText(String.valueOf(p.getPreco()));
        edEstoque.setText(String.valueOf(p.getEstoque()));
        edSetor.setText(p.getSetor().getDescricao());
        editando = true;
    }

    public void buscar(View v) {
        try {
            int id = Integer.parseInt(edBuscar.getText().toString());
            Intent it = new Intent(this, ProdutoService.class);
            it.setAction(ProdutoService.ACTION_LISTAR_POR_ID);
            it.putExtra("id", id);
            startService(it);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "ID inválido", Toast.LENGTH_SHORT).show();
        }
    }

    public void limparBusca(View v) {
        buscarProdutos();
        edBuscar.setText("");
    }

    private Setor cadastrarSetor(String descricaoSetor) {
        Setor novoSetor = new Setor();
        novoSetor.setDescricao(descricaoSetor);

        Intent it = new Intent(this, SetorService.class);
        it.setAction(SetorService.ACTION_CADASTRAR);
        it.putExtra("setor", novoSetor);
        startService(it);

        // Aqui você teria que implementar uma maneira de aguardar a resposta do servidor com o novo ID do setor

        return novoSetor;
    }

    protected void buscarSetores() {
        Log.d("TelaProd", "Chamando buscarSetores...");
        Intent it = new Intent(this, SetorService.class);
        it.setAction(SetorService.ACTION_LISTAR);
        startService(it);
    }
}