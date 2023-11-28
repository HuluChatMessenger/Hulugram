package org.telegram.ui.Components.voip;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.voip.Instance;
import org.telegram.messenger.voip.VoIPService;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.BetterRatingView;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

public class VoIPHelper {

	public static long lastCallTime = 0;

	private static final int VOIP_SUPPORT_ID = 4244000;

	public static void startCall(TLRPC.User user, boolean videoCall, boolean canVideoCall, final Activity activity, TLRPC.UserFull userFull) {
		if (userFull != null && userFull.phone_calls_private) {
			new AlertDialog.Builder(activity)
					.setTitle(LocaleController.getString("VoipFailed", R.string.VoipFailed))
					.setMessage(AndroidUtilities.replaceTags(LocaleController.formatString("CallNotAvailable", R.string.CallNotAvailable,
							ContactsController.formatName(user.first_name, user.last_name))))
					.setPositiveButton(LocaleController.getString("OK", R.string.OK), null)
					.show();
			return;
		}
		if (ConnectionsManager.getInstance(UserConfig.selectedAccount).getConnectionState() != ConnectionsManager.ConnectionStateConnected) {
			boolean isAirplaneMode = Settings.System.getInt(activity.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
			AlertDialog.Builder bldr = new AlertDialog.Builder(activity)
					.setTitle(isAirplaneMode ? LocaleController.getString("VoipOfflineAirplaneTitle", R.string.VoipOfflineAirplaneTitle) : LocaleController.getString("VoipOfflineTitle", R.string.VoipOfflineTitle))
					.setMessage(isAirplaneMode ? LocaleController.getString("VoipOfflineAirplane", R.string.VoipOfflineAirplane) : LocaleController.getString("VoipOffline", R.string.VoipOffline))
					.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
			if (isAirplaneMode) {
				final Intent settingsIntent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
				if (settingsIntent.resolveActivity(activity.getPackageManager()) != null) {
					bldr.setNeutralButton(LocaleController.getString("VoipOfflineOpenSettings", R.string.VoipOfflineOpenSettings), (dialog, which) -> activity.startActivity(settingsIntent));
				}
			}
			try {
				bldr.show();
			} catch (Exception e) {
				FileLog.e(e);
			}
			return;
		}

		if (Build.VERSION.SDK_INT >= 23) {
			ArrayList<String> permissions = new ArrayList<>();
			if (activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
				permissions.add(Manifest.permission.RECORD_AUDIO);
			}
			if (videoCall && activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
				permissions.add(Manifest.permission.CAMERA);
			}
			if (permissions.isEmpty()) {
				initiateCall(user, videoCall, canVideoCall, activity);
			} else {
				activity.requestPermissions(permissions.toArray(new String[0]), videoCall ? 102 : 101);
			}
		} else {
			initiateCall(user, videoCall, canVideoCall, activity);
		}
	}

	private static void initiateCall(final TLRPC.User user, boolean videoCall, boolean canVideoCall, final Activity activity) {
		if (activity == null || user == null) {
			return;
		}
		if (VoIPService.getSharedInstance() != null) {
			TLRPC.User callUser = VoIPService.getSharedInstance().getUser();
			if (callUser.id != user.id) {
				new AlertDialog.Builder(activity)
						.setTitle(LocaleController.getString("VoipOngoingAlertTitle", R.string.VoipOngoingAlertTitle))
						.setMessage(AndroidUtilities.replaceTags(LocaleController.formatString("VoipOngoingAlert", R.string.VoipOngoingAlert,
								ContactsController.formatName(callUser.first_name, callUser.last_name),
								ContactsController.formatName(user.first_name, user.last_name))))
						.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialog, which) -> {
							if (VoIPService.getSharedInstance() != null) {
								VoIPService.getSharedInstance().hangUp(() -> doInitiateCall(user, videoCall, canVideoCall, activity));
							} else {
								doInitiateCall(user, videoCall, canVideoCall, activity);
							}
						})
						.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null)
						.show();
			} else {
				activity.startActivity(new Intent(activity, LaunchActivity.class).setAction("voip"));
			}
		} else if (VoIPService.callIShouldHavePutIntoIntent == null) {
			doInitiateCall(user, videoCall, canVideoCall, activity);
		}
	}

	private static void doInitiateCall(TLRPC.User user, boolean videoCall, boolean canVideoCall, Activity activity) {
		if (activity == null || user == null) {
			return;
		}
		if (System.currentTimeMillis() - lastCallTime < 2000) {
			return;
		}
		lastCallTime = System.currentTimeMillis();
		Intent intent = new Intent(activity, VoIPService.class);
		intent.putExtra("user_id", user.id);
		intent.putExtra("is_outgoing", true);
		intent.putExtra("start_incall_activity", true);
		intent.putExtra("video_call", Build.VERSION.SDK_INT >= 18 && videoCall);
		intent.putExtra("can_video_call", Build.VERSION.SDK_INT >= 18 && canVideoCall);
		intent.putExtra("account", UserConfig.selectedAccount);
		try {
			activity.startService(intent);
		} catch (Throwable e) {
			FileLog.e(e);
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	public static void permissionDenied(final Activity activity, final Runnable onFinish, int code) {
		if (!activity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) || code == 102 && !activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
			AlertDialog dlg = new AlertDialog.Builder(activity)
					.setTitle(LocaleController.getString("AppName", R.string.AppName))
					.setMessage(code == 102 ? LocaleController.getString("VoipNeedMicCameraPermission", R.string.VoipNeedMicCameraPermission) : LocaleController.getString("VoipNeedMicPermission", R.string.VoipNeedMicPermission))
					.setPositiveButton(LocaleController.getString("OK", R.string.OK), null)
					.setNegativeButton(LocaleController.getString("Settings", R.string.Settings), (dialog, which) -> {
						Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
						Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
						intent.setData(uri);
						activity.startActivity(intent);
					})
					.show();
			dlg.setOnDismissListener(dialog -> {
				if (onFinish != null)
					onFinish.run();
			});
		}
	}

	public static File getLogsDir() {
		File logsDir = new File(ApplicationLoader.applicationContext.getCacheDir(), "voip_logs");
		if (!logsDir.exists()) {
			logsDir.mkdirs();
		}
		return logsDir;
	}

	public static boolean canRateCall(TLRPC.TL_messageActionPhoneCall call) {
		if (!(call.reason instanceof TLRPC.TL_phoneCallDiscardReasonBusy) && !(call.reason instanceof TLRPC.TL_phoneCallDiscardReasonMissed)) {
			SharedPreferences prefs = MessagesController.getNotificationsSettings(UserConfig.selectedAccount); // always called from chat UI
			Set<String> hashes = prefs.getStringSet("calls_access_hashes", (Set<String>) Collections.EMPTY_SET);
			for (String hash : hashes) {
				String[] d = hash.split(" ");
				if (d.length < 2) {
					continue;
				}
				if (d[0].equals(call.call_id + "")) {
					return true;
				}
			}
		}
		return false;
	}

	public static void showRateAlert(Context context, TLRPC.TL_messageActionPhoneCall call) {
		SharedPreferences prefs = MessagesController.getNotificationsSettings(UserConfig.selectedAccount); // always called from chat UI
		Set<String> hashes = prefs.getStringSet("calls_access_hashes", (Set<String>) Collections.EMPTY_SET);
		for (String hash : hashes) {
			String[] d = hash.split(" ");
			if (d.length < 2)
				continue;
			if (d[0].equals(call.call_id + "")) {
				try {
					long accessHash = Long.parseLong(d[1]);
					showRateAlert(context, null, call.video, call.call_id, accessHash, UserConfig.selectedAccount, true);
				} catch (Exception ignore) {
				}
				return;
			}
		}
	}

	public static void showRateAlert(final Context context, final Runnable onDismiss, boolean isVideo, final long callID, final long accessHash, final int account, final boolean userInitiative) {
		final File log = getLogFile(callID);
		final int[] page = {0};
		LinearLayout alertView = new LinearLayout(context);
		alertView.setOrientation(LinearLayout.VERTICAL);

		int pad = AndroidUtilities.dp(16);
		alertView.setPadding(pad, pad, pad, 0);

		TextView text = new TextView(context);
		text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		text.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
		text.setGravity(Gravity.CENTER);
		text.setText(LocaleController.getString("VoipRateCallAlert", R.string.VoipRateCallAlert));
		alertView.addView(text);

		final BetterRatingView bar = new BetterRatingView(context);
		alertView.addView(bar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 0));

		final LinearLayout problemsWrap = new LinearLayout(context);
		problemsWrap.setOrientation(LinearLayout.VERTICAL);

		View.OnClickListener problemCheckboxClickListener = v -> {
			CheckBoxCell check = (CheckBoxCell) v;
			check.setChecked(!check.isChecked(), true);
		};

		final String[] problems = {isVideo ? "distorted_video" : null, isVideo ? "pixelated_video" : null, "echo", "noise", "interruptions", "distorted_speech", "silent_local", "silent_remote", "dropped"};
		for (int i = 0; i < problems.length; i++) {
			if (problems[i] == null) {
				continue;
			}
			CheckBoxCell check = new CheckBoxCell(context, 1);
			check.setClipToPadding(false);
			check.setTag(problems[i]);
			String label = null;
			switch (i) {
				case 0:
					label = LocaleController.getString("RateCallVideoDistorted", R.string.RateCallVideoDistorted);
					break;
				case 1:
					label = LocaleController.getString("RateCallVideoPixelated", R.string.RateCallVideoPixelated);
					break;
				case 2:
					label = LocaleController.getString("RateCallEcho", R.string.RateCallEcho);
					break;
				case 3:
					label = LocaleController.getString("RateCallNoise", R.string.RateCallNoise);
					break;
				case 4:
					label = LocaleController.getString("RateCallInterruptions", R.string.RateCallInterruptions);
					break;
				case 5:
					label = LocaleController.getString("RateCallDistorted", R.string.RateCallDistorted);
					break;
				case 6:
					label = LocaleController.getString("RateCallSilentLocal", R.string.RateCallSilentLocal);
					break;
				case 7:
					label = LocaleController.getString("RateCallSilentRemote", R.string.RateCallSilentRemote);
					break;
				case 8:
					label = LocaleController.getString("RateCallDropped", R.string.RateCallDropped);
					break;
			}
			check.setText(label, null, false, false);
			check.setOnClickListener(problemCheckboxClickListener);
			check.setTag(problems[i]);
			problemsWrap.addView(check);
		}
		alertView.addView(problemsWrap, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, -8, 0, -8, 0));
		problemsWrap.setVisibility(View.GONE);

		final EditTextBoldCursor commentBox = new EditTextBoldCursor(context);
		commentBox.setHint(LocaleController.getString("VoipFeedbackCommentHint", R.string.VoipFeedbackCommentHint));
		commentBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		commentBox.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
		commentBox.setHintTextColor(Theme.getColor(Theme.key_dialogTextHint));
		commentBox.setBackgroundDrawable(Theme.createEditTextDrawable(context, true));
		commentBox.setPadding(0, AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4));
		commentBox.setTextSize(18);
		commentBox.setVisibility(View.GONE);
		alertView.addView(commentBox, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 8, 8, 8, 0));

		final boolean[] includeLogs = {true};
		final CheckBoxCell checkbox = new CheckBoxCell(context, 1);
		View.OnClickListener checkClickListener = v -> {
			includeLogs[0] = !includeLogs[0];
			checkbox.setChecked(includeLogs[0], true);
		};
		checkbox.setText(LocaleController.getString("CallReportIncludeLogs", R.string.CallReportIncludeLogs), null, true, false);
		checkbox.setClipToPadding(false);
		checkbox.setOnClickListener(checkClickListener);
		alertView.addView(checkbox, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, -8, 0, -8, 0));

		final TextView logsText = new TextView(context);
		logsText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		logsText.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
		logsText.setText(LocaleController.getString("CallReportLogsExplain", R.string.CallReportLogsExplain));
		logsText.setPadding(AndroidUtilities.dp(8), 0, AndroidUtilities.dp(8), 0);
		logsText.setOnClickListener(checkClickListener);
		alertView.addView(logsText);

		checkbox.setVisibility(View.GONE);
		logsText.setVisibility(View.GONE);
		if (!log.exists()) {
			includeLogs[0] = false;
		}

		final AlertDialog alert = new AlertDialog.Builder(context)
				.setTitle(LocaleController.getString("CallMessageReportProblem", R.string.CallMessageReportProblem))
				.setView(alertView)
				.setPositiveButton(LocaleController.getString("Send", R.string.Send), (dialog, which) -> {
					//SendMessagesHelper.getInstance(currentAccount).sendMessage(commentBox.getText().toString(), VOIP_SUPPORT_ID, null, null, true, null, null, null);
				})
				.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null)
				.setOnDismissListener(dialog -> {
					if (onDismiss != null)
						onDismiss.run();
				})
				.create();
		if (BuildVars.LOGS_ENABLED && log.exists()) {
			alert.setNeutralButton("Send log", (dialog, which) -> {
				Intent intent = new Intent(context, LaunchActivity.class);
				intent.setAction(Intent.ACTION_SEND);
				intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(log));
				context.startActivity(intent);
			});
		}
		alert.show();
		alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		final View btn = alert.getButton(DialogInterface.BUTTON_POSITIVE);
		btn.setEnabled(false);
		bar.setOnRatingChangeListener(rating -> {
			btn.setEnabled(rating > 0);
			/*commentBox.setHint(rating<4 ? LocaleController.getString("CallReportHint", R.string.CallReportHint) : LocaleController.getString("VoipFeedbackCommentHint", R.string.VoipFeedbackCommentHint));
			commentBox.setVisibility(rating < 5 && rating > 0 ? View.VISIBLE : View.GONE);
			if (commentBox.getVisibility() == View.GONE) {
				((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(commentBox.getWindowToken(), 0);
			}
			*/
			((TextView) btn).setText((rating < 4 ? LocaleController.getString("Next", R.string.Next) : LocaleController.getString("Send", R.string.Send)).toUpperCase());
		});
		btn.setOnClickListener(v -> {
			int rating = bar.getRating();
			if (rating >= 4 || page[0] == 1) {
				final int currentAccount = UserConfig.selectedAccount;
				final TLRPC.TL_phone_setCallRating req = new TLRPC.TL_phone_setCallRating();
				req.rating = bar.getRating();
				ArrayList<String> problemTags = new ArrayList<>();
				for (int i = 0; i < problemsWrap.getChildCount(); i++) {
					CheckBoxCell check = (CheckBoxCell) problemsWrap.getChildAt(i);
					if (check.isChecked())
						problemTags.add("#" + check.getTag());
				}

				if (req.rating < 5) {
					req.comment = commentBox.getText().toString();
				} else {
					req.comment = "";
				}
				if (!problemTags.isEmpty() && !includeLogs[0]) {
					req.comment += " " + TextUtils.join(" ", problemTags);
				}
				req.peer = new TLRPC.TL_inputPhoneCall();
				req.peer.access_hash = accessHash;
				req.peer.id = callID;
				req.user_initiative = userInitiative;
				ConnectionsManager.getInstance(account).sendRequest(req, (response, error) -> {
					if (response instanceof TLRPC.TL_updates) {
						TLRPC.TL_updates updates = (TLRPC.TL_updates) response;
						MessagesController.getInstance(currentAccount).processUpdates(updates, false);
					}
					if (includeLogs[0] && log.exists() && req.rating < 4) {
						AccountInstance accountInstance = AccountInstance.getInstance(UserConfig.selectedAccount);
						SendMessagesHelper.prepareSendingDocument(accountInstance, log.getAbsolutePath(), log.getAbsolutePath(), null, TextUtils.join(" ", problemTags), "text/plain", VOIP_SUPPORT_ID, null, null, null, null, true, 0);
						Toast.makeText(context, LocaleController.getString("CallReportSent", R.string.CallReportSent), Toast.LENGTH_LONG).show();
					}
				});
				alert.dismiss();
			} else {
				page[0] = 1;
				bar.setVisibility(View.GONE);
				//text.setText(LocaleController.getString("CallReportHint", R.string.CallReportHint));
				text.setVisibility(View.GONE);
				alert.setTitle(LocaleController.getString("CallReportHint", R.string.CallReportHint));
				commentBox.setVisibility(View.VISIBLE);
				if (log.exists()) {
					checkbox.setVisibility(View.VISIBLE);
					logsText.setVisibility(View.VISIBLE);
				}
				problemsWrap.setVisibility(View.VISIBLE);
				((TextView) btn).setText(LocaleController.getString("Send", R.string.Send).toUpperCase());
			}
		});
	}

	private static File getLogFile(long callID) {
		if (BuildVars.DEBUG_VERSION) {
			File debugLogsDir = new File(ApplicationLoader.applicationContext.getExternalFilesDir(null), "logs");
			String[] logs = debugLogsDir.list();
			if (logs != null) {
				for (String log : logs) {
					if (log.endsWith("voip" + callID + ".txt")) {
						return new File(debugLogsDir, log);
					}
				}
			}
		}
		return new File(getLogsDir(), callID + ".log");
	}

	public static void showCallDebugSettings(final Context context) {
		final SharedPreferences preferences = MessagesController.getGlobalMainSettings();
		LinearLayout ll = new LinearLayout(context);
		ll.setOrientation(LinearLayout.VERTICAL);

		TextView warning = new TextView(context);
		warning.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
		warning.setText("Please only change these settings if you know exactly what they do.");
		warning.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
		ll.addView(warning, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 16, 8, 16, 8));

		final TextCheckCell tcpCell = new TextCheckCell(context);
		tcpCell.setTextAndCheck("Force TCP", preferences.getBoolean("dbg_force_tcp_in_calls", false), false);
		tcpCell.setOnClickListener(v -> {
			boolean force = preferences.getBoolean("dbg_force_tcp_in_calls", false);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putBoolean("dbg_force_tcp_in_calls", !force);
			editor.commit();
			tcpCell.setChecked(!force);
		});
		ll.addView(tcpCell);

		if (BuildVars.DEBUG_VERSION && BuildVars.LOGS_ENABLED) {
			final TextCheckCell dumpCell = new TextCheckCell(context);
			dumpCell.setTextAndCheck("Dump detailed stats", preferences.getBoolean("dbg_dump_call_stats", false), false);
			dumpCell.setOnClickListener(v -> {
				boolean force = preferences.getBoolean("dbg_dump_call_stats", false);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean("dbg_dump_call_stats", !force);
				editor.commit();
				dumpCell.setChecked(!force);
			});
			ll.addView(dumpCell);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			final TextCheckCell connectionServiceCell = new TextCheckCell(context);
			connectionServiceCell.setTextAndCheck("Enable ConnectionService", preferences.getBoolean("dbg_force_connection_service", false), false);
			connectionServiceCell.setOnClickListener(v -> {
				boolean force = preferences.getBoolean("dbg_force_connection_service", false);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean("dbg_force_connection_service", !force);
				editor.commit();
				connectionServiceCell.setChecked(!force);
			});
			ll.addView(connectionServiceCell);
		}

		new AlertDialog.Builder(context)
				.setTitle(LocaleController.getString("DebugMenuCallSettings", R.string.DebugMenuCallSettings))
				.setView(ll)
				.show();
	}

	public static int getDataSavingDefault() {
		boolean low = DownloadController.getInstance(0).lowPreset.lessCallData,
				medium = DownloadController.getInstance(0).mediumPreset.lessCallData,
				high = DownloadController.getInstance(0).highPreset.lessCallData;
		if (!low && !medium && !high) {
			return Instance.DATA_SAVING_NEVER;
		} else if (low && !medium && !high) {
			return Instance.DATA_SAVING_ROAMING;
		} else if (low && medium && !high) {
			return Instance.DATA_SAVING_MOBILE;
		} else if (low && medium && high) {
			return Instance.DATA_SAVING_ALWAYS;
		}
		if (BuildVars.LOGS_ENABLED)
			FileLog.w("Invalid call data saving preset configuration: " + low + "/" + medium + "/" + high);
		return Instance.DATA_SAVING_NEVER;
	}


	public static String getLogFilePath(String name) {
		final Calendar c = Calendar.getInstance();
		final File externalFilesDir = ApplicationLoader.applicationContext.getExternalFilesDir(null);
		return new File(externalFilesDir, String.format(Locale.US, "logs/%02d_%02d_%04d_%02d_%02d_%02d_%s.txt",
				c.get(Calendar.DATE), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR), c.get(Calendar.HOUR_OF_DAY),
				c.get(Calendar.MINUTE), c.get(Calendar.SECOND), name)).getAbsolutePath();
	}

	public static String getLogFilePath(long callId, boolean stats) {
		final File logsDir = getLogsDir();
		if (!BuildVars.DEBUG_VERSION) {
			final File[] _logs = logsDir.listFiles();
			if (_logs != null) {
				final ArrayList<File> logs = new ArrayList<>(Arrays.asList(_logs));
				while (logs.size() > 20) {
					File oldest = logs.get(0);
					for (File file : logs) {
						if (file.getName().endsWith(".log") && file.lastModified() < oldest.lastModified()) {
							oldest = file;
						}
					}
					oldest.delete();
					logs.remove(oldest);
				}
			}
		}
		return new File(logsDir, callId + ".log").getAbsolutePath();
	}
}
