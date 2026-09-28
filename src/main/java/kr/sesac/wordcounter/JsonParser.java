package kr.sesac.wordcounter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

    public class JsonParser implements TextParser {
        static final String[] FIELDS = {"text"};   //분석할 필드 (설정)

        @Override
        public List<String> parse(Path file) throws IOException {
            List<String> texts = new ArrayList<>();
            try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                JsonElement root = new Gson().fromJson(reader, JsonElement.class);   //파일 → JSON 트리
                if (root == null) return texts;                                       //빈 파일
                if (!root.isJsonArray()) throw new IOException("JSON은 배열이어야 합니다.");

                for (JsonElement item : root.getAsJsonArray()) {        //레코드마다 (CSV의 record)
                    JsonObject record = item.getAsJsonObject();
                    for (String field : FIELDS) {                       //분석 필드마다 (CSV의 column)
                        JsonElement value = record.get(field);
                        if (value == null) throw new IOException("분석 필드가 없습니다: " + field);
                        texts.add(value.getAsString());
                    }
                }
            } catch (JsonParseException | IllegalStateException e) {    //깨진 JSON → Main이 잡는 IOException으로
                throw new IOException("JSON 형식이 올바르지 않습니다: " + e.getMessage());
            }
            return texts;
        }
    }

