package klim.mobile.android.testvk;

import java.util.ArrayList;

/**
 * Created by klim on 02.08.2016.
 */
public class VkUser {
    private String userId;
    private String token;
    private ArrayList<Friend> friends;

    private static volatile VkUser instance;
    public static VkUser getInstance() {
        VkUser localInstance = instance;
        if (localInstance == null) {
            synchronized (VkUser.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new VkUser();
                }
            }
        }
        return localInstance;
    }

    private VkUser(){
        friends = new ArrayList<>();
    }


    public ArrayList<Friend> getFriends() {
        return friends;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
