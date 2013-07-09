package com.mridang.messstats;

import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

/*
 * This class is the main class that provides the widget
 */
public class MessstatsWidget extends DashClockExtension {

	/*
	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onInitialize(boolean)
	 */
	@Override
	protected void onInitialize(boolean isReconnect) {

		super.onInitialize(isReconnect);
		if (!isReconnect) {
			addWatchContentUris(new String[]{"content://sms/"});
			addWatchContentUris(new String[]{"content://mms/"});
		}

	}

	/*
	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
	 */
	public void onCreate() {

		super.onCreate();
		Log.d("MessstatsWidget", "Created");
		BugSenseHandler.initAndStartSession(this, "bc2d4ec1");

	}

	/*
	 * @see
	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
	 * (int)
	 */
	@Override
	protected void onUpdateData(int arg0) {

		Log.d("MessstatsWidget", "Calculating message statistics");
		ExtensionData edtInformation = new ExtensionData();
		edtInformation.visible(false);

		try {

			Log.d("MessstatsWidget", "Calculating SMS statistics");
			Cursor curSmses = getContentResolver().query(Uri.parse("content://sms/"), null, null, null, null);

			Integer intSentSms = 0;
			Integer intRecdSms = 0;

			while (curSmses != null && curSmses.moveToNext()) {

				switch (curSmses.getInt(curSmses.getColumnIndex("type"))) {

				case 1:
					intRecdSms = intRecdSms + 1;
					break;

				case 2:
					intSentSms = intSentSms + 1;
					break;

				}

			}

			Log.d("MessstatsWidget", "Send SMSes: " + intSentSms);
			Log.d("MessstatsWidget", "Received SMSs: " + intRecdSms);
			Log.d("MessstatsWidget", "Total SMSes: " + intSentSms + intRecdSms);

			Log.d("MessstatsWidget", "Calculating MMS statistics");
			Cursor curMmses = getContentResolver().query(Uri.parse("content://mms/"), null, null, null, null);

			Integer intSentMms = 0;
			Integer intRecdMms = 0;

			while (curMmses != null && curMmses.moveToNext()) {

				switch (curMmses.getInt(curMmses.getColumnIndex("msg_box"))) {

				case 2:
					intRecdMms = intRecdMms + 1;
					break;

				case 1:
					intSentMms = intSentMms + 1;
					break;

				}

			}

			Log.d("MessstatsWidget", "Send MMSes: " + intSentMms);
			Log.d("MessstatsWidget", "Received MMSes: " + intRecdMms);
			Log.d("MessstatsWidget", "Total MMSes: " + intSentMms + intRecdMms);

			edtInformation
			.expandedBody((edtInformation.expandedBody() == null ? ""
					: edtInformation.expandedBody() + "\n")
					+ getResources().getQuantityString(R.plurals.sent,
							intSentSms + intSentMms,
							intSentSms + intSentMms));

			edtInformation.status(String.format(getString(R.string.messages),
					intSentSms + intRecdSms + intSentMms + intRecdMms));

			edtInformation
			.expandedBody((edtInformation.expandedBody() == null ? ""
					: edtInformation.expandedBody() + "\n")
					+ getResources().getQuantityString(R.plurals.received,
							intRecdSms + intRecdMms,
							intRecdSms + intRecdMms));

			edtInformation.visible(true);

		} catch (Exception e) {
			Log.e("MessstatsWidget", "Encountered an error", e);
			BugSenseHandler.sendException(e);
		}	

		edtInformation.icon(R.drawable.ic_dashclock);
		publishUpdate(edtInformation);
		Log.d("MessstatsWidget", "Done");

	}

	/*
	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
	 */
	public void onDestroy() {

		super.onDestroy();
		Log.d("MessstatsWidget", "Destroyed");
		BugSenseHandler.closeSession(this);

	}

}