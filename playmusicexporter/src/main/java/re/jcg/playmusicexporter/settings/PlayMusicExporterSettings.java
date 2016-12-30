/*
 * Copyright (c) 2015 David Schulte
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package re.jcg.playmusicexporter.settings;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import de.arcus.framework.settings.AppSettings;
import re.jcg.playmusicexporter.R;

/**
 * Helper class to read and write app settings without to care about to open and close an editor
 */
public class PlayMusicExporterSettings extends AppSettings {
    private static PlayMusicExporterSettings settings;
    /**
     * The default settings file
     */
    public static final String DEFAULT_SETTINGS_FILENAME = "play_music_exporter";

    // Preference constants
    private static final String PREF_ID3 = "pref_id3";
    private static final String PREF_ID3_ARTWORK_SIZE = "pref_id3_artwork_size";
    public static final String PREF_EXPORT_URI = "pref_export_uri";
    private static final String PREF_STRUCTURE_ALBA = "pref_structure_albua";
    private static final String PREF_STRUCTURE_GROUPS = "pref_structure_groups";
    public static final String PREF_DRAWER_LEARNED = "pref_drawer_learned";
    public static final String PREF_DRAWER_SELECTED_TYPE = "pref_drawer_selected_type";

    public PlayMusicExporterSettings(Context context) {
        super(context);
    }


    /**
     * Creates a new instance of PlayMusicExporterSettings that access to the default settings file
     * @param context Context of the app
     */
    public static void init(Context context) {
        settings = new PlayMusicExporterSettings(context);

        // Init the default values

        // ID3 settings
        if (!settings.contains(PREF_ID3))
            settings.setString(PREF_ID3, "id3_with_cover");

        // ID3 artwork settings
        if (!settings.contains(PREF_ID3_ARTWORK_SIZE))
            settings.setInt(PREF_ID3_ARTWORK_SIZE, 1024);

        // Export path
        if (!settings.contains(PREF_EXPORT_URI))
            settings.setUri(PREF_EXPORT_URI, Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)));

        // Alba export structure
        if (!settings.contains(PREF_STRUCTURE_ALBA))
            settings.setString(PREF_STRUCTURE_ALBA, context.getString(R.string.settings_export_structure_alba_default_value));

        // Groups export structure
        if (!settings.contains(PREF_STRUCTURE_GROUPS))
            settings.setString(PREF_STRUCTURE_GROUPS, context.getString(R.string.settings_export_structure_groups_default_value));

        // Drawer learned
        if (!settings.contains(PREF_DRAWER_LEARNED))
            settings.setBoolean(PREF_DRAWER_LEARNED, false);

        //
    }


}
