package klim.mobile.android.testvk;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class AuthorizationActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String VK_API_M = "https://api.vk.com/method/";
    private static final String VK_METHOD_FRIENDS = "friends.get?%s";
    private static final String VK_METHOD_USERS = "users.get?user_ids=%s";
    private static final String VK_METHOD_PHOTOS = "photos.getAll?owner_id=%s";
    private static final String VK_API_VERSION = "&v=%s";
    private static final String VK_TOKEN = "&access_token=%s";
    private static final String VK_ORDER = "&order=name";
    private static final String VK_FIELDS = "&fields=photo_50,last_seen,status";
    private static final String VK_PHOTOS_SIZE = "&photo_sizes=1";

    public static final String VK_API_FRIENDS = VK_API_M + VK_METHOD_FRIENDS + VK_ORDER + VK_TOKEN + VK_API_VERSION;
    public static final String VK_API_USERS = VK_API_M + VK_METHOD_USERS + VK_FIELDS + VK_TOKEN + VK_API_VERSION;
    public static final String VK_API_PHOTOS = VK_API_M + VK_METHOD_PHOTOS + VK_PHOTOS_SIZE + VK_TOKEN + VK_API_VERSION;



    private static final String VK_API_A = "https://oauth.vk.com/authorize";
    private static final String CLIENT = "?client_id=%s";
    private static final String DISPLAY = "&display=mobile";
    private static final String REDIRECT = "&redirect_uri=https://oauth.vk.com/blank.html";
    private static final String SCOPE = "&scope=friends,photos";
    private static final String RESPONSE = "&response_type=token";
    public static final String VK_API_AUTH = VK_API_A + CLIENT + DISPLAY + REDIRECT + SCOPE + RESPONSE + VK_API_VERSION;

    private DbManager dbManager;
    private Dialog dialog;
    private VKsession session;
    private VkUser vkUser = VkUser.getInstance();
    private EditText etPin;
    private Button btnOk;
    private String token;
    private String expires;
    private String user;
    private String pin;
    private int attempt = 3;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbManager.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);
        dbManager = new DbManager(this);
        dbManager.open();
        session = new VKsession(this);
        //проверяем есть ли сохраненные учетные данные
        if(isNotEmpty(session.getAccessToken())){
            //если есть - проверяем не истекло ли время жизни токена
            if(!session.isExpires()){
                dialogPinGet();
            } else {
                //в противном случае - отправляем на авторизацияю
                loadWebView();
            }
        } else {
            //в противном случае - отправляем на авторизацияю
            loadWebView();
        }
    }

    private void loadWebView(){
        WebView webView = (WebView) findViewById(R.id.web);
        webView.setWebViewClient(new authVKViewClient());

        String clId = getResources().getString(R.string.client_id);
        String api = getResources().getString(R.string.api_version);
        //загружаем страничку авторизации
        webView.loadUrl(String.format(Locale.ENGLISH,VK_API_AUTH ,clId,api));
    }

    private void dialogPinSet() {

        dialog = new Dialog(this);
        dialog.setTitle("Установка пинкода");
        dialog.setContentView(R.layout.pin_dialog);
        TextView message = (TextView) dialog.findViewById(R.id.dlg_pin_tv);
        message.setText(R.string.pin_set_dlg);
        etPin = (EditText) dialog.findViewById(R.id.dlg_pin_et);
        btnOk = (Button) dialog.findViewById(R.id.dlg_pin_btn);
        btnOk.setOnClickListener(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void dialogPinGet() {
        dialog = new Dialog(this);
        dialog.setTitle("Введите пин");
        dialog.setContentView(R.layout.pin_dialog);
        TextView message = (TextView) dialog.findViewById(R.id.dlg_pin_tv);
        message.setText(R.string.pin_get_dlg);
        etPin = (EditText) dialog.findViewById(R.id.dlg_pin_et);
        btnOk = (Button) dialog.findViewById(R.id.dlg_pin_btn);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tmpPin = etPin.getText().toString();
                //проверяем правильность введенного пина
                if (!tmpPin.equals(session.getAccessToken()[4])){
                    etPin.setText("");
                    attempt--;
                    switch (attempt){
                        case 2:
                            Toast.makeText(getBaseContext(),"у вас осталось " + attempt + "попытки",Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            Toast.makeText(getBaseContext(),"у вас осталось " + attempt + "попытка",Toast.LENGTH_SHORT).show();
                            break;
                        case 0:
                            Toast.makeText(getBaseContext(),"у вас не осталось попыток. Данные предыдущей сессии удалены. Авторизуйтесь заново.",Toast.LENGTH_SHORT).show();
                            break;
                    }
                    if(attempt == 0) {
                        //если 3 попытки не удачны - удаляем сохраненные данные и отправляем на авторизацию
                        dialog.dismiss();
                        session.resetAccessToken();
                        loadWebView();
                    }
                } else {
                    //если все пин верен - идем на страницу с друзьями
                    vkUser.setUserId(session.getUserId());
                    vkUser.setToken(session.getToken());
                    Cursor c = dbManager.getAllData();
                    if(c.moveToFirst()){
                        do{
                            int id = c.getInt(c.getColumnIndex(DbManager.COLUMN_USERID));
                            String name = c.getString(c.getColumnIndex(DbManager.COLUMN_NAME));
                            String status = c.getString(c.getColumnIndex(DbManager.COLUMN_STATUS));
                            vkUser.getFriends().add(new Friend(id, name, "", status,null,new ArrayList<Foto>()));

                        }while(c.moveToNext());
                    }
                    finish();
                    startActivity(new Intent(getBaseContext(),MainActivity.class));
                }
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Toast.makeText(this,"У вас 3 попытки",Toast.LENGTH_SHORT).show();
    }

    //проверка строчного массива на пустые значания
    private boolean isNotEmpty(String [] arr){
        int length = arr.length;
        for(int i=0;i<length;i++){
            if(isNull(arr[i]))
                return false;
        }
        return true;
    }

    private boolean isNull(String s){
        return s == null || s.equals("");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dlg_pin_btn:
                dialog.dismiss();
                pin = etPin.getText().toString();
                vkUser.setUserId(user);
                vkUser.setToken(token);
                if(isNull(pin)) {
                    //если пин не установлен - стираем сохраненные данные
                    Toast.makeText(this, "Пин-код не установлен", Toast.LENGTH_SHORT).show();
                    session.resetAccessToken();
                    upDate();
                } else {
                    String  tmpId = session.getUserId();
                    session.saveAccessToken(token, expires, user, pin);
                    //обновление данных о друзьях авторизовавшегося пользователя
                    if(!user.equals(tmpId))
                        upDate();
                }

                break;
        }
    }

    private void upDate(){
        getFriendsAsync friendsAsync = new getFriendsAsync(this);
        friendsAsync.execute(String.format(Locale.ENGLISH, VK_API_FRIENDS,vkUser.getUserId(),vkUser.getToken(),getResources().getString(R.string.api_version)));
    }

    private void updateFriends(JSONArray object) {
        JSONArray jsonArray = object;
        int sz = jsonArray.length();
        if (sz!=vkUser.getFriends().size()){
            Toast.makeText(this,"Не удалось получить данные по некоторым друзьям",Toast.LENGTH_SHORT).show();
        }
        vkUser.getFriends().clear();
        String fname,lname,status,urlPhoto;
        for(int i=0; i<sz;i++){
            fname = lname = status = urlPhoto = "";
            int id = 0;
            try {
                id = jsonArray.getJSONObject(i).getInt("id");
                fname = jsonArray.getJSONObject(i).getString("first_name");
                lname = jsonArray.getJSONObject(i).getString("last_name");
                status = jsonArray.getJSONObject(i).getString("status");
                urlPhoto = jsonArray.getJSONObject(i).getString("photo_50");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Friend tmpFriend = new Friend(id,fname,lname,status,urlPhoto,new ArrayList<Foto>());
            vkUser.getFriends().add(tmpFriend);
        }

        downloadImageTask imageTask = new downloadImageTask(this);
        imageTask.execute();
    }

    private void addFriends(JSONObject object) throws JSONException {
        JSONArray jsonArray = object.getJSONArray("items");
        int sz = jsonArray.length();
        for(int i=0;i<sz;i++){
            int id = jsonArray.getInt(i);
            Friend tmpFriend = new Friend(id,null,null,null,null,null);
            vkUser.getFriends().add(tmpFriend);
        }

        StringBuilder sb = new StringBuilder();
        //ограничение vk-API
        if(sz >= 1000)
            sz = 999;
        for(int i=0; i< sz;i++){
            sb.append(vkUser.getFriends().get(i).getId() + ",");
        }
        sb.delete(sb.lastIndexOf(","),sb.length());
        String s = String.format(Locale.ENGLISH,VK_API_USERS,sb.toString(),vkUser.getToken(),getResources().getString(R.string.api_version));
        fillFriendsAsync fillFr = new fillFriendsAsync(this);
        fillFr.execute(s);
    }

    //загрузка миниатюр аватар и заполнение БД для списка друзей
    private class downloadImageTask extends AsyncTask<Void, Void, Void> {
        Context context;
        DbManager dbManager;
        ProgressDialog dbLoadDialog;

        public downloadImageTask(Context context) {
            this.context = context;
            dbManager = new DbManager(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dbManager.open();
            dbLoadDialog = new ProgressDialog(context);
            dbLoadDialog.setTitle("Идет загрузка фотографий");
            dbLoadDialog.setMessage("пожалуйста дождитесь окончания загрузки");
            dbLoadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dbLoadDialog.setMax(vkUser.getFriends().size());
            dbLoadDialog.show();
        }

        @Override
        protected Void doInBackground(Void... param) {
            ArrayList<Friend> arrayList = vkUser.getFriends();
            Bitmap mIcon;
            for(int i=0; i<arrayList.size();i++){
                mIcon = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.camera_c));
                try {
                    InputStream in = new java.net.URL(arrayList.get(i).getPicture()).openStream();
                    mIcon = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
                byte[] img = DbManager.getBytes(mIcon);
                dbManager.addRec(arrayList.get(i).getId(),arrayList.get(i).getFullName(),arrayList.get(i).getStatus(),img);
                dbLoadDialog.setProgress(i);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dbManager.close();
            dbLoadDialog.dismiss();
            finish();
            startActivity(new Intent(getBaseContext(),MainActivity.class));
        }
    }

    //запрос данных по пользователям по предварительно найденному списку ИД
    private class fillFriendsAsync extends AsyncTask<String,Void,JSONObject>{
        Context context;
        ProgressDialog pd;

        public fillFriendsAsync(Context context){
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context);
            pd.setMessage("Актуализация...");
            pd.setTitle("Пожалуйста подождите");
            pd.show();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            String url = params[0];
            JSONObject object = null;
            try {
                object = NetUtil.getJSON(url);
            } catch (IOException e) {
                Log.e("vk_log","fillFriends getJSON "+e.getMessage());
                e.printStackTrace();
            } catch (JSONException e) {
                Log.e("vk_log","fillFriends getJSON "+e.getMessage());
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
                        JSONArray arr = object.getJSONArray("response");
                        updateFriends(arr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                else
                    Toast.makeText(context,"В ответе нет требуемых данных",Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context,"Не удалось загрузить данные",Toast.LENGTH_SHORT).show();
        }
    }

    //запрос списка ИД-шников друзей пользователя
    private class getFriendsAsync extends AsyncTask<String, Void, JSONObject>{
        Context context;
        ProgressDialog pd;

        public getFriendsAsync(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context);
            pd.setMessage("Идет загрузка...");
            pd.setTitle("Пожалуйста подождите");
            pd.show();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            String url = params[0];
            JSONObject jObj = null;
            try {
                jObj = NetUtil.getJSON(url);
            } catch (IOException e) {
                Log.e("vk_log","getFriendsAsync getJSON "+e.getMessage());
                e.printStackTrace();
            } catch (JSONException e) {
                Log.e("vk_log","getFriendsAsync getJSON "+e.getMessage());
                e.printStackTrace();
            }
            return jObj;
        }

        @Override
        protected void onPostExecute(JSONObject object) {
            super.onPostExecute(object);
            pd.dismiss();
            if(object != null)
                if(object.has("response"))
                    try {
                        addFriends(object.getJSONObject("response"));
                    } catch (JSONException e) {
                        Log.e("vk_log","addFriends "+e.getMessage());
                        e.printStackTrace();
                    }
                else
                    Toast.makeText(context,"В ответе нет требуемых данных",Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context,"Не удалось загрузить данные",Toast.LENGTH_SHORT).show();
        }
    }

    //наследник класса WebViewClient, для того чтобы перехватить URL-адрес после авторизации
    private class authVKViewClient extends WebViewClient {

        //метод для открытия новых URL внутри веб-вью
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        //перехват токена после авторизации пользователя
        @Override
        public void onPageFinished(WebView view, String url) {
            if(url.startsWith("https://oauth.vk.com/blank.html")){
                if(dialog == null || !dialog.isShowing()){
                    String[] splitUrl = url.replace("https://oauth.vk.com/blank.html", "").split("&");
                    if (splitUrl.length == 3) {
                        token = splitUrl[0].substring(splitUrl[0].indexOf("=") + 1);
                        expires = splitUrl[1].substring(splitUrl[1].indexOf("=") + 1);
                        user = splitUrl[2].substring(splitUrl[2].indexOf("=") + 1);
                        //если авторизовался пользователя отличный от того который сохранен - удаляем БД
                        if (!session.getUserId().equals(user))
                            dbManager.delRec();
                        dialogPinSet();
                    }
                }
            } else
                super.onPageFinished(view, url);
        }
    }
}
