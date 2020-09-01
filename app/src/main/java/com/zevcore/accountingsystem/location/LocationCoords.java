package com.zevcore.accountingsystem.location;

import org.json.JSONException;
import org.json.JSONObject;

public class LocationCoords {
    private double longitude;
    private double latitutde;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitutde() {
        return latitutde;
    }

    public void setLatitutde(double latitutde) {
        this.latitutde = latitutde;
    }

    public String toJSON() throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("latitude", this.latitutde);
        jo.put("longitude", this.longitude);
        return jo.toString();
    }
}
