package re.jcg.playmusicexporter.settings;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

public class PlayMusicExporterPreferences {
    private static SharedPreferences preferences;

    private static final String AUTO_EXPORT_USES_DIFFERENT_PATH = "preference_auto_export_use_different_path";
    private static final boolean AUTO_EXPORT_USES_DIFFERENT_PATH_DEFAULT = false;
    private static final String AUTO_EXPORT_USES_DIFFERENT_STRUCTURE = "preference_auto_export_use_different_structure";
    private static final boolean AUTO_EXPORT_USES_DIFFERENT_STRUCTURE_DEFAULT = false;

    private static final String AUTO_EXPORT_PATH = "preference_auto_export_path";
    //TODO Split Export Paths in export prefs, this won't work else.
    private static final String ALBA_EXPORT_PATH = "preference_alba_export_path";
    private static final String GROUP_EXPORT_PATH = "preference_group_export_path";
    private static final String URI_DEFAULT = Uri.EMPTY.toString();

    private static final String AUTO_EXPORT_STRUCTURE = "preference_auto_export_structure";
    private static final String ALBA_EXPORT_STRUCTURE = "preference_auto_export_structure";
    private static final String EXPORT_STRUCTURE_DEFAULT = "{album-artist}/{album}/{disc=CD $}/{no=$$.} {title}.mp3";


    public static void init(Context pContext) {
        preferences = PreferenceManager.getDefaultSharedPreferences(pContext);
    }

    public static Uri getConditionedAutoExportPath() {
        if (getAutoExportUsesDifferentPath()) {
            return getAutoExportPath();
        } else {
            return getAlbaExportPath();
        }
    }

    public static boolean getAutoExportUsesDifferentPath() {
        return preferences.getBoolean(AUTO_EXPORT_USES_DIFFERENT_PATH, AUTO_EXPORT_USES_DIFFERENT_PATH_DEFAULT);
    }

    public static Uri getAutoExportPath() {
        return getUri(AUTO_EXPORT_PATH);
    }

    public static Uri getAlbaExportPath() {
        return getUri(ALBA_EXPORT_PATH);
    }

    public static Uri getGroupExportPath() {
        return getUri(GROUP_EXPORT_PATH);
    }

    private static Uri getUri(String key) {
        return Uri.parse(preferences.getString(key, URI_DEFAULT));
    }

    public static String getConditionedAutoExportStructure() {
        if (getAutoExportUsesDifferentStructure()) {
            return getAutoExportStructure();
        } else {
            return getAlbaExportStructure();
        }
    }

    public static String getAlbaExportStructure() {
        return preferences.getString(ALBA_EXPORT_STRUCTURE, EXPORT_STRUCTURE_DEFAULT);
    }

    public static String getAutoExportStructure() {
        return preferences.getString(AUTO_EXPORT_STRUCTURE, EXPORT_STRUCTURE_DEFAULT);
    }

    public static boolean getAutoExportUsesDifferentStructure() {
        return preferences.getBoolean(AUTO_EXPORT_USES_DIFFERENT_STRUCTURE, AUTO_EXPORT_USES_DIFFERENT_STRUCTURE_DEFAULT);
    }
}
