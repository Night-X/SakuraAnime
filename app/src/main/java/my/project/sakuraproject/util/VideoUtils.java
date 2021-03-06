package my.project.sakuraproject.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AlertDialog;
import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeDescBean;
import my.project.sakuraproject.main.player.PlayerActivity;
import my.project.sakuraproject.main.webview.WebActivity;

public class VideoUtils {
    private static AlertDialog alertDialog;
    private final static Pattern PLAY_URL_PATTERN = Pattern.compile("(https?|ftp|file):\\/\\/[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]");

    /**
     * 解析失败提示弹窗
     *
     * @param context
     * @param HTML_url
     */
    public static void showErrorInfo(Context context, String HTML_url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setPositiveButton(Utils.getString(R.string.play_not_found_positive), null);
        builder.setNegativeButton(Utils.getString(R.string.play_not_found_negative), null);
        builder.setTitle(Utils.getString(R.string.play_not_found_title));
        builder.setMessage(Utils.getString(R.string.error_800));
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            alertDialog.dismiss();
//            context.startActivity(new Intent(context, DefaultWebActivity.class).putExtra("url", HTML_url));
            Utils.viewInChrome(context, HTML_url);
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> alertDialog.dismiss());
    }

    /**
     * 发现多个播放地址时弹窗
     *
     * @param context
     * @param list
     * @param listener
     */
    public static void showMultipleVideoSources(Context context,
                                                List<String> list,
                                                DialogInterface.OnClickListener listener) {
        String[] items = new String[list.size()];
        for (int i = 0, size = list.size(); i < size; i++) {
            items[i] = getVideoUrl(list.get(i));
        }
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle(Utils.getString(R.string.select_video_source));
        builder.setCancelable(false);
        builder.setItems(items, listener);
        builder.setNegativeButton(Utils.getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        alertDialog = builder.create();
        alertDialog.show();
    }

    public static String getVideoUrl(String url) {
        String playStr = "";
        url = url.substring(12, url.length());
        url = url.substring(0, url.length() - 3);
        //如果网址
        if (Patterns.WEB_URL.matcher(url.replace(" ", "")).matches()) {
            Matcher m = PLAY_URL_PATTERN.matcher(url);
            while (m.find()) {
                playStr = m.group();
                break;
            }
        } else playStr = url;
        return playStr;
    }

    /**
     * 打开播放器
     *
     * @param isDescActivity
     * @param activity
     * @param witchTitle
     * @param url
     * @param animeTitle
     * @param diliUrl
     * @param list
     */
    public static void openPlayer(boolean isDescActivity, Activity activity, String witchTitle, String url, String animeTitle, String diliUrl, List<AnimeDescBean> list) {
        Bundle bundle = new Bundle();
        bundle.putString("title", witchTitle);
        bundle.putString("url", url);
        bundle.putString("animeTitle", animeTitle);
        bundle.putString("dili", diliUrl);
        bundle.putSerializable("list", (Serializable) list);
        Sakura.destoryActivity("player");
        if (isDescActivity)
            activity.startActivityForResult(new Intent(activity, PlayerActivity.class).putExtras(bundle), 0x10);
        else {
            activity.startActivity(new Intent(activity, PlayerActivity.class).putExtras(bundle));
            activity.finish();
        }
    }

    /**
     * 打开webview
     *
     * @param isDescActivity
     * @param activity
     * @param witchTitle
     * @param animeTitle
     * @param url
     * @param diliUrl
     * @param list
     */
    public static void openWebview(boolean isDescActivity, Activity activity, String witchTitle, String animeTitle, String url, String diliUrl, List<AnimeDescBean> list) {
        Bundle bundle = new Bundle();
        bundle.putString("witchTitle", witchTitle);
        bundle.putString("title", animeTitle);
        bundle.putString("url", url);
        bundle.putString("dili", diliUrl);
        bundle.putSerializable("list", (Serializable) list);
        if (isDescActivity)
            activity.startActivityForResult(new Intent(activity, WebActivity.class).putExtras(bundle), 0x10);
        else {
            activity.startActivity(new Intent(activity, WebActivity.class).putExtras(bundle));
            activity.finish();
        }
    }


    public static String getUrl(String url) {
        return url.contains(Sakura.DOMAIN) ? url : Sakura.DOMAIN + url;
    }
}
