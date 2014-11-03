package us.axelia.axelia.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mac on 1/11/14.
 */
public class Location {
    @SerializedName("Id")
    private int id;
    @SerializedName("AlertMessage")
    private String alertMessage;
    @SerializedName("Name")
    private String name;

    public Location() {
    }

    public Location(int id, String alertMessage, String name) {
        this.id = id;
        this.alertMessage = alertMessage;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
