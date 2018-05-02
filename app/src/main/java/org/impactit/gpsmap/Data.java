package org.impactit.gpsmap;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bikram Koju on 5/2/18.
 * ImpactIT
 * info@impactit.org
 */

@IgnoreExtraProperties
public class Data {
    private double lat, lng;

    public Data(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public Data() {
    }

    public double getLat() {
        return lat;
    }


    public double getLng() {
        return lng;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("lat", lat);
        result.put("lng", lng);


        return result;
    }


}
