package Classes;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class BuildJSON {
    public static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                if (i + 1 >= args.length) {
                    System.err.println("Отсутствует значение для аргумента: " + arg);
                    System.exit(1);
                }
                map.put(arg, args[i + 1]);
                i++;
            } else {
                System.err.println("Неизвестный аргумент: " + arg);
                System.exit(1);
            }
        }
        return map;
    }

    private static String escapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    public static String buildJsonOutput(long initTime, List<SearchResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"initTime\":").append(initTime).append(",\"result\":[");
        if(!results.isEmpty()) sb.append('\n');
        for (int i = 0; i < results.size(); i++) {
            SearchResult sr = results.get(i);
            sb.append("\t{\"search\":\"").append(escapeJson(sr.getSearch())).append("\",");
            sb.append("\"result\":[");
            List<Integer> res = sr.getResult();
            for (int j = 0; j < res.size(); j++) {
                sb.append(res.get(j));
                if (j < res.size() - 1) sb.append(",");
            }
            sb.append("],\"time\":").append(sr.getTime()).append("}");
            if (i < results.size() - 1) sb.append(",\n");
            else sb.append("\n");
        }
        sb.append("]}");
        return sb.toString();
    }
}