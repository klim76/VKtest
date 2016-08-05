package klim.mobile.android.testvk;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by e.klim on 02.08.2016.
 */
public class VKsession {
    private final String VK_TOKEN = "VkAccessToken";
    private final String VK_EXPIRES = "VkExpiresIn";
    private final String VK_USER = "VkUserId";
    private final String VK_PIN = "VkPin";
    private final String VK_TIME = "VkAccessTime";

    private SharedPreferences preferences;
    private final String PREFS_NAME = "Vk:Settings";
    private SharedPreferences.Editor editor;

    public VKsession(Context context){
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void saveAccessToken(String accessToken, String expires, String userId, String pin){
        editor.putString(VK_TOKEN, accessToken);
        editor.putString(VK_EXPIRES, expires);
        editor.putString(VK_USER, userId);
        editor.putString(VK_PIN, pin);
        editor.putLong(VK_TIME, System.currentTimeMillis());
        editor.commit();
    }

    public String[] getAccessToken(){
        String[] params = new String[5];
        params[0] = preferences.getString(VK_TOKEN, "");
        params[1] = preferences.getString(VK_EXPIRES, "");
        params[2] = preferences.getString(VK_USER, "");
        params[3] =  String.valueOf(preferences.getLong(VK_TIME,0));
        params[4] = preferences.getString(VK_PIN, "");
        return params;
    }

    public void resetAccessToken(){
        editor.putString(VK_TOKEN, "");
        editor.putString(VK_EXPIRES, "");
        editor.putString(VK_USER, "");
        editor.putString(VK_PIN, "");
        editor.putLong(VK_TIME, 0);
        editor.commit();
    }

    public String getToken(){
        return getAccessToken()[0];
    }

    public String getUserId(){
        return getAccessToken()[2];
    }

    public boolean isExpires(){
        return (System.currentTimeMillis() > (Long.parseLong(getAccessToken()[3]) + (Long.parseLong(getAccessToken()[1]) * 1000) ) );
    }
}
