package ma.emsi.recrutementia.utils;

public class OcrTextCleaner {

    public static String clean(String raw) {
        if (raw == null) return "";

        String s = raw;

        // Supprimer caractères bizarres OCR
        s = s.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}\\n]", " ");

        // Normaliser espaces
        s = s.replaceAll("[\\t\\r]+", " ");
        s = s.replaceAll(" +", " ");

        // Nettoyer sauts de ligne excessifs
        s = s.replaceAll("\\n{3,}", "\n\n");

        return s.trim();
    }
}