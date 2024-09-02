package com.example.avaliacao2certo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.LinkedList;

public class TelaSetor extends AppCompatActivity {
    class SetorServiceObserver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SetorService.RESULTADO_LISTA_SETORES)) {
                if (intent.hasExtra("setor")) {
                    // Caso tenha vindo um único setor
                    Setor setor = (Setor) intent.getSerializableExtra("setor");
                    if (setor != null) {
                        setores.clear(); // Limpa a lista para mostrar apenas o setor buscado
                        setores.add(setor);
                    }
                } else {
                    Setor[] sets = (Setor[]) intent.getSerializableExtra("setores");
                    setores.clear(); // refaz a lista do adapter.
                    if (sets != null && sets.length > 0) {
                        setores.addAll(Arrays.asList(sets));
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    LinkedList<Setor> setores;
    EditText edDescricao, edMargem, edBuscar;
    ListView lista;
    ArrayAdapter<Setor> adapter;
    int selecionado = -1;
    Boolean editando = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_setor);
        setores = new LinkedList<>();
        edDescricao = (EditText) findViewById(R.id.txt_descr_setor);
        edMargem = (EditText) findViewById(R.id.txt_margem_setor);
        edBuscar = (EditText) findViewById(R.id.txt_busca_id);
        lista = (ListView) findViewById(R.id.lista_setores);
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, setores);
        lista.setAdapter( adapter );
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                selecionado = pos;
                adapter.notifyDataSetChanged();
            }
        });

        lista.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {

                Setor a = setores.get(pos);
                String descricao = a.getDescricao();
                try {
                    Intent it = new Intent(Intent.ACTION_DIAL);
                    it.setData( Uri.parse("nome:"+ descricao) );
                    startActivity(it);
                } catch(Exception ex) {
                }
                return false;
            }
        });
        registerReceiver(new SetorServiceObserver(),
                new IntentFilter(SetorService.RESULTADO_LISTA_SETORES));
        buscarSetores();
    }

    protected void buscarSetores() {
        Intent it = new Intent(this, SetorService.class);
        it.setAction( SetorService.ACTION_LISTAR );
        startService(it);
    }

    public void buscar(View v){
        try {
            int id = Integer.parseInt(edBuscar.getText().toString());
            Intent it = new Intent(this, SetorService.class);
            it.setAction(SetorService.ACTION_LISTAR_POR_ID);
            it.putExtra("id", id);  // Passa o ID convertido como inteiro
            startService(it);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "ID inválido", Toast.LENGTH_SHORT).show();
        }
    }

    public void confirmar(View v) {
        if(editando) {
            Setor a = setores.get(selecionado);
            a.setDescricao(edDescricao.getText().toString());
            a.setMargem(Double.parseDouble(edMargem.getText().toString()));
//            setores.set(selecionado, a);
//            edDescricao.setText("");
//            edMargem.setText("");
//            adapter.notifyDataSetChanged();
//            editando = false;


            Setor setorExistente = setores.get(selecionado);
            a.setId(setorExistente.getId());  // Aqui assumimos que o Setor tem um método setId()

            setores.set(selecionado, a);

            Intent it = new Intent(this, SetorService.class);
            it.setAction(SetorService.ACTION_ATUALIZAR);
            editando = false; // Depois de atualizar, retornamos ao estado de criação

            it.putExtra("setor", a);
            startService(it);

// Atualizar a lista após a operação
            it = new Intent(this, SetorService.class);
            it.setAction(SetorService.ACTION_LISTAR);
            startService(it);

// Limpar os campos de texto
            edDescricao.setText("");
            edMargem.setText("");
            adapter.notifyDataSetChanged();

        }else {
            Setor s = new Setor();
            s.setDescricao(edDescricao.getText().toString());

            String margemTexto = edMargem.getText().toString();
            if(margemTexto.isEmpty()) {
                s.setMargem(0); // Define margem como 0 se o campo estiver vazio
            } else {
                try {
                    double margem = Double.parseDouble(margemTexto);
                    s.setMargem(margem);
                } catch (NumberFormatException e) {
                    // Trate a exceção se a conversão falhar, por exemplo:
                    s.setMargem(0); // Define margem como 0 em caso de erro
                    // Também pode adicionar um Toast para informar o usuário:
                    Toast.makeText(this, "Valor de margem inválido. Usando 0.", Toast.LENGTH_SHORT).show();
                }
            }

            Intent it = new Intent(this, SetorService.class);
            it.setAction(SetorService.ACTION_CADASTRAR);
            it.putExtra("setor", s);
            startService(it);

// Agora, faz a listagem dos setores novamente
            it = new Intent(this, SetorService.class);
            it.setAction(SetorService.ACTION_LISTAR);
            startService(it);

// Limpa os campos de entrada
            edDescricao.setText("");
            edMargem.setText("");
        }
    }

    public void excluir(View v){
        if (selecionado == -1) {
            Toast.makeText(this, "Selecione um setor para excluir", Toast.LENGTH_SHORT).show();
            return;
        }

        Setor setor = setores.get(selecionado);
        int idDoSetor = setor.getId(); // Obtendo o ID do setor selecionado

        Intent intent = new Intent(this, SetorService.class);
        intent.setAction(SetorService.ACTION_EXCLUIR);
        intent.putExtra("id", idDoSetor);
        startService(intent);

        // Atualizar a lista após a exclusão
        Intent atualizarLista = new Intent(this, SetorService.class);
        atualizarLista.setAction(SetorService.ACTION_LISTAR);
        startService(atualizarLista);

        // Limpar a seleção e atualizar a interface
        selecionado = -1;
        adapter.notifyDataSetChanged();
    }

    public void editar(View v) {
        if (lista != null) {
            Setor c =  setores.get( selecionado );
            if (c == null) {
                Toast.makeText(this,"Selecione o setor a editar", Toast.LENGTH_SHORT).show();
            } else {
                edDescricao.setText(c.getDescricao());
                edMargem.setText(String.valueOf(c.getMargem()));
                editando = true;
            }
        }
    }

    public void limparBusca(View v){
        buscarSetores();
        edBuscar.setText("");
    }

    @Override
    public void onSaveInstanceState(Bundle dados) {
        super.onSaveInstanceState(dados);
        dados.putSerializable("LISTA_SETORES", setores);
        dados.putInt("SELECIONADO", selecionado);
    }

}
