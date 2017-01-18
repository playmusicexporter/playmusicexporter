package re.jcg.playmusicexporter.settings;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

import re.jcg.playmusicexporter.fragments.NavigationDrawerFragment;

public class PlayMusicExporterPreferences {
    private static SharedPreferences preferences;

    private static final String AUTO_EXPORT_ENABLED = "preference_auto_export_enabled";
    private static final boolean AUTO_EXPORT_ENABLED_DEFAULT = false;
    private static final String AUTO_EXPORT_USES_DIFFERENT_PATH = "preference_auto_export_use_different_path";
    private static final boolean AUTO_EXPORT_USES_DIFFERENT_PATH_DEFAULT = false;
    private static final String AUTO_EXPORT_USES_DIFFERENT_STRUCTURE = "preference_auto_export_use_different_structure";
    private static final boolean AUTO_EXPORT_USES_DIFFERENT_STRUCTURE_DEFAULT = false;
    private static final String AUTO_EXPORT_FREQUENCY = "preference_auto_export_frequency";
    private static final String AUTO_EXPORT_FREQUENCY_DEFAULT = "86400000";
    public static final String AUTO_EXPORT_REQUIRE_CHARGING = "preference_auto_export_require_charging";
    public static final String AUTO_EXPORT_REQUIRE_UNMETERED = "preference_auto_export_require_unmetered";
    public static final boolean AUTO_EXPORT_REQUIRE_CONDITION_DEFAULT = false;

    private static final String AUTO_EXPORT_PATH = "preference_auto_export_path";
    //TODO Split Export Paths in export prefs, this won't work else.
    private static final String ALBA_EXPORT_PATH = "preference_alba_export_path";
    private static final String GROUPS_EXPORT_PATH = "preference_groups_export_path";
    private static final String URI_DEFAULT = Uri.fromFile(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)).toString();

    private static final String AUTO_EXPORT_STRUCTURE = "preference_auto_export_structure";
    private static final String ALBA_EXPORT_STRUCTURE = "preference_alba_export_structure";
    private static final String GROUPS_EXPORT_STRUCTURE = "preference_groups_export_structure";
    private static final String EXPORT_STRUCTURE_DEFAULT = "{album-artist}/{album}/{disc=CD $}/{no=$$.} {title}.mp3";

    private static final String EXPORT_ALBUM_ART_SIZE = "preference_id3_artwork_size";
    private static final int EXPORT_ALBUM_ART_SIZE_DEFAULT = 512;

    private static final String DRAWER_LEARNED = "pref_drawer_learned";
    private static final boolean DRAWER_LEARNED_DEFAULT = false;
    private static final String DRAWER_SELECTED_TYPE = "pref_drawer_selected_type";
    private static final String DRAWER_SELECTED_TYPE_DEFAULT = "Album";

    private static final String SETUP_DONE = "preference_setup_done";
    private static final boolean SETUP_DONE_DEFAULT = false;


    private PlayMusicExporterPreferences() {
    }

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

    public static Uri getGroupsExportPath() {
        return getUri(GROUPS_EXPORT_PATH);
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

    public static String getGroupsExportStructure() {
        return preferences.getString(GROUPS_EXPORT_STRUCTURE, EXPORT_STRUCTURE_DEFAULT);
    }

    public static String getAutoExportStructure() {
        return preferences.getString(AUTO_EXPORT_STRUCTURE, EXPORT_STRUCTURE_DEFAULT);
    }

    public static boolean getAutoExportUsesDifferentStructure() {
        return preferences.getBoolean(AUTO_EXPORT_USES_DIFFERENT_STRUCTURE, AUTO_EXPORT_USES_DIFFERENT_STRUCTURE_DEFAULT);
    }

    public static boolean getDrawerLearned() {
        return preferences.getBoolean(DRAWER_LEARNED, DRAWER_LEARNED_DEFAULT);
    }
    public static void setDrawerLearned(boolean drawerLearned) {
        preferences.edit().putBoolean(DRAWER_LEARNED, drawerLearned).apply();
    }

    public static NavigationDrawerFragment.ViewType getDrawerViewType() {
        return NavigationDrawerFragment.ViewType.fromName(preferences.getString(DRAWER_SELECTED_TYPE, DRAWER_SELECTED_TYPE_DEFAULT));
    }

    public static void setDrawerViewType(NavigationDrawerFragment.ViewType viewType) {
        preferences.edit().putString(DRAWER_SELECTED_TYPE, viewType.name()).apply();
    }

    public static void setAlbaExportPath(Uri treeUri) {
        preferences.edit().putString(ALBA_EXPORT_PATH, treeUri.toString()).apply();
    }

    public static void setGroupsExportPath(Uri treeUri) {
        preferences.edit().putString(GROUPS_EXPORT_PATH, treeUri.toString()).apply();
    }

    public static boolean getAutoExportEnabled() {
        return preferences.getBoolean(AUTO_EXPORT_ENABLED, AUTO_EXPORT_ENABLED_DEFAULT);
    }

    public static void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    public static long getAutoExportFrequency() {
        return Long.parseLong(preferences.getString(AUTO_EXPORT_FREQUENCY, AUTO_EXPORT_FREQUENCY_DEFAULT));
    }

    public static boolean getAutoExportRequireUnmetered() {
        return preferences.getBoolean(AUTO_EXPORT_REQUIRE_UNMETERED, AUTO_EXPORT_REQUIRE_CONDITION_DEFAULT);
    }

    public static boolean getAutoExportRequireCharging() {
        return preferences.getBoolean(AUTO_EXPORT_REQUIRE_CHARGING, AUTO_EXPORT_REQUIRE_CONDITION_DEFAULT);
    }

    public static boolean getSetupDone() {
        return preferences.getBoolean(SETUP_DONE, SETUP_DONE_DEFAULT);
    }

    public static void setSetupDone(boolean done) {
        preferences.edit().putBoolean(SETUP_DONE, done).apply();
    }

    public static int getAlbumArtSize() {
        return Integer.parseInt(preferences.getString(EXPORT_ALBUM_ART_SIZE, "" + EXPORT_ALBUM_ART_SIZE_DEFAULT));
    }

    public static void setAlbumArtSize(int size) {
        preferences.edit().putString(EXPORT_ALBUM_ART_SIZE, "" + size).apply();
    }
}
