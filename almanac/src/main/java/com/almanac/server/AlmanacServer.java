package com.almanac.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.tyme.culture.God;
import com.tyme.culture.Taboo;
import com.tyme.eightchar.ChildLimit;
import com.tyme.eightchar.EightChar;
import com.tyme.enums.Gender;
import com.tyme.lunar.LunarDay;
import com.tyme.lunar.LunarHour;
import com.tyme.lunar.LunarMonth;
import com.tyme.lunar.LunarYear;
import com.tyme.sixtycycle.EarthBranch;
import com.tyme.sixtycycle.HeavenStem;
import com.tyme.sixtycycle.HideHeavenStem;
import com.tyme.sixtycycle.SixtyCycle;
import com.tyme.solar.SolarTime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class AlmanacServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new HtmlHandler());
        server.createContext("/api/calculate", new ApiHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on http://localhost:8080");
    }

    static class HtmlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String path = t.getRequestURI().getPath();
            if (path == null || path.isEmpty()) {
                path = "/";
            }
            if ("/".equals(path)) {
                path = "/index.html";
            }
            if (path.contains("..")) {
                t.sendResponseHeaders(400, -1);
                return;
            }

            String resourcePath = "/static" + path;
            byte[] bytes;
            try {
                bytes = ResourceReader.read(resourcePath);
            } catch (IOException e) {
                t.sendResponseHeaders(404, -1);
                return;
            }

            String contentType = getContentType(path);
            if (contentType != null) {
                t.getResponseHeaders().add("Content-Type", contentType);
            }
            t.sendResponseHeaders(200, bytes.length);
            OutputStream os = t.getResponseBody();
            os.write(bytes);
            os.close();
        }

        private String getContentType(String path) {
            if (path.endsWith(".html")) {
                return "text/html; charset=UTF-8";
            }
            if (path.endsWith(".css")) {
                return "text/css; charset=UTF-8";
            }
            if (path.endsWith(".js")) {
                return "application/javascript; charset=UTF-8";
            }
            if (path.endsWith(".json")) {
                return "application/json; charset=UTF-8";
            }
            if (path.endsWith(".png")) {
                return "image/png";
            }
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                return "image/jpeg";
            }
            if (path.endsWith(".svg")) {
                return "image/svg+xml";
            }
            if (path.endsWith(".ico")) {
                return "image/x-icon";
            }
            return null;
        }
    }

    static class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("OPTIONS".equals(t.getRequestMethod())) {
                t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                t.sendResponseHeaders(204, -1);
                return;
            }

            try {
                String query = t.getRequestURI().getQuery();
                String dateStr = getParam(query, "date");
                String timeStr = getParam(query, "time");
                String genderStr = getParam(query, "gender");

                int y = Integer.parseInt(dateStr.split("-")[0]);
                int m = Integer.parseInt(dateStr.split("-")[1]);
                int d = Integer.parseInt(dateStr.split("-")[2]);
                int h = Integer.parseInt(timeStr.split(":")[0]);
                int min = Integer.parseInt(timeStr.split(":")[1]);

                SolarTime solarTime = SolarTime.fromYmdHms(y, m, d, h, min, 0);
                LunarHour lunarHour = solarTime.getLunarHour();
                LunarDay lunarDay = lunarHour.getLunarDay();
                LunarMonth lunarMonth = lunarDay.getLunarMonth();
                LunarYear lunarYear = lunarMonth.getLunarYear();

                EightChar eightChar = lunarHour.getEightChar();
                SixtyCycle yearCol = eightChar.getYear();
                SixtyCycle monthCol = eightChar.getMonth();
                SixtyCycle dayCol = eightChar.getDay();
                SixtyCycle hourCol = eightChar.getHour();
                HeavenStem me = dayCol.getHeavenStem();

                JsonObject res = new JsonObject();

                JsonObject lunar = new JsonObject();
                lunar.put("solarStr", y + "年" + m + "月" + d + "日 " + h + ":" + String.format("%02d", min));
                lunar.put("week", solarTime.getSolarDay().getWeek().getName() + "星期");
                lunar.put("yearName", lunarYear.getName());
                lunar.put("monthName", lunarMonth.getName());
                lunar.put("dayName", lunarDay.getName());
                lunar.put("yearGz", lunarYear.getSixtyCycle().getName());
                lunar.put("monthGz", lunarMonth.getSixtyCycle().getName());
                lunar.put("dayGz", lunarDay.getSixtyCycle().getName());
                lunar.put("hourGz", lunarHour.getSixtyCycle().getName());
                lunar.put("zodiac", lunarYear.getSixtyCycle().getEarthBranch().getZodiac().getName());
                lunar.put("solarTerm", lunarDay.getSolarDay().getTerm() != null ? lunarDay.getSolarDay().getTerm().getName() : "无");
                res.put("lunar", lunar);

                JsonObject almanac = new JsonObject();
                almanac.put("yi", lunarDay.getRecommends().stream().map(Taboo::getName).collect(Collectors.joining(" ")));
                almanac.put("ji", lunarDay.getAvoids().stream().map(Taboo::getName).collect(Collectors.joining(" ")));
                almanac.put("chong", lunarDay.getSixtyCycle().getEarthBranch().getOpposite().getName() + " (" + lunarDay.getSixtyCycle().getEarthBranch().getOpposite().getZodiac().getName() + ")");
                almanac.put("sha", lunarDay.getSixtyCycle().getEarthBranch().getOminous().getName() + "煞");
                almanac.put("jishen", lunarDay.getGods().stream().filter(g -> "吉".equals(g.getLuck().getName())).map(God::getName).collect(Collectors.joining(" ")));
                almanac.put("xiongshen", lunarDay.getGods().stream().filter(g -> "凶".equals(g.getLuck().getName())).map(God::getName).collect(Collectors.joining(" ")));
                almanac.put("taishen", lunarDay.getFetusDay().getName());
                almanac.put("pengzu", lunarDay.getSixtyCycle().getHeavenStem().getPengZuHeavenStem().getName() + " " + lunarDay.getSixtyCycle().getEarthBranch().getPengZuEarthBranch().getName());
                almanac.put("jiuxing", lunarDay.getNineStar().getName());
                res.put("almanac", almanac);

                JsonObject bazi = new JsonObject();
                JsonArray pillars = new JsonArray();
                pillars.add(buildPillar(yearCol, false, me, "1".equals(genderStr)));
                pillars.add(buildPillar(monthCol, false, me, "1".equals(genderStr)));
                pillars.add(buildPillar(dayCol, true, me, "1".equals(genderStr)));
                pillars.add(buildPillar(hourCol, false, me, "1".equals(genderStr)));
                bazi.put("pillars", pillars);
                bazi.put("mingGong", eightChar.getOwnSign().getName());
                bazi.put("mingGongNayin", eightChar.getOwnSign().getSound().getName());
                bazi.put("shenGong", eightChar.getBodySign().getName());
                bazi.put("shenGongNayin", eightChar.getBodySign().getSound().getName());
                bazi.put("taiYuan", eightChar.getFetalOrigin().getName());
                bazi.put("taiYuanNayin", eightChar.getFetalOrigin().getSound().getName());
                bazi.put("taiXi", eightChar.getFetalBreath().getName());
                bazi.put("taiXiNayin", eightChar.getFetalBreath().getSound().getName());
                try {
                    ChildLimit cl = ChildLimit.fromSolarTime(solarTime, "1".equals(genderStr) ? Gender.MAN : Gender.WOMAN);
                    bazi.put("childLimitText", "出生后 " + cl.getYearCount() + " 年 " + cl.getMonthCount() + " 个月 " + cl.getDayCount() + " 天起运，即公历 " + cl.getEndTime().getYear() + "年" + cl.getEndTime().getMonth() + "月" + cl.getEndTime().getDay() + "日交运。");
                } catch (Exception e) {
                    bazi.put("childLimitText", "起运计算失败");
                }
                res.put("bazi", bazi);

                byte[] bytes = res.toString().getBytes(StandardCharsets.UTF_8);
                t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                t.sendResponseHeaders(200, bytes.length);
                OutputStream os = t.getResponseBody();
                os.write(bytes);
                os.close();
            } catch (Exception e) {
                String err = "{\"error\": \"" + escape(e.getMessage()) + "\"}";
                byte[] bytes = err.getBytes(StandardCharsets.UTF_8);
                t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                t.sendResponseHeaders(500, bytes.length);
                OutputStream os = t.getResponseBody();
                os.write(bytes);
                os.close();
            }
        }

        private String getParam(String query, String key) {
            if (query == null) {
                return "";
            }
            for (String param : query.split("&")) {
                String[] pair = param.split("=", 2);
                if (pair.length > 1 && pair[0].equals(key)) {
                    try {
                        return URLDecoder.decode(pair[1], "UTF-8");
                    } catch (Exception e) {
                        return pair[1];
                    }
                }
            }
            return "";
        }

        private JsonObject buildPillar(SixtyCycle cycle, boolean isDay, HeavenStem me, boolean isMan) {
            JsonObject p = new JsonObject();
            HeavenStem stem = cycle.getHeavenStem();
            EarthBranch branch = cycle.getEarthBranch();
            p.put("name", cycle.getName());
            p.put("mainStar", isDay ? "元" + (isMan ? "男" : "女") : me.getTenStar(stem).getName());
            p.put("stem", stem.getName());
            p.put("stemElement", stem.getElement().getName());
            p.put("branch", branch.getName());
            p.put("branchElement", branch.getElement().getName());

            JsonArray hiddenStems = new JsonArray();
            List<HideHeavenStem> mains = branch.getHideHeavenStems();
            for (HideHeavenStem hs : mains) {
                JsonObject h = new JsonObject();
                h.put("stem", hs.getName());
                h.put("element", hs.getHeavenStem().getElement().getName());
                h.put("star", me.getTenStar(hs.getHeavenStem()).getName());
                hiddenStems.add(h);
            }
            p.put("hiddenStems", hiddenStems);
            p.put("terrain", me.getTerrain(branch).getName());

            EarthBranch[] extras = cycle.getExtraEarthBranches();
            StringBuilder emp = new StringBuilder();
            for (EarthBranch eb : extras) {
                emp.append(eb.getName());
            }
            p.put("emptiness", emp.length() > 0 ? emp.toString() : "无");
            p.put("nayin", cycle.getSound().getName());
            return p;
        }
    }

    static class ResourceReader {
        static byte[] read(String path) throws IOException {
            InputStream inputStream = ResourceReader.class.getResourceAsStream(path);
            if (inputStream == null) {
                throw new IOException("resource not found: " + path);
            }
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
                return outputStream.toByteArray();
            } finally {
                inputStream.close();
            }
        }
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    static class JsonObject {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        JsonObject() {
            sb.append("{");
        }

        JsonObject put(String key, String value) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(key).append("\":\"").append(escape(value)).append("\"");
            first = false;
            return this;
        }

        JsonObject put(String key, JsonObject obj) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(key).append("\":").append(obj.toString());
            first = false;
            return this;
        }

        JsonObject put(String key, JsonArray arr) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(key).append("\":").append(arr.toString());
            first = false;
            return this;
        }

        public String toString() {
            return sb.toString() + "}";
        }
    }

    static class JsonArray {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        JsonArray() {
            sb.append("[");
        }

        JsonArray add(JsonObject obj) {
            if (!first) {
                sb.append(",");
            }
            sb.append(obj.toString());
            first = false;
            return this;
        }

        public String toString() {
            return sb.toString() + "]";
        }
    }
}
