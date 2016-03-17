package de.pasligh.android.teamme.tools;

import de.pasligh.android.teamme.R;

/**
 * Created by Thomas on 15.03.2016.
 */
public class ShareHelper {

    private static boolean CREATE_FOOTER = true;

    public static void appendFooter_Signature(StringBuilder p_stb, String p_footer) {
        if (CREATE_FOOTER) {
            p_stb.append("... ").append(p_footer).append(": ").append("\n" +
                    "http://play.google.com/store/apps/details?id=de.pasligh.android.teamme");
            CREATE_FOOTER = false;
        }
    }

}
