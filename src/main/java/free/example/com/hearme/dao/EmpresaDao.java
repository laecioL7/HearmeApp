package free.example.com.hearme.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import free.example.com.hearme.model.Empresa;
import free.example.com.hearme.model.Teste;

/**
 * Created by laecio on 17/10/16.
 */
public class EmpresaDao {
    private DataBaseHelper helper;
    private SQLiteDatabase db; // instanciar só quando usar

    public EmpresaDao(Context context) {
        helper = new DataBaseHelper(context);
    }

    private SQLiteDatabase getDb() {
        if (db == null) {
            db = helper.getWritableDatabase(); // se não tiver ele pega a database lá no helper
        }
        return db;
    }

    public List<Empresa> buscarEmpresas() { // para pegar os Empresas do banco
        List<Empresa> lista = new ArrayList<Empresa>();
        Empresa e = null;
        Cursor cursor; // cursor para procurar no banco
        cursor = getDb().rawQuery("select * from empresa", null); // args são
        while (cursor.moveToNext()) {
            e = new Empresa();
            e.setId(cursor.getLong((cursor.getColumnIndex("_id"))));
            e.setNome(cursor.getString((cursor.getColumnIndex("nome"))));
            e.setCodigo(cursor.getLong(cursor.getColumnIndex("codigo")));
            //enum fica salva como numero 0 ou 1 no caso
            e.setTesta(Teste.values()[cursor.getInt(cursor.getColumnIndex("testa"))]);
            lista.add(e);
        }
        cursor.close();
        return lista;
    }// fecha o metodo


    public List<Empresa> buscarEmpresasqTestam() { // para pegar os Empresas do banco
        List<Empresa> lista = new ArrayList<Empresa>();
        Empresa e = null;
        Cursor cursor; // cursor para procurar no banco
        cursor = getDb().rawQuery("select * from empresa WHERE testa = 0 ORDER BY nome", null); // args são
        while (cursor.moveToNext()) {
            e = new Empresa();
            e.setId(cursor.getLong((cursor.getColumnIndex("_id"))));
            e.setNome(cursor.getString((cursor.getColumnIndex("nome"))));
            e.setCodigo(cursor.getLong(cursor.getColumnIndex("codigo")));
            //enum fica salva como numero 0 ou 1 no caso
            e.setTesta(Teste.values()[cursor.getInt(cursor.getColumnIndex("testa"))]);
            lista.add(e);
        }
        cursor.close();
        return lista;
    }// fecha o metodo

    public List<Empresa> buscarEmpresasqNaoTestam() { // para pegar os Empresas do banco
        List<Empresa> lista = new ArrayList<Empresa>();
        Empresa e = null;
        Cursor cursor; // cursor para procurar no banco
        cursor = getDb().rawQuery("select * from empresa WHERE testa = 1 ORDER BY nome", null); // args são
        while (cursor.moveToNext()) {
            e = new Empresa();
            e.setId(cursor.getLong((cursor.getColumnIndex("_id"))));
            e.setNome(cursor.getString((cursor.getColumnIndex("nome"))));
            e.setCodigo(cursor.getLong(cursor.getColumnIndex("codigo")));
            //enum fica salva como numero 0 ou 1 no caso
            e.setTesta(Teste.values()[cursor.getInt(cursor.getColumnIndex("testa"))]);
            lista.add(e);
        }
        cursor.close();
        return lista;
    }// fecha o metodo

    public long salvar(Empresa e) {
        long retorno;
        // salvando um objeto to to to
        ContentValues values = new ContentValues();
        values.put("_id", e.getId());
        values.put("nome", e.getNome()); //coluna e valor em si
        values.put("codigo", e.getCodigo()); //enum para inteiro
        values.put("testa", e.getTesta().ordinal()); //enum para inteiro
        //insert
        retorno = getDb().insert("empresa", null, values);
        // retorna um numero maior que 0 para sucesso e diferente para falha
        return retorno;
    }

    public long alterar(Empresa e) {
        long retorno;
        ContentValues values = new ContentValues();
        values.put("_id", e.getId());
        values.put("nome", e.getNome()); //coluna e valor em si
        values.put("codigo", e.getCodigo()); //enum para inteiro
        values.put("testa", e.getTesta().ordinal()); //enum para inteiro
        //faz uma verificação aqui
        //insert ou update
        //update
        retorno = getDb().update("empresa", values, "_id= ?", new String[]{e.getId().toString()});
        // onde id for igual a vetor para cada validação....
        return retorno;
    }

    public Empresa buscarPorCodigo(long parametro) {
        Empresa e = null;
        Cursor cursor; // cursor para procurar no banco
        cursor = getDb().rawQuery("select * from empresa WHERE codigo = ?", new String[]{String.valueOf(parametro)}); //args são para passar para o ?
        if (cursor.moveToFirst()) {
            e = new Empresa();
            e.setId(cursor.getLong((cursor.getColumnIndex("_id"))));// busca o indice da coluna de id e passa para o metodo de fora que pega a String
            e.setNome(cursor.getString((cursor.getColumnIndex("nome"))));
            e.setCodigo(cursor.getLong(cursor.getColumnIndex("codigo")));
            //enum fica salva como numero 0 ou 1 no caso
            e.setTesta(Teste.values()[cursor.getInt(cursor.getColumnIndex("testa"))]);//pega o indice salvo no banco e joga no objeto
        }
        cursor.close();
        return e;
    }

    public Empresa buscarEmpresa() { //apenas para pegar a Empresa do banco
        Empresa e = null;
        Cursor cursor; // cursor para procurar no banco
        cursor = getDb().rawQuery("select * from empresa", null); //args são para passar para o ?
        if (cursor.moveToFirst()) {
            e = new Empresa();
            e.setId(cursor.getLong((cursor.getColumnIndex("_id"))));// busca o indice da coluna de id e passa para o metodo de fora que pega a String
            e.setNome(cursor.getString((cursor.getColumnIndex("nome"))));
            e.setCodigo(cursor.getLong(cursor.getColumnIndex("codigo")));
            //enum fica salva como numero 0 ou 1 no caso
            e.setTesta(Teste.values()[cursor.getInt(cursor.getColumnIndex("testa"))]);//pega o indice salvo no banco e joga no objeto
        }
        cursor.close();
        return e;
    }//fecha o metodo

    public void close() {
        helper.close();
        if (db != null && db.isOpen()) {
            db.close();
        }

    }

}
