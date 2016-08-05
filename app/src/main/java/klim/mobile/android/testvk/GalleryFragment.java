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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryFragment extends Fragment implements AdapterView.OnItemClickListener {

    private int bundleUserId;
    private VkUser vkUser = VkUser.getInstance();
    private ArrayList<Foto> fotos = new ArrayList<>();
    private GridView gridFoto;


    public GalleryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_gallery, container, false);
        Bundle b = getArguments();
        if ( b!=null ){
            bundleUserId = b.getInt("user_id",Integer.parseInt(vkUser.getUserId()));
        }

        gridFoto = (GridView) v.findViewById(R.id.gridfotos);

        if(vkUser.getFriends().get(findByVKid(bundleUserId)).getPhotos().size() == 0) {
            String s = String.format(Locale.ENGLISH, AuthorizationActivity.VK_API_PHOTOS, bundleUserId, vkUser.getToken(), getResources().getString(R.string.api_version));
            getPhotosAsync photosAsync = new getPhotosAsync(getActivity());
            photosAsync.execute(s);
        }else {
            fotos = vkUser.getFriends().get(findByVKid(bundleUserId)).getPhotos();
            setPotoAdaptor();
        }

        return v;
    }

    private int findByVKid(int findId){
        int i;
        ArrayList<Friend> arr = vkUser.getFriends();
        for(i=0;i<arr.size();i++){
            if(arr.get(i).getId() == findId)
                break;
        }
        return i;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String url = ((Foto) parent.getAdapter().getItem(position)).getUrl_orig();
        FotoFragment ff = new FotoFragment();
        Bundle b = new Bundle();
        b.putString("url",url);
        ff.setArguments(b);
        ((MainActivity) getActivity()).nextFragment(ff);
    }

    private class getPhotosAsync extends AsyncTask<String,Void,JSONObject>{
        Context context;
        ProgressDialog pd;
        public getPhotosAsync(Context c){
            context = c;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context);
            pd.setMessage("идет загрузка фотографий, подождите.");
            pd.setTitle("Загрузка...");
            pd.show();
        }


        @Override
        protected JSONObject doInBackground(String... params) {
            String url = params[0];
            JSONObject object = null;
            try {
                object = NetUtil.getJSON(url);
            } catch (IOException e) {
                Log.e("vk_log","getPhotosAsync getJSON "+e.getMessage());
                e.printStackTrace();
            } catch (JSONException e) {
                Log.e("vk_log","getPhotosAsync getJSON "+e.getMessage());
                e.printStackTrace();
            }
            return object;
        }

        @Override
        protected void onPostExecute(JSONObject object) {
            super.onPostExecute(object);
            pd.dismiss();
            if(object != null)
                if(object.has("response"))
                    try {
                        JSONObject response = object.getJSONObject("response");
                        loadImg(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                else
                    Toast.makeText(context,"В ответе нет требуемых данных",Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context,"Не удалось загрузить данные",Toast.LENGTH_SHORT).show();
        }
    }

    private class downloadMiniature extends AsyncTask<Void,Void,Void>{
        Context context;
        ProgressDialog dbLoadDialog;

        downloadMiniature(Context c){
            context =c;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dbLoadDialog = new ProgressDialog(context);
            dbLoadDialog.setTitle("Идет загрузка фотографий");
            dbLoadDialog.setMessage("пожалуйста дождитесь окончания загрузки");
            dbLoadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dbLoadDialog.setMax(fotos.size());
            dbLoadDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Bitmap mIcon;
            for(int i=0;i<fotos.size();i++){
                mIcon = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.camera_c));
                try {
                    InputStream in = new java.net.URL(fotos.get(i).getUrl_mini()).openStream();
                    mIcon = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
                fotos.get(i).setMiniature(mIcon);
                dbLoadDialog.setProgress(i);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dbLoadDialog.dismiss();
            vkUser.getFriends().get(findByVKid(bundleUserId)).setPhotos(fotos);
            setPotoAdaptor();
        }
    }

    private void setPotoAdaptor() {
        miniatureAdapter adapter = new miniatureAdapter(getActivity(),fotos);
        gridFoto.setAdapter(adapter);
        gridFoto.setOnItemClickListener(this);
    }

    private class miniatureAdapter extends BaseAdapter{
        ArrayList<Foto> items;
        private LayoutInflater mInflater;
        private ImageView viewHolder;

        miniatureAdapter(Context c,ArrayList<Foto> f){
            items = f;
            mInflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.item_poto, null);
                viewHolder = (ImageView) convertView.findViewById(R.id.mini_foto);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ImageView) convertView.getTag();
            }

            viewHolder.setImageBitmap(items.get(position).getMiniature());
            return convertView;
        }
    }

    private void loadImg(JSONObject response) {
        int tmpId;
        String tmpUrlm, tmpUrlo;
        try {
            int count = response.getInt("count");
            JSONArray array = response.getJSONArray("items");
            for(int i=0; i< array.length();i++){
                tmpUrlm = tmpUrlo = "";
                JSONObject tmp = array.getJSONObject(i);
                tmpId = tmp.getInt("id");
                JSONArray sizes = tmp.getJSONArray("sizes");
                for(int j=0; j < sizes.length();j++){
                    if(sizes.getJSONObject(j).getString("type").equals("m"))
                        tmpUrlm = sizes.getJSONObject(j).getString("src");
                    if(sizes.getJSONObject(j).getString("type").equals("x"))
                        tmpUrlo = sizes.getJSONObject(j).getString("src");
                }
                fotos.add(new Foto(tmpId,tmpUrlm,tmpUrlo,null));
            }
        } catch (JSONException e) {
            Log.e("vk_log","loadImg getJSON "+e.getMessage());
            e.printStackTrace();
        }
        downloadMiniature dm = new downloadMiniature(getActivity());
        dm.execute();
    }
}
