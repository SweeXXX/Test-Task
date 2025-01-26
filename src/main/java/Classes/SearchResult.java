package Classes;

import java.util.*;

import static Classes.NumericProcess.isNumber;

public class SearchResult {
    private String search;
    private List<Integer> result;
    private long time;

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }
    public List<Integer> getResult() { return result; }
    public void setResult(List<Integer> result) { this.result = result; }
    public long getTime() { return time; }
    public void setTime(long time) { this.time = time; }

    public static boolean checkIfNumeric(List<RowIndexEntry> indexList) {
        if (indexList.isEmpty()) return false;
        int checkCount = Math.min(1000, indexList.size());
        for (int i = 0; i < checkCount; i++) {
            String val = indexList.get(i).getColumnValue();
            if (!isNumber(val)) return false;
        }
        return true;
    }



    public static List<Integer> findPrefixRange(List<RowIndexEntry> indexList, String prefix, boolean isNumeric) {
        List<Integer> result = new ArrayList<>();
        if (prefix == null || prefix.isEmpty()) {
            for (RowIndexEntry entry : indexList) {
                result.add(entry.getLineNumber());
            }
            return result;
        }

        prefix = prefix.trim().toLowerCase();

        if (isNumeric) {
            try {
                long numericPrefix = Long.parseLong(prefix);
                int start = findLowerBound(indexList, numericPrefix);
                int end = findUpperBound(indexList, numericPrefix);

                for (int i = start; i < end; i++) {
                    String val = indexList.get(i).getColumnValue();
                    if (val.startsWith(prefix)) {
                        result.add(indexList.get(i).getLineNumber());
                    }
                }
            } catch (NumberFormatException e) {
                return result;
            }
        } else {
            int low = 0;
            int high = indexList.size() - 1;
            int start = -1;

            // Поиск начального индекса
            while (low <= high) {
                int mid = (low + high) / 2;
                String midVal = indexList.get(mid).getColumnValue().toLowerCase();

                if (midVal.startsWith(prefix)) {
                    start = mid;
                    high = mid - 1;
                } else if (midVal.compareTo(prefix) < 0) {
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }

            if (start == -1) return result;

            // Поиск конечного индекса
            int end = start;
            while (end < indexList.size() &&
                    indexList.get(end).getColumnValue().toLowerCase().startsWith(prefix)) {
                end++;
            }

            for (int i = start; i < end; i++) {
                result.add(indexList.get(i).getLineNumber());
            }
        }

        return result;
    }
    private static int findLowerBound(List<RowIndexEntry> list, long key) {
        int left = 0, right = list.size();
        while (left < right) {
            int mid = (left + right) / 2;
            if (list.get(mid).getNumericValue() < key)
                left = mid + 1;
            else
                right = mid;
        }
        return left;
    }

    private static int findUpperBound(List<RowIndexEntry> list, long key) {
        int left = 0, right = list.size();
        while (left < right) {
            int mid = (left + right) / 2;
            if (list.get(mid).getNumericValue() < key)
                left = mid + 1;
            else
                right = mid;
        }
        return left;
    }
}