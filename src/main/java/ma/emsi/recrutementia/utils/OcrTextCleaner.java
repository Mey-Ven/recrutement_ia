package ma.emsi.recrutementia.utils;

import java.text.Normalizer;

public class OcrTextCleaner {

    private OcrTextCleaner() {}

    public static String clean(String raw) {
        if (raw == null) return "";

        String s = Normalizer.normalize(raw, Normalizer.Form.NFC);

        // remove control chars except \n \t
        s = s.replaceAll("[\\p{Cntrl}&&[^\n\t]]", " ");

        // fix hyphen line breaks: "dévelop-\npement" -> "développement"
        s = s.replaceAll("-\\s*\\n\\s*", "");

        // normalize new lines and spaces
        s = s.replace("\r", "\n");
        s = s.replaceAll("[\\n\\t]+", " ");
        s = s.replaceAll("\\s{2,}", " ").trim();

        return s;
    }
}
