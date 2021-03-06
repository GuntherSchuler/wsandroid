package fi.bitrite.android.ws.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import fi.bitrite.android.ws.R;
import fi.bitrite.android.ws.WSAndroidApplication;
import fi.bitrite.android.ws.api.RestClient;
import fi.bitrite.android.ws.model.Host;
import fi.bitrite.android.ws.util.GlobalInfo;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * Responsible for letting the user type in a message and then sending it to a host
 * over the WarmShowers web service.
 */
public class FeedbackActivity extends RoboActivity {

    @InjectView(R.id.txtActivityTitle)
    TextView activityTitleView;
    @InjectView(R.id.feedbackEditText)
    EditText feedbackEditText;
    @InjectView(R.id.date_picker)
    DatePicker datePicker;
    @InjectView(R.id.lblOverallExperience)
    TextView lblOverallExperience;
    @InjectView(R.id.feedback_overall_experience)
    Spinner feedbackOverallExperience;
    @InjectView(R.id.feedback_how_we_met)
    Spinner howWeMet;

    // These are unfortunately artificially positional-mapped to the spinners since Android doesn't
    // have separate *value* from *title*
    // in the Spinner, and we want translatable spinners. So these must exactly match the names
    // of the field values on the Feedback form.
    static private final String[] HOST_GUEST_MAPPING = new String[]{"Host", "Guest", "Met Traveling", "Other"};
    static private final String[] OVERALL_EXPERIENCE_MAPPING = new String[]{"Positive", "Neutral", "Negative"};

    // This value must match the "minimum number of words" in the node submission settings at
    // https://www.warmshowers.org/admin/content/node-type/trust-referral
    static private final int MIN_FEEDBACK_WORD_LENGTH = 10;

    private Host host;
    private DialogHandler dialogHandler;
    private static final String WARMSHOWERS_FEEDBACK_POST_URL = GlobalInfo.warmshowersBaseUrl + "/services/rest/node";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        dialogHandler = new DialogHandler(this);

        if (savedInstanceState != null) {
            host = savedInstanceState.getParcelable("host");
        } else {
            Intent i = getIntent();
            host = (Host) i.getParcelableExtra("host");
        }

        activityTitleView.setText(getString(R.string.leaving_feedback, host.getFullname()));

        // Set up datepicker
        GregorianCalendar now = new GregorianCalendar();
        datePicker.findViewById(Resources.getSystem().getIdentifier("day", "id", "android")).setVisibility(View.GONE);
        datePicker.init(now.get( Calendar.YEAR), now.get( Calendar.MONTH), now.get( Calendar.DAY_OF_MONTH), null);

        lblOverallExperience.setText(getString(R.string.lbl_feedback_overall_experience, host.getFullname()));
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("host", host);
        super.onSaveInstanceState(outState);
    }

    /**
     * Function called when button gets clicked
     *
     * @param view
     */
    public void sendFeedback(View view) {

        // Site requires 10 words in the feedback, so pre-enforce that.
        if (feedbackEditText.getText().toString().split("\\w+").length < MIN_FEEDBACK_WORD_LENGTH) {
            dialogHandler.alert(getResources().getString(R.string.feedback_validation_error));
            return;
        }
        // Ensure a selection in the "how we met"
        if (howWeMet.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
            dialogHandler.alert(getString(R.string.feedback_how_we_met_error));
            return;
        }
        // Ensure a selection in "overall experience"
        if (feedbackOverallExperience.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
            dialogHandler.alert(getString(R.string.feedback_overall_experience_error));
            return;
        }

        dialogHandler.showDialog(DialogHandler.HOST_CONTACT);

        SendFeedbackTask task = new SendFeedbackTask();
        task.execute();
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        return dialogHandler.createDialog(id, getResources().getString(R.string.sending_feedback));
    }

    private class SendFeedbackTask extends AsyncTask<Void, Void, Object> {

        @Override
        protected Object doInBackground(Void[] params) {
            Object retObj = null;
            try {

                // See https://github.com/warmshowers/Warmshowers.org/wiki/Warmshowers-RESTful-Services-for-Mobile-Apps#create_feedback
                List<NameValuePair> args = new ArrayList<NameValuePair>();
                args.add(new BasicNameValuePair("node[type]", "trust_referral"));
                args.add(new BasicNameValuePair("node[field_member_i_trust][0][uid][uid]", host.getName()));
                args.add(new BasicNameValuePair("node[body]", feedbackEditText.getText().toString()));
                args.add(new BasicNameValuePair("node[field_guest_or_host][value]", HOST_GUEST_MAPPING[howWeMet.getSelectedItemPosition()]));
                args.add(new BasicNameValuePair("node[field_rating][value]", OVERALL_EXPERIENCE_MAPPING[feedbackOverallExperience.getSelectedItemPosition()]));
                args.add(new BasicNameValuePair("node[field_hosting_date][0][value][year]", Integer.toString(datePicker.getYear())));
                args.add(new BasicNameValuePair("node[field_hosting_date][0][value][month]", Integer.toString(datePicker.getMonth() + 1)));

                RestClient restClient = new RestClient();
                JSONObject result = restClient.post(WARMSHOWERS_FEEDBACK_POST_URL, args);

            } catch (Exception e) {
                Log.e(WSAndroidApplication.TAG, e.getMessage(), e);
                retObj = e;
            }

            return retObj;
        }

        @Override
        protected void onPostExecute(Object result) {
            dialogHandler.dismiss();
            if (result instanceof Exception) {
                RestClient.reportError(FeedbackActivity.this, result);
                return;
            }
            showSuccessDialog();
        }
    }

    protected void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FeedbackActivity.this);
        builder.setMessage(getResources().getString(R.string.feedback_sent, host.getFullname())).setPositiveButton(
                getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onStop() {
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

}
