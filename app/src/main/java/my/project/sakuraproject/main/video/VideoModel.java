package my.project.sakuraproject.main.video;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import my.project.sakuraproject.bean.AnimeDescBean;
import my.project.sakuraproject.config.AnimeType;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.net.HttpGet;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class VideoModel implements VideoContract.Model {
    private List<String> videoUrlList = new ArrayList<>();

    @Override
    public void getData(String title, String HTML_url, VideoContract.LoadDataCallback callback) {

        new HttpGet(HTML_url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Document doc = Jsoup.parse(response.body().string());
                String fid = DatabaseUtil.getAnimeID(title);
                DatabaseUtil.addIndex(fid, HTML_url);
                callback.successDrama(getAllDrama(fid, doc.select("div.movurls > ul > li")));
                Elements playList = doc.select("div.playbo > a");
                if (playList.size() > 0) {
                    for (int i = 0, size = playList.size(); i < size; i++) {
                        videoUrlList.add(playList.get(i).attr("onClick"));
                    }
                    callback.success(videoUrlList);
                } else {
                    callback.empty();
                }
            }
        });
    }

    private static List<AnimeDescBean> getAllDrama(String fid, Elements dramaList) {
        List<AnimeDescBean> list = new ArrayList<>();
        try {
            String dataBaseDrama = DatabaseUtil.queryAllIndex(fid);
            String dramaTitle;
            String dramaUrl;
            for (int i = 0, size = dramaList.size(); i < size; i++) {
                dramaUrl = dramaList.get(i).select("a").attr("href");
                dramaTitle = dramaList.get(i).select("a").text();
                if (dataBaseDrama.contains(dramaUrl))
                    list.add(new AnimeDescBean(AnimeType.TYPE_LEVEL_1, true, dramaTitle, dramaUrl, "play"));
                else
                    list.add(new AnimeDescBean(AnimeType.TYPE_LEVEL_1, false, dramaTitle, dramaUrl, "play"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return list;
        }
        return list;
    }
}
