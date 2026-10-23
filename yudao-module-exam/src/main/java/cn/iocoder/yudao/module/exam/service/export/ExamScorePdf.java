package cn.iocoder.yudao.module.exam.service.export;

import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.exam.support.ExamException.require;

@Component
public class ExamScorePdf {
    private static final float MARGIN = 30;
    private static final float SIZE = 8;
    private static final float LINE = 12;
    private static final float[] WIDTHS = {140, 90, 80, 90, 80, 222, 60};
    private static final String[] HEADERS = {"考试 / 时间", "课程", "班级", "学号", "姓名", "分项成绩", "总分"};
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void export(ScoreSnapshotResp snapshot, HttpServletResponse response) throws IOException {
        byte[] bytes = render(snapshot);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=exam-scores.pdf");
        response.setHeader("Cache-Control", "no-store");
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    private byte[] render(ScoreSnapshotResp snapshot) throws IOException {
        try (PDDocument document = new PDDocument();
             InputStream resource = ExamScorePdf.class.getResourceAsStream("/fonts/ExamSans-Regular.ttf");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            require(resource != null, "考试导出中文字体资源缺失");
            PDType0Font font = PDType0Font.load(document, resource, true);
            Map<Long, List<ScoreItemResp>> items = snapshot.getItems().stream()
                    .collect(Collectors.groupingBy(ScoreItemResp::getSessionId));
            try (Table table = new Table(document, font, snapshot)) {
                for (ScoreRowResp row : snapshot.getRows()) {
                    table.row(cells(row, items.getOrDefault(row.getSessionId(), Collections.emptyList())));
                }
                if (snapshot.getRows().isEmpty()) {
                    table.row(new String[]{"无可导出成绩", "", "", "", "", "", ""});
                }
            }
            document.save(output);
            return output.toByteArray();
        } catch (IllegalArgumentException exception) {
            throw cn.iocoder.yudao.module.exam.support.ExamException.error("字体无法显示部分字符，PDF未生成");
        }
    }

    private String[] cells(ScoreRowResp row, List<ScoreItemResp> items) {
        String details = items.stream().sorted(Comparator.comparing(ScoreItemResp::getSort).thenComparing(ScoreItemResp::getId))
                .map(item -> item.getName() + "(" + decimal(item.getWeight()) + "%)："
                        + decimal(row.getScores() == null ? null : row.getScores().get(item.getId())))
                .collect(Collectors.joining("；"));
        return new String[]{row.getSessionName() + "\n" + TIME.format(row.getStartTime()) + "\n至 " + TIME.format(row.getEndTime()),
                row.getCourseName(), row.getClassName(), row.getStudentNo(), row.getStudentName(), details, decimal(row.getFinalScore())};
    }

    private String decimal(BigDecimal value) {
        return value == null ? "未录入" : value.stripTrailingZeros().toPlainString();
    }

    private static class Table implements AutoCloseable {
        private final PDDocument document;
        private final PDType0Font font;
        private final ScoreSnapshotResp snapshot;
        private PDPageContentStream stream;
        private float y;
        private int pageNumber;

        private Table(PDDocument document, PDType0Font font, ScoreSnapshotResp snapshot) throws IOException {
            this.document = document;
            this.font = font;
            this.snapshot = snapshot;
            newPage();
        }

        private void newPage() throws IOException {
            if (stream != null) {
                stream.close();
            }
            PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            pageNumber++;
            y = page.getMediaBox().getHeight() - MARGIN;
            text("考试成绩", MARGIN, y, 15);
            y -= 22;
            text("数据时间：" + TIME.format(snapshot.getGeneratedAt()) + "    第" + pageNumber + "页", MARGIN, y, SIZE);
            y -= 18;
            StatisticsResp statistics = snapshot.getStatistics();
            if (statistics != null) {
                text(summary(statistics), MARGIN, y, SIZE);
                y -= 18;
            }
            List<List<String>> headers = new ArrayList<>();
            for (String header : HEADERS) {
                headers.add(Collections.singletonList(header));
            }
            draw(headers, 0, 1);
        }

        private String summary(StatisticsResp value) {
            return "人数 " + value.getParticipantCount() + "  已录 " + value.getGradedCount() + "  未录 " + value.getUngradedCount()
                    + "  最高 " + number(value.getMaxScore()) + "  最低 " + number(value.getMinScore())
                    + "  均分 " + number(value.getAverageScore()) + "  及格率 " + number(value.getPassRate()) + "%";
        }

        private String number(BigDecimal value) {
            return value == null ? "—" : value.stripTrailingZeros().toPlainString();
        }

        private void row(String[] cells) throws IOException {
            List<List<String>> lines = new ArrayList<>();
            int total = 1;
            for (int column = 0; column < cells.length; column++) {
                List<String> wrapped = wrap(cells[column], WIDTHS[column] - 8);
                lines.add(wrapped);
                total = Math.max(total, wrapped.size());
            }
            int offset = 0;
            while (offset < total) {
                int available = (int) ((y - MARGIN - 8) / LINE);
                if (available < 1) {
                    newPage();
                    continue;
                }
                int count = Math.min(available, total - offset);
                draw(lines, offset, count);
                offset += count;
            }
        }

        private void draw(List<List<String>> lines, int offset, int count) throws IOException {
            float height = count * LINE + 8;
            float x = MARGIN;
            for (int column = 0; column < WIDTHS.length; column++) {
                stream.addRect(x, y - height, WIDTHS[column], height);
                stream.stroke();
                List<String> values = lines.get(column);
                for (int line = 0; line < count && offset + line < values.size(); line++) {
                    text(values.get(offset + line), x + 4, y - 12 - line * LINE, SIZE);
                }
                x += WIDTHS[column];
            }
            y -= height;
        }

        private List<String> wrap(String value, float width) throws IOException {
            List<String> lines = new ArrayList<>();
            String normalized = value == null ? "" : value.replace('\r', ' ').replace('\t', ' ');
            StringBuilder line = new StringBuilder();
            for (int offset = 0; offset < normalized.length();) {
                int codePoint = normalized.codePointAt(offset);
                offset += Character.charCount(codePoint);
                if (codePoint == '\n') {
                    lines.add(line.toString());
                    line.setLength(0);
                    continue;
                }
                String character = new String(Character.toChars(codePoint));
                if (font.getStringWidth(line.toString() + character) * SIZE / 1000 > width && line.length() > 0) {
                    lines.add(line.toString());
                    line.setLength(0);
                }
                line.append(character);
            }
            lines.add(line.toString());
            return lines;
        }

        private void text(String text, float x, float baseline, float size) throws IOException {
            stream.beginText();
            stream.setFont(font, size);
            stream.newLineAtOffset(x, baseline);
            stream.showText(text);
            stream.endText();
        }

        @Override
        public void close() throws IOException {
            if (stream != null) {
                stream.close();
            }
        }
    }
}
