package fi.bitrite.android.ws.model;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;

import com.google.android.gms.maps.model.LatLng;
import com.yelp.parcelgen.JsonParser.DualCreator;
import org.json.JSONException;
import org.json.JSONObject;

import fi.bitrite.android.ws.R;
import roboguice.util.Strings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Host extends _Host {

    private String mUpdated;

    public static Host createFromBriefInfo(HostBriefInfo briefInfo) {
        Host host = new Host();
        host.mId = briefInfo.getId();
        host.mName = briefInfo.getName();
        host.mFullname = briefInfo.getFullname();
        host.mComments = briefInfo.getAboutMe();
        host.mLongitude = briefInfo.getLongitude();
        host.mLatitude = briefInfo.getLatitude();
        return host;
    }

    public static final DualCreator<Host> CREATOR = new DualCreator<Host>() {

        public Host[] newArray(int size) {
            return new Host[size];
        }

        public Host createFromParcel(Parcel source) {
            Host object = new Host();
            object.readFromParcel(source);
            return object;
        }

        @Override
        public Host parse(JSONObject obj) throws JSONException {
            Host newInstance = new Host();
            newInstance.readFromJson(obj);
            return newInstance;
        }
    };

    public String getLocation() {
        StringBuilder sb = new StringBuilder();

        if (!Strings.isEmpty(getStreet())) {
            sb.append(getStreet()).append("\n");
        }

        if (!Strings.isEmpty(getAdditional())) {
            sb.append(getAdditional()).append("\n");
        }

        sb.append(getPostalCode()).append(", ").append(getCity()).append(", ").append(getProvince());

        if (!Strings.isEmpty(getCountry())) {
            sb.append(", ").append(getCountry().toUpperCase());
        }

        sb.append("\nLat: ").append(getLatitude()).append("\n");
        sb.append("Lon: ").append(getLongitude());

        return sb.toString();
    }

    public String getServices(Context context) {
        StringBuilder sb = new StringBuilder();
        Resources r = context.getResources();

        if (hasService(getShower()))
            sb.append(r.getString(R.string.host_service_shower) + "\n");
        if (hasService(getFood()))
            sb.append(r.getString(R.string.host_services_food) + "\n");
        if (hasService(getBed()))
            sb.append(r.getString(R.string.host_services_bed) + "\n");
        if (hasService(getLaundry()))
            sb.append(r.getString(R.string.host_service_laundry) + "\n");
        if (hasService(getStorage()))
            sb.append(r.getString(R.string.host_service_storage) + "\n");
        if (hasService(getKitchenUse()))
            sb.append(r.getString(R.string.host_service_kitchen) + "\n");
        if (hasService(getLawnspace()))
            sb.append(r.getString(R.string.host_service_tentspace) + "\n");
        if (hasService(getSag()))
            sb.append(context.getString(R.string.host_service_sag) + "\n");

        return sb.toString();
    }

    private boolean hasService(String service) {
        return !Strings.isEmpty(service) && service.equals("1");
    }

    public boolean isNotCurrentlyAvailable() {
        return getNotCurrentlyAvailable().equals("1");
    }

    public String getUpdated() {
        return mUpdated;
    }

    public void setUpdated(String updated) {
        mUpdated = updated;
    }

    public String getMemberSince() {
        return formatDate(getCreated());
    }

    public String getLastLogin() {
        return formatDate(getLogin());
    }

    private String formatDate(String timestamp) {
        if (Strings.isEmpty(timestamp)) {
            return "";
        }

        Date date = new Date(Long.valueOf(timestamp) * 1000);
        DateFormat dateFormat = SimpleDateFormat.getDateInstance();
        return dateFormat.format(date);
    }

    public Object getLatLng() {
        return new LatLng(Double.parseDouble(mLatitude), Double.parseDouble(mLongitude));
    }
}
