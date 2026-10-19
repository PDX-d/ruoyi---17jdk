package cn.iocoder.yudao.module.timetable.service.export;

import cn.iocoder.yudao.module.timetable.controller.admin.vo.LessonQueryReqVO;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.LessonRespVO;
import cn.iocoder.yudao.module.timetable.service.LessonService;
import cn.iocoder.yudao.module.timetable.support.TimetableException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.font.FontRenderContext;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.ToDoubleFunction;

@Service
public class TimetableExportService {
    private static final String FONT = "/fonts/TimetableSans-Regular.ttf";
    private static final float FONT_SIZE = 11;
    private static final int[] WIDTHS = {115, 145, 210, 120, 250, 180, 80};
    private static final String[] HEADERS = {"日期", "时间", "课程", "教师", "班级", "教室", "状态"};
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    @Resource private LessonService lessons;

    public void export(LessonQueryReqVO request, HttpServletResponse response) throws IOException {
        TimetableException.require("png".equals(request.getFormat()) || "pdf".equals(request.getFormat()), "导出格式必须是 png 或 pdf");
        TimetableException.require(request.getStartDate() != null && request.getEndDate() != null
                && !request.getEndDate().isBefore(request.getStartDate()), "导出需要明确日期范围");
        if ("png".equals(request.getFormat())) {
            TimetableException.require(ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) < 7,
                    "PNG 仅支持七天以内，请改用 PDF 导出");
        }
        List<LessonRespVO> data = lessons.exportData(request);
        byte[] bytes = "png".equals(request.getFormat()) ? png(request, data) : pdf(request, data);
        response.setContentType("png".equals(request.getFormat()) ? "image/png" : "application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=timetable." + request.getFormat());
        response.setHeader("Cache-Control", "no-store");
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    private InputStream fontStream() {
        InputStream stream = TimetableExportService.class.getResourceAsStream(FONT);
        TimetableException.require(stream != null, "模块中文字体资源缺失");
        return stream;
    }

    private String[] cells(LessonRespVO lesson) {
        String endDate = lesson.getStartTime().toLocalDate().equals(lesson.getEndTime().toLocalDate())
                ? "" : lesson.getEndTime().toLocalDate() + " ";
        return new String[]{lesson.getStartTime().toLocalDate().toString(),
                TIME.format(lesson.getStartTime()) + " - " + endDate + TIME.format(lesson.getEndTime()),
                lesson.getCourseName(), lesson.getTeacherName(), String.join("、", lesson.getClassNames()),
                lesson.getClassroomName(), lesson.getStatus() == 1 ? "已停课" : "正常"};
    }

    private List<String> wrap(String text, float availableWidth, ToDoubleFunction<String> measure) {
        String normalized = text == null ? "" : text.replaceAll("[\\r\\n\\t]", " ");
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (int offset = 0; offset < normalized.length();) {
            int codePoint = normalized.codePointAt(offset);
            String character = new String(Character.toChars(codePoint));
            if (measure.applyAsDouble(line.toString() + character) > availableWidth && line.length() > 0) {
                lines.add(line.toString());
                line.setLength(0);
            }
            line.appendCodePoint(codePoint);
            offset += Character.charCount(codePoint);
        }
        lines.add(line.toString());
        return lines;
    }

    private List<List<String>> lines(String[] cells, float scale, ToDoubleFunction<String> measure) {
        List<List<String>> lines = new ArrayList<>();
        for (int i = 0; i < cells.length; i++) {
            lines.add(wrap(cells[i], WIDTHS[i] * scale - 12, measure));
        }
        return lines;
    }

    private int lineCount(List<List<String>> lines) {
        return lines.stream().mapToInt(List::size).max().orElse(1);
    }

    private String title(LocalDate start, LocalDate end) {
        return "课程表  " + start + " 至 " + end;
    }

    private byte[] png(LessonQueryReqVO request, List<LessonRespVO> data) throws IOException {
        Font font;
        try (InputStream stream = fontStream()) {
            font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(18F);
        } catch (FontFormatException exception) {
            throw TimetableException.invalid("模块中文字体格式无效");
        }
        List<List<List<String>>> rows = new ArrayList<>();
        int height = 140;
        for (LessonRespVO lesson : data) {
            List<List<String>> row = lines(cells(lesson), 1F,
                    text -> font.getStringBounds(text, new FontRenderContext(null, true, true)).getWidth());
            rows.add(row);
            height += lineCount(row) * 26 + 18;
        }
        TimetableException.require(height <= 16000, "课次过多导致 PNG 过大，请改用 PDF");
        int width = java.util.Arrays.stream(WIDTHS).sum() + 60;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setFont(font.deriveFont(24F));
            drawText(graphics, title(request.getStartDate(), request.getEndDate()), 30, 40);
            graphics.setFont(font);
            int x = 30;
            for (int i = 0; i < HEADERS.length; i++) {
                drawText(graphics, HEADERS[i], x + 6, 90);
                x += WIDTHS[i];
            }
            int y = 110;
            for (int r = 0; r < rows.size(); r++) {
                List<List<String>> row = rows.get(r);
                int rowHeight = lineCount(row) * 26 + 18;
                graphics.setColor(data.get(r).getStatus() == 1 ? new Color(255, 235, 235) : new Color(245, 247, 250));
                graphics.fillRect(30, y, width - 60, rowHeight);
                x = 30;
                for (int c = 0; c < row.size(); c++) {
                    int textY = y + 25;
                    for (String line : row.get(c)) {
                        drawText(graphics, line, x + 6, textY);
                        textY += 26;
                    }
                    x += WIDTHS[c];
                }
                y += rowHeight;
            }
            if (rows.isEmpty()) {
                drawText(graphics, "所选区间暂无课次", 30, 130);
            }
        } finally {
            graphics.dispose();
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        TimetableException.require(ImageIO.write(image, "png", output), "PNG 编码失败");
        return output.toByteArray();
    }

    private void drawText(Graphics2D graphics, String text, int x, int y) {
        TimetableException.require(graphics.getFont().canDisplayUpTo(text) == -1, "导出文本包含字体不支持的字符");
        graphics.setColor(Color.DARK_GRAY);
        graphics.drawString(text, x, y);
    }

    private byte[] pdf(LessonQueryReqVO request, List<LessonRespVO> data) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument(); InputStream stream = fontStream()) {
            PDType0Font font = PDType0Font.load(document, stream, true);
            LocalDate week = request.getStartDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            while (!week.isAfter(request.getEndDate())) {
                LocalDate first = week.isBefore(request.getStartDate()) ? request.getStartDate() : week;
                LocalDate last = week.plusDays(6).isAfter(request.getEndDate()) ? request.getEndDate() : week.plusDays(6);
                List<LessonRespVO> weekly = data.stream().filter(lesson -> lesson.getStartTime().isBefore(last.plusDays(1).atStartOfDay())
                        && lesson.getEndTime().isAfter(first.atStartOfDay())).collect(Collectors.toList());
                pdfWeek(document, font, first, last, weekly);
                week = week.plusWeeks(1);
            }
            document.save(output);
        }
        return output.toByteArray();
    }

    private PDPage newPage(PDDocument document) {
        PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
        document.addPage(page);
        return page;
    }

    private void pdfWeek(PDDocument document, PDType0Font font, LocalDate start, LocalDate end,
                         List<LessonRespVO> data) throws IOException {
        PDPageContentStream content = null;
        try {
            content = new PDPageContentStream(document, newPage(document));
            header(content, font, start, end);
            float y = 510;
            if (data.isEmpty()) {
                pdfText(content, font, "所选区间暂无课次", 30, y);
            }
            for (LessonRespVO lesson : data) {
                List<List<String>> row = lines(cells(lesson), 0.69F, text -> pdfWidth(font, text));
                int max = lineCount(row);
                for (int offset = 0; offset < max;) {
                    if (y < 65) {
                        content.close();
                        content = new PDPageContentStream(document, newPage(document));
                        header(content, font, start, end);
                        y = 510;
                    }
                    int count = Math.min(max - offset, (int) ((y - 40) / 16));
                    float x = 30;
                    for (int column = 0; column < row.size(); column++) {
                        List<String> cell = row.get(column);
                        for (int line = offset; line < Math.min(cell.size(), offset + count); line++) {
                            pdfText(content, font, cell.get(line), x + 3, y - (line - offset) * 16);
                        }
                        x += WIDTHS[column] * 0.69F;
                    }
                    y -= count * 16;
                    offset += count;
                }
                content.setStrokingColor(new Color(200, 200, 200));
                content.moveTo(30, y - 5);
                content.lineTo(790, y - 5);
                content.stroke();
                y -= 20;
            }
        } finally {
            if (content != null) {
                content.close();
            }
        }
    }

    private void header(PDPageContentStream content, PDType0Font font, LocalDate start, LocalDate end) throws IOException {
        pdfText(content, font, title(start, end), 30, 558);
        float x = 30;
        for (int i = 0; i < HEADERS.length; i++) {
            pdfText(content, font, HEADERS[i], x + 3, 530);
            x += WIDTHS[i] * 0.69F;
        }
    }

    private void pdfText(PDPageContentStream content, PDType0Font font, String text, float x, float y) throws IOException {
        content.beginText();
        content.setFont(font, FONT_SIZE);
        content.newLineAtOffset(x, y);
        try {
            content.showText(text);
        } catch (IllegalArgumentException exception) {
            throw TimetableException.invalid("导出文本包含中文字体不支持的字符");
        }
        content.endText();
    }

    private double pdfWidth(PDType0Font font, String text) {
        try {
            return font.getStringWidth(text) * FONT_SIZE / 1000;
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        } catch (IllegalArgumentException exception) {
            throw TimetableException.invalid("导出文本包含中文字体不支持的字符");
        }
    }
}
