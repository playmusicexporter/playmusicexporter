package re.jcg.playmusicexporter;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;


/**
 * Android Application.
 * Normally, we would not need to extend this, but it is required for
 * the leak detection library we use.
 * See {@link LeakCanary}
 */
public class PlayMusicExporter extends Application {
    @Override public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }
}
