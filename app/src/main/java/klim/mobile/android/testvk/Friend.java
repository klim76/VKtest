package klim.mobile.android.testvk;

import java.util.ArrayList;

/**
 * Created by klim on 02.08.2016.
 */
public class Friend {

    private int id;
    private String name;
    private String surname;
    private String status;
    private String picture;
    private ArrayList<Foto> photos = new ArrayList<>();

    public Friend(int id,String name,String surname, String status, String picture, ArrayList<Foto> photos){
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.status = status;
        this.picture = picture;
        this.photos = photos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<Foto> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<Foto> photos) {
        this.photos = photos;
    }

    public String getFullName(){
        StringBuilder sb = new StringBuilder();
        if(!name.equals(""))
            sb.append(name);
        else
            sb.append("noName");

        if(!surname.equals(""))
            sb.append(" ").append(surname);
        else
            sb.append(" noSurName");
        return sb.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
