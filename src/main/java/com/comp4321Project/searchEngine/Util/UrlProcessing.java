package com.comp4321Project.searchEngine.Util;

public class UrlProcessing {
    public static String getBaseUrl(String url) throws IllegalArgumentException {
        int start = url.indexOf("://");
        if (start == -1) {
            if (!url.contains(".")) {
                throw new IllegalArgumentException("Url " + url + " has no valid base url!");
            }
            start = 0;
        } else {
            start += 3;
        }

        int end = start;
        while (end < url.length() && url.charAt(end) != '/') {
            end++;
        }

        return url.substring(start, end);
    }

    public static boolean isUrlEqual(String u1, String u2) throws IllegalArgumentException {
        return trimHeaderAndSlashAtTheEnd(u1).equals(trimHeaderAndSlashAtTheEnd(u2));
    }

    public static String trimHeaderAndSlashAtTheEnd(String rawUrl) throws IllegalArgumentException {
        String returnUrl = rawUrl;

        // trim header
        int start = rawUrl.indexOf("://");
        if (start != -1) {
            returnUrl = returnUrl.substring(start + 3, returnUrl.length());
        } else if (returnUrl.contains(".")) {
            // do nothing
        } else if (returnUrl.contains(":")) {
            throw new IllegalArgumentException("Url " + rawUrl + " is not a valid url!");
        } else {
            // unknown url
            // not treating header
        }

        // trim slash
        while (returnUrl.endsWith("/")) {
            returnUrl = returnUrl.substring(0, returnUrl.length() - 1);
        }

        return returnUrl;
    }

    public static boolean containsOtherFileType(String link) {
        if (!link.contains(".") || link.contains(".html")) {
            return false;
        } else {
            return true;
        }
    }

    public static String cleanContentAfterFileType(String fullLink) {
        int index = fullLink.indexOf(".html");
        if (index == -1) return fullLink;
        return fullLink.substring(0, index + 5);
    }
}
