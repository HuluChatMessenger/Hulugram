package plus.utils;

import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.NotificationCenter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import plus.helpers.LogManager;

public class TelegramMessageCodeNotificationListenerService  {

    public static boolean hasTried;
//    @Override
//    public void onNotificationPosted(StatusBarNotification sbn) {
//        super.onNotificationPosted(sbn);
//        ApplicationLoader.postInitApplication();
////        if(hasTried){
////            return;
////        }
////        try {
////            String notificationText = sbn.getNotification().extras.getCharSequence("android.text").toString();
////            Pattern pattern = Pattern.compile("Login code: (\\d+)");
////            Matcher matcher = pattern.matcher(notificationText);
////            AndroidUtilities.runOnUIThread(new Runnable() {
////                @Override
////                public void run() {//plus telegram messenger
////                    try {
////                        if (matcher.find()) {
////                            String loginCode = matcher.group(1).strip();
////                            hasTried = true;
////                            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didReceiveTelegramCode,loginCode);
////                        }
////                    }catch (Exception ex){
////                        hasTried = true;
////                    }
////
////                }
////            });
////
////        }catch (Exception exception){
////        }
//    }
//
//    @Override
//    public void onNotificationRemoved(StatusBarNotification sbn) {
//        super.onNotificationRemoved(sbn);
//
//        // This method is called when a notification is removed.
//    }
}
