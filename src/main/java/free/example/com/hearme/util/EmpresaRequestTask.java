package free.example.com.hearme.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import free.example.com.hearme.dao.EmpresaDao;
import free.example.com.hearme.model.Empresa;

/**
 * Created by laecio on 21/09/16.
 */
public class EmpresaRequestTask extends AsyncTask<String, Void, Empresa[]> {
    // private List<Empresa> Empresaslista = new ArrayList<Empresa>();
    @Override
    protected Empresa[] doInBackground(String... params) {
//        String path = params[0];
        try {
            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();
            // Add the Jackson message converter
            restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
            // Make the HTTP GET request, marshaling the response from JSON to an array of Events
            Empresa[] Empresas = restTemplate.getForObject(Repositorio.URL + "listarEmpresa", Empresa[].class);
            // Empresaslista = Arrays.asList(Empresas);
            return Empresas;
        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }

        return null;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onPostExecute(Empresa[] a) {

    }

}

