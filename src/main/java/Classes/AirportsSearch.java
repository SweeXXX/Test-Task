package Classes;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static Classes.NumericProcess.isNumericString;

public class AirportsSearch {
    public static void main(String[] args) {
        Map<String, String> params = BuildJSON.parseArgs(args);

        String dataFilePath = params.get("--data");
        String inputFilePath = params.get("--input-file");
        String outputFilePath = params.get("--output-file");
        String indexedColumnIdStr = params.get("--indexed-column-id");

        if (dataFilePath == null || inputFilePath == null || outputFilePath == null || indexedColumnIdStr == null) {
            System.err.println("Missing required parameters");
            System.exit(1);
        }

        int indexedColumnId;
        try {
            indexedColumnId = Integer.parseInt(indexedColumnIdStr);
            if (indexedColumnId < 1) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.err.println("Invalid column index: " + indexedColumnIdStr);
            System.exit(1);
            return;
        }

        long startTime = System.currentTimeMillis();

        // 1. Чтение входных запросов
        List<String> searchQueries = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    searchQueries.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading input file: " + e.getMessage());
            System.exit(1);
        }

        // 2. Построение индекса
        List<RowIndexEntry> indexList = new ArrayList<>();
        boolean isNumeric = false;
        try (BufferedReader br = new BufferedReader(new FileReader(dataFilePath, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                List<String> parts = parseCsvLine(line);
                if (parts.size() < indexedColumnId) continue;

                try {
                    int lineNumber = Integer.parseInt(parts.get(0).trim());
                    String columnValue = parts.get(indexedColumnId - 1)
                            .trim()
                            .replaceAll("^\"|\"$", ""); // Удаление кавычек

                    indexList.add(new RowIndexEntry(columnValue, lineNumber));
                }
                catch (Exception e) {
                    // Пропуск некорректных строк
                }
            }

            // Определение типа колонки
            isNumeric = isColumnNumeric(indexList);

            // Сортировка данных
            if (isNumeric) {
                indexList.sort(Comparator.comparing(row -> {
                    try {
                        return Long.parseLong(row.getColumnValue());
                    } catch (NumberFormatException e) {
                        return Long.MAX_VALUE;
                    }
                }));
            } else {
                indexList.sort(Comparator.comparing(row ->
                        row.getColumnValue().toLowerCase()
                ));
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            System.exit(1);
        }

        long initTime = System.currentTimeMillis() - startTime;

        // 3. Выполнение поиска
        List<SearchResult> results = new ArrayList<>();
        for (String query : searchQueries) {
            long searchStart = System.currentTimeMillis();
            List<Integer> found = performSearch(indexList, query, isNumeric);
            // Время поиска
            long searchTime = System.currentTimeMillis() - searchStart;

            SearchResult sr = new SearchResult();
            sr.setSearch(query);
            sr.setResult(found);
            sr.setTime(searchTime);
            results.add(sr);
        }

        // 4. Запись результатов
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(outputFilePath, StandardCharsets.UTF_8))
        ) {
            writer.write(BuildJSON.buildJsonOutput(initTime, results));
        } catch (IOException e) {
            System.err.println("Error writing output file: " + e.getMessage());
            System.exit(1);
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> parts = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                parts.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString().trim());
        return parts;
    }

    private static boolean isColumnNumeric(List<RowIndexEntry> entries) {
        if (entries.isEmpty()) return false;

        // Проверяем первые 100 значений
        int samples = Math.min(100, entries.size());
        int numericCount = 0;

        for (int i = 0; i < samples; i++) {
            String value = entries.get(i).getColumnValue();
            if (isNumericString(value)) {
                numericCount++;
            }
        }
        return numericCount > samples * 0.9; // 90% значений должны быть числами
    }

    private static List<Integer> performSearch(
            List<RowIndexEntry> index,
            String query,
            boolean isNumeric
    ) {
        List<Integer> results = new ArrayList<>();
        String cleanQuery = query.trim().toLowerCase();

        if (isNumeric) {
            if (!cleanQuery.matches("-?\\d+")) return results;
            long numericQuery = Long.parseLong(cleanQuery);
            String queryStr = String.valueOf(numericQuery);

            for (RowIndexEntry entry : index) {
                String val = entry.getColumnValue();
                if (val.startsWith(queryStr)) {
                    results.add(entry.getLineNumber());
                }
            }
        } else {
            int left = 0;
            int right = index.size() - 1;
            int startPos = -1;

            // Поиск первого вхождения
            while (left <= right) {
                int mid = (left + right) / 2;
                String midVal = index.get(mid).getColumnValue().toLowerCase();

                if (midVal.startsWith(cleanQuery)) {
                    startPos = mid;
                    right = mid - 1;
                } else if (midVal.compareTo(cleanQuery) < 0) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }

            if (startPos == -1) return results;

            // Поиск последнего вхождения
            int endPos = startPos;
            while (endPos < index.size() &&
                    index.get(endPos).getColumnValue().toLowerCase().startsWith(cleanQuery)) {
                endPos++;
            }

            for (int i = startPos; i < endPos; i++) {
                results.add(index.get(i).getLineNumber());
            }
        }

        return results;
    }
}