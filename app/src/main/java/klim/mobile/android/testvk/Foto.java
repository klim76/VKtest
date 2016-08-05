package klim.mobile.android.testvk;

import android.graphics.Bitmap;

/**
 * Created by e.klim on 05.08.2016.
 */
public class Foto {

    private int photo_id;
    private String url_mini;
    private String url_orig;
    private Bitmap miniature;

    public Foto(int id, String url_m, String url_o, Bitmap bitmap){
        photo_id = id;
        url_mini = url_m;
        url_orig = url_o;
        miniature = bitmap;
    }

    public int getPhoto_id() {
        return photo_id;
    }

    public void setPhoto_id(int photo_id) {
        this.photo_id = photo_id;
    }

    public String getUrl_mini() {
        return url_mini;
    }

    public void setUrl_mini(String url_mini) {
        this.url_mini = url_mini;
    }

    public String getUrl_orig() {
        return url_orig;
    }

    public void setUrl_orig(String url_orig) {
        this.url_orig = url_orig;
    }

    public Bitmap getMiniature() {
        return miniature;
    }

    public void setMiniature(Bitmap miniature) {
        this.miniature = miniature;
    }
}
