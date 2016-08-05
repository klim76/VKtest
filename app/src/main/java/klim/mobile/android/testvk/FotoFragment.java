package klim.mobile.android.testvk;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.InputStream;


/**
 * A simple {@link Fragment} subclass.
 */
public class FotoFragment extends Fragment {


    private String bundleFotoURL;
    private ImageView fotoView;

    public FotoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_foto, container, false);
        Bundle b = getArguments();
        if ( b!=null ){
            bundleFotoURL = b.getString("url");
        }

        fotoView = (ImageView) v.findViewById(R.id.original_foto);

        fotoDownloader downloader = new fotoDownloader(getActivity());
        downloader.execute(bundleFotoURL);
        return v;
    }

    private class fotoDownloader extends AsyncTask<String,Void,Bitmap>{

        ProgressDialog pd;

        public fotoDownloader(Context c){
            pd = new ProgressDialog(c);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Загрузка фотографии...");
            pd.setTitle("wait...");
            pd.show();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            Bitmap mIcon = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.camera_c));
                try {
                    InputStream in = new java.net.URL(url).openStream();
                    mIcon = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("fotoDownloader Error", e.getMessage());
                    e.printStackTrace();
                }
            return mIcon;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            pd.dismiss();
            fotoView.setImageBitmap(bitmap);
        }
    }
}
