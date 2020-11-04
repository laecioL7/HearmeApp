package free.example.com.hearme.dao;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import free.example.com.hearme.R;

/**
 * Created by laecio on 17/10/16.
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE = "hearme";
    private static final int VERSAO = 1;

    public DataBaseHelper(Context context) {
        super(context, DATABASE, null, VERSAO);

    }

    //tipos de dados = integer , text, real
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table empresa(_id integer primary key, codigo integer,"
                + "nome text, testa integer);");
/* EXEMPLO:
    db.execSQL("CREATE TABLE obra(_id INTEGER PRIMARY KEY, titulo TEXT, " +
    "data INTEGER, aut_diret TEXT, pag_durac INTEGER, tipo INTEGER, rating REAL, imagem blob,"
    + " id_genero INTEGER, FOREIGN KEY(id_genero) REFERENCES genero(id_genero));");*/
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }
}
