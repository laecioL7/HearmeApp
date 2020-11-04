package free.example.com.hearme.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import free.example.com.hearme.R;
import free.example.com.hearme.model.Empresa;

/**
 * Created by laecio on 20/09/16.
 */
public class ListEmpresaAdapter extends ArrayAdapter<Empresa> {
    private Context context;
    private List<Empresa> plist = null;
    private Bitmap bmpImagem;
    private Bitmap novabmp;

    public ListEmpresaAdapter(Context context, List<Empresa> plist) {
        super(context, 0, plist);
        this.plist = plist;
        this.context = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Empresa e = plist.get(position);

        if (view == null)
            view = LayoutInflater.from(context).inflate(R.layout.list_itens_empresas, null);
        /*ImageView imgIcone = (ImageView) view.findViewById(R.id.iv_icone);
        imgIcone.setImageResource(R.drawable.logosmall);*/
        TextView txnome = (TextView) view.findViewById(R.id.text_view_empresanome);
        txnome.setText(e.getNome());

        TextView txdesc = (TextView) view.findViewById(R.id.text_view_desc);
        String textoProfissao = String.valueOf(e.getId());
        txdesc.setText(textoProfissao);
       /* RatingBar rb = (RatingBar) view.findViewById(R.id.rb_clas);
        rb.setRating(4);*/
        return view;
    }
}
