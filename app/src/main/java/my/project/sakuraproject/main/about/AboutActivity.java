package my.project.sakuraproject.main.about;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.r0adkll.slidr.Slidr;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.LogAdapter;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.LogBean;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.net.DownloadUtil;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.SwipeBackLayoutUtil;
import my.project.sakuraproject.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AboutActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.cache)
    TextView cache;
    @BindView(R.id.open_source)
    TextView open_source;
    @BindView(R.id.version)
    TextView version;
    private ProgressDialog p;
    private String downloadUrl;
    private Call downCall;
    @BindView(R.id.footer)
    LinearLayout footer;

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_about;
    }

    @Override
    protected void init() {
        Slidr.attach(this, Utils.defaultInit());
        initToolbar();
        initViews();
    }

    @Override
    protected void initBeforeView() {
        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
    }

    public void initToolbar() {
        toolbar.setTitle(Utils.getString(R.string.about));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    private void initViews() {
        LinearLayout.LayoutParams Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Utils.getNavigationBarHeight(this));
        footer.findViewById(R.id.footer).setLayoutParams(Params);
        version.setText(Utils.getASVersionName());
        cache.setText(Environment.getExternalStorageDirectory() + Utils.getString(R.string.cache_text));
        open_source.setOnClickListener(v -> {
            if (Utils.isFastClick())
                startActivity(new Intent(AboutActivity.this, OpenSourceActivity.class));
        });
    }

    @OnClick({R.id.sakura,R.id.github})
    public void openBrowser(CardView cardView) {
        switch (cardView.getId()) {
            case R.id.sakura:
                Utils.viewInChrome(this, Sakura.DOMAIN);
                break;
            case R.id.github:
                Utils.viewInChrome(this, Utils.getString(R.string.github_url));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about_menu, menu);
        MenuItem checkUpdateItem = menu.findItem(R.id.check_update);
        MenuItem updateLogItem = menu.findItem(R.id.update_log);
        if (!(Boolean) SharedPreferencesUtils.getParam(this, "darkTheme", false)) {
            checkUpdateItem.setIcon(R.drawable.baseline_update_black_48dp);
            updateLogItem.setIcon(R.drawable.baseline_log_black_48dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update_log:
                showUpdateLogs();
                break;
            case R.id.check_update:
                if (Utils.isFastClick()) checkUpdate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showUpdateLogs() {
        AlertDialog alertDialog;
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_update_log, null);
        RecyclerView logs = view.findViewById(R.id.rv_list);
        logs.setLayoutManager(new LinearLayoutManager(this));
        LogAdapter logAdapter = new LogAdapter(createUpdateLogList());
        logs.setAdapter(logAdapter);
        builder.setPositiveButton(Utils.getString(R.string.page_positive), null);
        TextView title = new TextView(this);
        title.setText(Utils.getString(R.string.update_log));
        title.setPadding(30,30,30,30);
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        title.setGravity(Gravity.LEFT);
        title.setTextSize(18);
        title.setTextColor(getResources().getColor(R.color.text_color_primary));
        builder.setCustomTitle(title);
        alertDialog = builder.setView(view).create();
        alertDialog.show();
    }

    public List createUpdateLogList() {
        List logsList = new ArrayList();
        logsList.add(new LogBean("版本：1.8.1","修复某些设备导航栏的显示问题"));
        logsList.add(new LogBean("版本：1.8","修复一些Bugs\n修正部分界面布局\n适配沉浸式导航栏《仅支持原生导航栏，第三方魔改UI无效》（Test）"));
        logsList.add(new LogBean("版本：1.7","修复一些Bugs\n修正部分界面布局\n新增亮色主题（Test）"));
        logsList.add(new LogBean("版本：1.6","修复更新SDK后导致崩溃的严重问题"));
        logsList.add(new LogBean("版本：1.5","升级SDK版本为29（Android 10）"));
        logsList.add(new LogBean("版本：1.4","修复搜索界面无法加载更多动漫的Bug"));
        logsList.add(new LogBean("版本：1.3","部分UI变更，优化体验\n修复存在的一些问题"));
        logsList.add(new LogBean("版本：1.2","修复番剧详情剧集列表的一个显示错误\n新增视频播放解析方法，通过目前使用情况，理论上能正常播放樱花动漫网站大部分视频啦（有一些无法播放的视频绝大多数是网站自身原因）"));
        logsList.add(new LogBean("版本：1.1","修复一个Bug\n修复一个显示错误\n修复部分番剧无法正常播放的问题（兼容性待测试）"));
        logsList.add(new LogBean("版本：1.0","第一个版本（业余时间花了两个下午时间编写，可能存在许多Bug~）"));
        return logsList;
    }

    public void checkUpdate() {
        p = Utils.getProDialog(this, R.string.check_update_text);
        new Handler().postDelayed(() -> new HttpGet(Api.CHECK_UPDATE, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> application.showErrorToastMsg( Utils.getString(R.string.ck_network_error)));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                try {
                    JSONObject obj = new JSONObject(json);
                    String newVersion = obj.getString("tag_name");
                    if (newVersion.equals(Utils.getASVersionName()))
                        runOnUiThread(() -> {
                            Utils.cancelProDialog(p);
                            application.showSuccessToastMsg(Utils.getString(R.string.no_new_version));
                        });
                    else {
                        downloadUrl = obj.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
                        String body = obj.getString("body");
                        runOnUiThread(() -> {
                            Utils.cancelProDialog(p);
                            Utils.findNewVersion(AboutActivity.this,
                                    newVersion,
                                    body,
                                    (dialog, which) -> {
                                        p = Utils.showProgressDialog(AboutActivity.this);
                                        p.setButton(ProgressDialog.BUTTON_NEGATIVE, Utils.getString(R.string.cancel), (dialog1, which1) -> {
                                            if (null != downCall)
                                                downCall.cancel();
                                            dialog1.dismiss();
                                        });
                                        p.show();
                                        downNewVersion(downloadUrl);
                                    },
                                    (dialog, which) -> {
                                        dialog.dismiss();
                                    });
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }), 1000);
    }

    /**
     * 下载apk
     *
     * @param url 下载地址
     */
    private void downNewVersion(String url) {
        downCall = DownloadUtil.get().downloadApk(url, new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(final String fileName) {
                runOnUiThread(() -> {
                    Utils.cancelProDialog(p);
                    Utils.startInstall(AboutActivity.this);
                });
            }

            @Override
            public void onDownloading(final int progress) {
                runOnUiThread(() -> p.setProgress(progress));
            }

            @Override
            public void onDownloadFailed() {
                runOnUiThread(() -> {
                    Utils.cancelProDialog(p);
                    application.showErrorToastMsg(Utils.getString(R.string.download_error));
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10001) {
            Utils.startInstall(AboutActivity.this);
        }
    }
}
