package com.mridang.messstats;

import java.util.Calendar;

import org.acra.ACRA;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.android.apps.dashclock.api.ExtensionData;

/*
 * This class is the main class that provides the widget
 */
public class MessstatsWidget extends ImprovedExtension {

	/*
	 * (non-Javadoc)
	 * @see com.mridang.battery.ImprovedExtension#getIntents()
	 */
	@Override
	protected IntentFilter getIntents() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.battery.ImprovedExtension#getTag()
	 */
	@Override
	protected String getTag() {
		return getClass().getSimpleName();
	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.battery.ImprovedExtension#getUris()
	 */
	@Override
	protected String[] getUris() {
		return new String[] {"content://sms/", "content://mms/"};
	}

	/*
	 * @see
	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
	 * (int)
	 */
	@Override
	protected void onUpdateData(int intReason) {

		Log.d(getTag(), "Calculating message statistics");
		ExtensionData edtInformation = new ExtensionData();
		setUpdateWhenScreenOn(false);

		try {

			Log.d(getTag(), "Checking period that user has selected");
			Calendar calCalendar = Calendar.getInstance();
			calCalendar.set(Calendar.MINUTE, 0);
			calCalendar.set(Calendar.HOUR, 0);
			calCalendar.set(Calendar.SECOND, 0);
			calCalendar.set(Calendar.MILLISECOND, 0);
			calCalendar.set(Calendar.HOUR_OF_DAY, 0);

			switch (Integer.parseInt(getString("period", "4"))) {

			case 0: // Day
				Log.d(getTag(), "Fetch messages for the day");
				calCalendar.set(Calendar.HOUR_OF_DAY, 0);
				break;

			case 1: // Week
				Log.d(getTag(), "Fetch messages for the week");
				calCalendar.set(Calendar.DAY_OF_WEEK, calCalendar.getFirstDayOfWeek());
				break;

			case 2: // Month
				Log.d(getTag(), "Fetch messages for the month");
				calCalendar.set(Calendar.DAY_OF_MONTH, 1);
				break;

			case 3: // Year
				Log.d(getTag(), "Fetch messages for the year");
				calCalendar.set(Calendar.DAY_OF_YEAR, 1);
				break;

			default:
				Log.d(getTag(), "Fetch all messages");
				calCalendar.clear();
				break;

			}

			Log.d(getTag(), "Querying the database to get the messages since " + calCalendar.getTime());
			String strClause = "date >= ?";
			String[] strValues = { String.valueOf(calCalendar.getTimeInMillis()) };

			Log.d(getTag(), "Calculating SMS statistics");
			Cursor curSmses = getContentResolver().query(Uri.parse("content://sms/"), null, strClause, strValues, null);

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

			Log.d(getTag(), "Send SMSes: " + intSentSms);
			Log.d(getTag(), "Received SMSs: " + intRecdSms);
			Log.d(getTag(), "Total SMSes: " + (intSentSms + intRecdSms));

			Log.d(getTag(), "Calculating MMS statistics");
			Cursor curMmses = getContentResolver().query(Uri.parse("content://mms/"), null, strClause, strValues, null);

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

			Log.d(getTag(), "Send MMSes: " + intSentMms);
			Log.d(getTag(), "Received MMSes: " + intRecdMms);
			Log.d(getTag(), "Total MMSes: " + (intSentMms + intRecdMms));

			Integer intSent = intSentSms + intSentMms;
			Integer intRecd = intRecdSms + intReason;
			Integer intTotal = intSentSms + intRecdSms + intSentMms + intRecdMms;

			edtInformation.expandedTitle(getQuantityString(R.plurals.sent, intSent, intSent));
			edtInformation.expandedTitle(edtInformation.expandedTitle() + getQuantityString(R.plurals.received, intRecd, intRecd));
			edtInformation.expandedBody(getString(R.string.messages, intTotal));
			edtInformation.status(intTotal.toString());
			edtInformation.visible(true);

		} catch (Exception e) {
			edtInformation.visible(false);
			Log.e(getTag(), "Encountered an error", e);
			ACRA.getErrorReporter().handleSilentException(e);
		}

		edtInformation.icon(R.drawable.ic_dashclock);
		doUpdate(edtInformation);

	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.alarmer.ImprovedExtension#onReceiveIntent(android.content.Context, android.content.Intent)
	 */
	@Override
	protected void onReceiveIntent(Context ctxContext, Intent ittIntent) {
		onUpdateData(UPDATE_REASON_MANUAL);
	}

}