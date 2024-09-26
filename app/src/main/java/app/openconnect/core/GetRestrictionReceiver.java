package app.openconnect.core;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.RestrictionEntry;
import android.os.Bundle;
import java.util.ArrayList;
import app.openconnect.R;

public class GetRestrictionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        final PendingResult result = goAsync();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bundle extras = new Bundle();

                ArrayList<RestrictionEntry> restrictionEntries = initRestrictions(context);


                extras.putParcelableArrayList(Intent.EXTRA_RESTRICTIONS_LIST, restrictionEntries);
                result.setResult(Activity.RESULT_OK, null, extras);
                result.finish();
            }
        }).start();
    }

    private ArrayList<RestrictionEntry> initRestrictions(Context context) {
        ArrayList<RestrictionEntry> restrictions = new ArrayList<>();


        RestrictionEntry allowChanges = new RestrictionEntry("allow_changes", false);
        String title = context.getString(R.string.allow_vpn_changes);


        if (title != null) {
            allowChanges.setTitle(title);
        }

        restrictions.add(allowChanges);
        return restrictions;
    }
}