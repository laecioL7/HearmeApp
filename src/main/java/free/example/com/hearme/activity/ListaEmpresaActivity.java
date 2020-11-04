package free.example.com.hearme.activity;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.purplebrain.adbuddiz.sdk.AdBuddiz;
import com.revmob.RevMob;
import com.revmob.RevMobAdsListener;
import com.revmob.ads.banner.RevMobBanner;

import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import free.example.com.hearme.R;
import free.example.com.hearme.dao.EmpresaDao;
import free.example.com.hearme.model.Empresa;
import free.example.com.hearme.util.ListEmpresaAdapter;
import free.example.com.hearme.util.Repositorio;

/**
 * Created by laecio on 20/09/16.
 */
public class ListaEmpresaActivity extends AppCompatActivity {
    private Context c = this;
    private ListEmpresaAdapter myadapter;
    private ListView listview;
    private ProgressDialog progress;
    private SearchView searchView;
    private TextView txmsg;
    protected List<Empresa> listaE = new ArrayList<Empresa>();
    protected boolean testa;
    private EmpresaDao dao;
    protected SharedPreferences config;
    protected RevMob revmob;
    protected RevMobBanner banner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empresas_list);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        injetarDepen();
        startRevMobSession();
    }

    public void injetarDepen() {
        listview = (ListView) findViewById(R.id.lv_empresas);
        progress = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progress.setCanceledOnTouchOutside(false);
         /* pegando valores para filtro*/
        txmsg = (TextView) findViewById(R.id.empty);
        txmsg.setVisibility(View.GONE);
        testa = getIntent().getBooleanExtra("testa", true);
        dao = new EmpresaDao(this);
        /* Search Configuration */
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView = (SearchView) findViewById(R.id.sv_nome_empresa);
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        // preferencia instancia
        config = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        /* **** não funciona
        boolean jasalvou = config.getBoolean("salvoulista", false);*/
        if (testa) {
            listaE = dao.buscarEmpresasqTestam();
        } else {
            listaE = dao.buscarEmpresasqNaoTestam();
        }

        if (!listaE.isEmpty()) {
           // Log.w("3JaSalvou", "SIM SIM - "+listaE.size());
            //Toast.makeText(this, "Ja salvou", Toast.LENGTH_SHORT).show();
            myadapter = new ListEmpresaAdapter(c, listaE);
            listview.setAdapter(myadapter);
            myadapter.notifyDataSetChanged();
            txmsg.setVisibility(View.GONE);
            searchView.setVisibility(View.VISIBLE);
        } else {
           // Log.w("3JaSalvou", "Nao Nao my friend");
            //buscar dados do web service
            new HttpRequestTask().execute();
        }

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, "Pesquisando por: " + query, Toast.LENGTH_SHORT).show();
            List<Empresa> lista = new ArrayList<Empresa>();
            for (Empresa p : listaE) {
                if (p.getNome().toLowerCase().contains(query.toLowerCase())) {
                    lista.add(p);
                }
            }

            if (lista.isEmpty()) {
                Toast.makeText(this, getString(R.string.nao_encontrada), Toast.LENGTH_SHORT).show();
                myadapter = new ListEmpresaAdapter(c, listaE);
                listview.setAdapter(myadapter);
                myadapter.notifyDataSetChanged();
            } else {
                myadapter = new ListEmpresaAdapter(c, lista);
                listview.setAdapter(myadapter);
                myadapter.notifyDataSetChanged();
            }

        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            String uri = intent.getDataString();
            Toast.makeText(this, "Suggestion: " + uri, Toast.LENGTH_SHORT).show();
        }
    }


    /* HTTP REQUEST TASK - classe interna para usar spring android */
    private class HttpRequestTask extends AsyncTask<String, Void, Empresa[]> {
        @Override
        protected Empresa[] doInBackground(String... params) {
            Empresa[] emp = null;
            EmpresaDao dao = new EmpresaDao(c);
            long linha;
            String url = "listarTS/";
            if (!testa) {
                url = "listarNTS/";
            }
            try {
                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();
                // Add the Jackson message converter
                restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
                // Make the HTTP GET request, marshaling the response from JSON to an array of Events
                emp = restTemplate.getForObject(Repositorio.URL + url, Empresa[].class);
                listaE = Arrays.asList(emp);
                return emp;
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return emp;
        }

        @Override
        protected void onPreExecute() {
            progress.setMessage("Carregando lista de empresas");
            progress.show();
        }

        @Override
        protected void onPostExecute(Empresa[] p) {
            // Toast.makeText(c, p[0].getProfissao(), Toast.LENGTH_LONG).show();
            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (listaE.isEmpty()) {
                txmsg.setVisibility(View.VISIBLE);
                searchView.setVisibility(View.GONE);
            } else {
                myadapter = new ListEmpresaAdapter(c, listaE);
                listview.setAdapter(myadapter);
                myadapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dao.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //AdBuddiz.showAd(this);
    }


    /*ads*/
    public void startRevMobSession() {
        //RevMob's Start Session method:
        revmob = RevMob.startWithListener(this, new RevMobAdsListener() {
            @Override
            public void onRevMobSessionStarted() {
                loadBanner(); // Cache the banner once the session is started
                //loadFullscreen(); // pre-cache it without showing it
                //  Log.i("RevMob", "Session Started");
            }

            @Override
            public void onRevMobSessionNotStarted(String message) {
                //If the session Fails to start, no ads can be displayed.
                //  Log.i("RevMob", "Session Failed to Start");
            }
        }, "57eaa05b16af1c3d0b3ac013");
    }

    public void loadBanner() {
        banner = revmob.preLoadBanner(this, new RevMobAdsListener() {
            @Override
            public void onRevMobAdReceived() {
                showBanner();
                // Log.i("RevMob", "Banner Ready to be Displayed"); //At this point, the banner is ready to be displayed.
            }

            @Override
            public void onRevMobAdNotReceived(String message) {
                // Log.i("RevMob", "Banner Not Failed to Load");
            }

            @Override
            public void onRevMobAdDisplayed() {
                Log.i("RevMob", "Banner Displayed");
            }
        });
    }

    public void showBanner() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewGroup view = (ViewGroup) findViewById(R.id.bannerLayout);
                view.addView(banner);
                banner.show(); //This method must be called in order to display the ad.
            }
        });
    }

}
