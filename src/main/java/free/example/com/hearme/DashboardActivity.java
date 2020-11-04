package free.example.com.hearme;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.michael.easydialog.EasyDialog;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;
import com.revmob.RevMob;
import com.revmob.RevMobAdsListener;
import com.revmob.ads.banner.RevMobBanner;
import com.revmob.ads.interstitial.RevMobFullscreen;

import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import free.example.com.hearme.activity.AboutActivity;
import free.example.com.hearme.activity.ListaEmpresaActivity;
import free.example.com.hearme.dao.EmpresaDao;
import free.example.com.hearme.model.Empresa;
import free.example.com.hearme.model.Teste;
import free.example.com.hearme.util.Repositorio;

/**
 * Created by laecio on 13/09/16.
 */
public class DashboardActivity extends AppCompatActivity {
    private TextView statusMessage;
    private TextView barcodeValue;
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";
    private ImageView im;
    private ProgressDialog progress;
    protected Context c = this;
    protected RevMob revmob;
    protected RevMobBanner banner;
    private RevMobFullscreen fullscreen;
    private boolean fullscreenIsLoaded;
    private EmpresaDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        statusMessage = (TextView) findViewById(R.id.status_message);
        barcodeValue = (TextView) findViewById(R.id.barcode_value);
        im = (ImageView) findViewById(R.id.iv_ntesta);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        //myToolbar.setLogo(R.drawable.logosmall);
        myToolbar.setPadding(0, 5, 0, 5);
        myToolbar.setTitle("HearMe");
        myToolbar.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        progress = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progress.setCanceledOnTouchOutside(false);
        /*ads*/
        startRevMobSession();
        fullscreenIsLoaded = false;
        revmob = RevMob.startWithListener(this, new RevMobAdsListener() {
            @Override
            public void onRevMobSessionStarted() {
                loadFullscreen(); // pre-cache it without showing it
            }
        }, "57eaa05b16af1c3d0b3ac013");

    }

    public void clickBarcode(View v) {
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
        intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    public void clickListaTesta(View v) {
        Intent i = new Intent(this, ListaEmpresaActivity.class);
        i.putExtra("testa", true);
        startActivity(i);
    }

    public void clickListaNaoTesta(View v) {
        Intent i = new Intent(this, ListaEmpresaActivity.class);
        i.putExtra("testa", false);
        startActivity(i);
    }

    public void clickAbout(View v) {
        Intent i = new Intent(this, AboutActivity.class);
        startActivity(i);
    }

    public void clickRate(View v) {
        Uri uri = Uri.parse("market://details?id=free.example.com.hearme");
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        } else {
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=free.example.com.hearme")));
        }
    }

    public void clickShare(View v) {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "HearMe");
            String sAux = "\n"+getString(R.string.reco)+"\n\n";
            sAux = sAux + "https://play.google.com/store/apps/details?id=free.example.com.hearme \n\n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "choose one"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    String cd = barcode.displayValue;
                    cd = cd.substring(0, 7);
                    System.out.print("codigo: - - - : " + cd);
                    Log.w("codigo", "codigo da substring:" + cd);
                    dao = new EmpresaDao(this);
                    new EmpresaRequestTask().execute(cd);
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

/*
    public void show(View v) {
        new EasyDialog(DashboardActivity.this)
                .setLayoutResourceId(R.layout.layout_tip_image_text)
                .setGravity(EasyDialog.GRAVITY_TOP)
                .setBackgroundColor(Color.parseColor("#00796B"))
                .setLocationByAttachedView(im)
                .setAnimationTranslationShow(EasyDialog.DIRECTION_X, 350, 400, 0)
                .setAnimationTranslationDismiss(EasyDialog.DIRECTION_X, 350, 0, 400)
                .setTouchOutsideDismiss(true)
                .setMatchParent(false)
                .setMarginLeftAndRight(24, 24)
                .setMatchParent(true)
                .setOutsideColor(Color.parseColor("#009688"))
                .setOnEasyDialogDismissed(new EasyDialog.OnEasyDialogDismissed() {
                    @Override
                    public void onDismissed() {
                        Toast.makeText(DashboardActivity.this, "dismiss", Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnEasyDialogShow(new EasyDialog.OnEasyDialogShow() {
                    @Override
                    public void onShow() {
                        Toast.makeText(DashboardActivity.this, "show", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
*/
    public void showPopup(int layout) {
        new EasyDialog(DashboardActivity.this)
                .setLayoutResourceId(layout)
                .setGravity(EasyDialog.GRAVITY_TOP)
                .setBackgroundColor(Color.parseColor("#00796B"))
                .setLocationByAttachedView(im)
                .setAnimationTranslationShow(EasyDialog.DIRECTION_X, 350, 400, 0)
                .setAnimationTranslationDismiss(EasyDialog.DIRECTION_X, 350, 0, 400)
                .setTouchOutsideDismiss(true)
                .setMatchParent(false)
                .setMarginLeftAndRight(24, 24)
                .setMatchParent(true)
                .setOutsideColor(Color.parseColor("#009688"))
                .setOnEasyDialogDismissed(new EasyDialog.OnEasyDialogDismissed() {
                    @Override
                    public void onDismissed() {
                        // Toast.makeText(DashboardActivity.this, "dismiss", Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnEasyDialogShow(new EasyDialog.OnEasyDialogShow() {
                    @Override
                    public void onShow() {
                        //  Toast.makeText(DashboardActivity.this, "show", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private class EmpresaRequestTask extends AsyncTask<String, Void, Empresa> {
        // private List<Empresa> Empresaslista = new ArrayList<Empresa>();
        @Override
        protected Empresa doInBackground(String... params) {
            String codigo = params[0];
            try {
                Empresa ep = dao.buscarPorCodigo(Long.valueOf(codigo));
                if (ep == null) {
                    RestTemplate restTemplate = new RestTemplate();
                    restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
                    Empresa e = restTemplate.getForObject(Repositorio.URL + "PesquisarEmpresa/" + codigo, Empresa.class);
                    return e;
                } else {
                    return ep;
                }
            } catch (Exception e) {
               // Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            progress.setMessage("Pesquisando codigo...");
            progress.show();
        }

        @Override
        protected void onPostExecute(Empresa e) {
            if (progress.isShowing()) {
                progress.dismiss();
            }

            if (e == null) {
                Toast.makeText(c, getString(R.string.nao_encontrada2), Toast.LENGTH_LONG).show();
            } else if (e.getTesta() == Teste.TESTA) {
                showPopup(R.layout.layout_tip_image_text2);
            } else if (e.getTesta() == Teste.NAOTESTA) {
                showPopup(R.layout.layout_tip_image_text);
            }

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        showFullscreen();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        showFullscreen();
    }

    /*ads*/
    public void startRevMobSession() {
        //RevMob's Start Session method:
        revmob = RevMob.startWithListener(this, new RevMobAdsListener() {
            @Override
            public void onRevMobSessionStarted() {
                loadBanner(); // Cache the banner once the session is started
                loadFullscreen();
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


    public void loadFullscreen() {
        //load it with RevMob listeners to control the events fired
        fullscreen = revmob.createFullscreen(this,  new RevMobAdsListener() {
            @Override
            public void onRevMobAdReceived() {
                Log.w("RevMob", "Fullscreen loaded.");
                fullscreenIsLoaded = true;
               // showFullscreen();
            }
            @Override
            public void onRevMobAdNotReceived(String message) {
                Log.w("RevMob", "Fullscreen not received.");
            }
            @Override
            public void onRevMobAdDismissed() {
                Log.w("RevMob", "Fullscreen dismissed.");
            }
            @Override
            public void onRevMobAdClicked() {
                Log.w("RevMob", "Fullscreen clicked.");
            }
            @Override
            public void onRevMobAdDisplayed() {
                Log.w("RevMob", "Fullscreen displayed.");
            }
        });
    }


    public void showFullscreen() {
        if(fullscreenIsLoaded) {
            fullscreen.show(); // call it wherever you want to show the fullscreen ad
        } else {
            //Log.i("RevMob", "Ad not loaded yet.");
        }
    }


}