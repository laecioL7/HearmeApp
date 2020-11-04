package free.example.com.hearme.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.purplebrain.adbuddiz.sdk.AdBuddiz;

import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import free.example.com.hearme.DashboardActivity;
import free.example.com.hearme.R;
import free.example.com.hearme.dao.EmpresaDao;
import free.example.com.hearme.model.Empresa;
import free.example.com.hearme.model.Teste;
import free.example.com.hearme.util.EmpresaRequestTask;
import free.example.com.hearme.util.ListEmpresaAdapter;
import free.example.com.hearme.util.Repositorio;

/**
 * Created by laecio on 14/09/16.
 */
public class SplashActivity extends Activity {
    private EmpresaDao dao;
    protected SharedPreferences config;
    protected EmpresaRequestTask ar;
    Context c = this;
    SharedPreferences.Editor editor;
    boolean jasalvou;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
       /* AdBuddiz.setPublisherKey("94dd47e3-513c-42c6-aaf0-bc654474a5b5");
        AdBuddiz.setTestModeActive();
        AdBuddiz.cacheAds(this);*/
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(2000);
                    dao = new EmpresaDao(c);
                    // preferencia instancia
                    config = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    jasalvou = config.getBoolean("salvoulista", false);
                    boolean conectado = verificaConexao();
                    if (conectado) {
                        new HttpRequestTask().execute();
                    } else {
                        //TODO
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent intent = new Intent(SplashActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

        timerThread.start();


    }

    public void fazerInserts(String nome, long codigo, Teste t) {
        Empresa e = new Empresa();
        e.setNome(nome);
        e.setCodigo(codigo);
        e.setTesta(t);
        long linha = dao.salvar(e);
        long gg = 0;
        if (gg == linha) {
            Log.w("banco", "sucesso ao salvar");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dao != null) {
            dao.close();
        }
    }

    public boolean verificaConexao() {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            conectado = true;
        } else {
            conectado = false;
        }
        return conectado;
    }

    /* HTTP REQUEST TASK - classe interna para usar spring android */
    private class HttpRequestTask extends AsyncTask<String, Void, Empresa[]> {
        @Override
        protected Empresa[] doInBackground(String... params) {
            try {
                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();
                // Add the Jackson message converter
                restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
                // Make the HTTP GET request, marshaling the response from JSON to an array of Events
                Empresa[] em = restTemplate.getForObject(Repositorio.URL + "listarEmpresa", Empresa[].class);
                // Empresaslista = Arrays.asList(Empresas);
                List<Empresa> listaDoBanco = dao.buscarEmpresas();
                /*depois de buscar as duas listas*/
                if (em != null) {
                    //verifica se o banco esta vazio, se estiver vazio salva todas no banco
                    if (listaDoBanco.isEmpty() || listaDoBanco.size() < 1) {
//                       Toast.makeText(c, "Lista do banco vazia", Toast.LENGTH_SHORT).show();
                      //  Log.w("1banco", "LISTA DO BANCO VAZIA");
                        for (Empresa p : em) {
                            long linha = dao.salvar(p);
                            if (linha == 0) {
                               // Log.w("2banco", "sucesso ao salvar");
                            }
                        }
                    } else {
                        /*se o banco não estiver vazio, adiciona as empresas que não tem
                        ; mas se for dia 1 do mês faz o update banco inteiro */
                        int dia = pegarDiadoMes();
                       // Log.w("Dia do mes", "DIA DO Mês: " + dia);
                        if (dia != 1) {
                            if (!(em.length == listaDoBanco.size())) {
                                for (Empresa p : em) {
                                    if (!listaDoBanco.contains(p)) {
                                        long linha = dao.salvar(p);
                                        if (linha == 0) {
                                            Log.w("2banco", "sucesso ao salvar: " + p.getNome());
                                        }
                                    }
                                }

                            }
                        } else {
                            // se for dia 1 faz o update do banco inteiro
                            for (Empresa p : em) {
                                long linha = dao.alterar(p);
                                if (linha == 0) {
                                  //  Log.w("2banco", "sucesso ao salvar");
                                }
                            }
                        }//fim do if dia do mês
                    }
                /*todo*/
                    listaDoBanco = null;
                    listaDoBanco = dao.buscarEmpresas();
                    if (listaDoBanco.size() >= 1) {
                        editor = config.edit();
                        editor.putBoolean("salvoulista", true);
                        editor.commit();
                    }
                }//fim else
                return em;
            } catch (Exception e) {
               // Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(Empresa[] p) {

            if (p == null) {

            }


        }
    }

    public int pegarDiadoMes() {
        GregorianCalendar gc = new GregorianCalendar();
        int dayOfMonth = gc.get(GregorianCalendar.DAY_OF_MONTH);
        return dayOfMonth;
    }

}
