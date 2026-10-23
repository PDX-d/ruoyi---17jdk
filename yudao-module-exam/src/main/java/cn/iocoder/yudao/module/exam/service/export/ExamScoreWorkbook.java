package cn.iocoder.yudao.module.exam.service.export;

import cn.idev.excel.FastExcelFactory;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.event.AnalysisEventListener;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.exam.support.ExamException.require;

@Component
public class ExamScoreWorkbook {
    private static final String SCHEMA = "EXAM_SCORE_V1";
    private static final int LIMIT = 5000;
    private static final List<String> FIXED = Arrays.asList("模板版本", "考试ID", "场次版本", "名单ID", "学号", "姓名", "行版本");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void template(ScoreSnapshotResp snapshot, HttpServletResponse response) throws IOException {
        SessionResp session = singleSession(snapshot);
        List<ScoreItemResp> items = orderedItems(snapshot, session.getId());
        List<String> headers = new ArrayList<>(FIXED);
        items.forEach(item -> headers.add(itemHeader(item)));
        List<List<Object>> data = new ArrayList<>();
        for (ScoreRowResp row : snapshot.getRows()) {
            List<Object> cells = new ArrayList<>(Arrays.asList(SCHEMA, text(session.getId()),
                    text(session.getSessionVersion()), text(row.getId()), row.getStudentNo(), row.getStudentName(), text(row.getRowVersion())));
            items.forEach(item -> cells.add(row.getScores() == null ? null : row.getScores().get(item.getId())));
            data.add(cells);
        }
        response.setHeader("Cache-Control", "no-store");
        ExcelUtils.write(response, "exam-score-template-" + session.getId() + ".xlsx", "成绩模板", head(headers), data);
    }

    public void export(ScoreSnapshotResp snapshot, HttpServletResponse response) throws IOException {
        List<ScoreItemResp> items = snapshot.getItems();
        List<String> headers = new ArrayList<>(Arrays.asList("考试ID", "考试名称", "开始时间", "结束时间", "课程", "班级", "学号", "姓名"));
        items.forEach(item -> headers.add("考试" + item.getSessionId() + ":" + itemHeader(item)));
        headers.add("总分");
        headers.add("数据时间");
        List<List<Object>> data = new ArrayList<>();
        for (ScoreRowResp row : snapshot.getRows()) {
            List<Object> cells = new ArrayList<>(Arrays.asList(text(row.getSessionId()), row.getSessionName(),
                    TIME.format(row.getStartTime()), TIME.format(row.getEndTime()), row.getCourseName(), row.getClassName(),
                    row.getStudentNo(), row.getStudentName()));
            items.forEach(item -> cells.add(row.getScores() == null ? null : row.getScores().get(item.getId())));
            cells.add(row.getFinalScore());
            cells.add(TIME.format(snapshot.getGeneratedAt()));
            data.add(cells);
        }
        response.setHeader("Cache-Control", "no-store");
        ExcelUtils.write(response, "exam-scores.xlsx", "考试成绩", head(headers), data);
    }

    public ParsedImport parse(MultipartFile file, Long sessionId, Long sessionVersion, ScoreSnapshotResp snapshot) {
        ParsedImport result = new ParsedImport();
        if (file == null || file.isEmpty() || file.getSize() > 5L * 1024 * 1024) {
            result.error(0, "文件不能为空且不得超过5MB");
            return result;
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            result.error(0, "请使用下载的xlsx成绩模板");
            return result;
        }
        SessionResp session = singleSession(snapshot);
        if (!Objects.equals(session.getId(), sessionId) || !Objects.equals(session.getSessionVersion(), sessionVersion)) {
            result.error(0, "场次版本已变化，请重新下载模板");
            return result;
        }
        List<ScoreItemResp> items = orderedItems(snapshot, sessionId);
        List<String> headers = new ArrayList<>(FIXED);
        items.forEach(item -> headers.add(itemHeader(item)));
        Map<Long, ScoreRowResp> participants = snapshot.getRows().stream()
                .collect(Collectors.toMap(ScoreRowResp::getId, row -> row));
        BoundedListener listener = new BoundedListener();
        try (InputStream input = file.getInputStream()) {
            FastExcelFactory.read(input).autoCloseStream(false).headRowNumber(1)
                    .registerReadListener(listener).doReadAll();
        } catch (Exception exception) {
            result.error(listener.overflow ? LIMIT + 2 : 0, listener.overflow ? "最多导入5000行，不得截断导入"
                    : listener.multipleSheets ? "模板只允许一个工作表" : "Excel结构无效或内容无法解析");
            return result;
        }
        if (!matchesHeader(listener.header, headers)) {
            result.error(1, "模板表头或分项配置已变化，请重新下载模板");
            return result;
        }
        if (listener.rows.isEmpty()) {
            result.error(2, "没有成绩数据");
            return result;
        }
        Set<Long> seen = new HashSet<>();
        List<ScoreRowSave> rows = new ArrayList<>();
        for (SheetRow row : listener.rows) {
            try {
                rows.add(parseRow(row.cells, sessionId, sessionVersion, items, participants, seen));
            } catch (IllegalArgumentException exception) {
                result.error(row.number, exception.getMessage());
            }
        }
        if (result.errors.isEmpty()) {
            ScoreSaveReq request = new ScoreSaveReq();
            request.setSessionId(sessionId);
            request.setSessionVersion(sessionVersion);
            request.setRows(rows);
            result.request = request;
        }
        return result;
    }

    private ScoreRowSave parseRow(Map<Integer, String> cells, Long sessionId, Long sessionVersion,
                                  List<ScoreItemResp> items, Map<Long, ScoreRowResp> participants, Set<Long> seen) {
        check(cells.keySet().stream().allMatch(index -> index >= 0 && index < FIXED.size() + items.size()), "存在模板之外的列");
        check(SCHEMA.equals(cells.get(0)), "模板版本不匹配");
        check(Objects.equals(number(cells.get(1)), sessionId), "考试ID不匹配");
        check(Objects.equals(number(cells.get(2)), sessionVersion), "场次版本不匹配");
        Long participantId = number(cells.get(3));
        ScoreRowResp participant = participants.get(participantId);
        check(participant != null, "名单不在当前考试或可维护范围内");
        check(seen.add(participantId), "名单ID重复");
        check(Objects.equals(cells.get(4), participant.getStudentNo())
                && Objects.equals(cells.get(5), participant.getStudentName()), "学号或姓名与名单不匹配");
        Long rowVersion = number(cells.get(6));
        check(Objects.equals(rowVersion, participant.getRowVersion()), "成绩行版本已变化，请重新下载模板");
        Map<Long, BigDecimal> scores = new LinkedHashMap<>();
        for (int index = 0; index < items.size(); index++) {
            ScoreItemResp item = items.get(index);
            scores.put(item.getId(), score(cells.get(FIXED.size() + index), item.getName()));
        }
        ScoreRowSave row = new ScoreRowSave();
        row.setParticipantId(participantId);
        row.setRowVersion(rowVersion);
        row.setScores(scores);
        return row;
    }

    private BigDecimal score(String value, String name) {
        try {
            BigDecimal score = new BigDecimal(value);
            check(score.compareTo(BigDecimal.ZERO) >= 0 && score.compareTo(new BigDecimal("100")) <= 0
                    && score.scale() <= 2, name + "须为0至100且最多两位小数");
            return score;
        } catch (NumberFormatException | NullPointerException exception) {
            throw new IllegalArgumentException(name + "分数必填且须为数字");
        }
    }

    private Long number(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("考试ID、名单ID和版本字段必须保持模板原值");
        }
    }

    private void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    private SessionResp singleSession(ScoreSnapshotResp snapshot) {
        require(snapshot.getSessions() != null && snapshot.getSessions().size() == 1, "成绩模板必须指定一场考试");
        return snapshot.getSessions().get(0);
    }

    private List<ScoreItemResp> orderedItems(ScoreSnapshotResp snapshot, Long sessionId) {
        return snapshot.getItems().stream().filter(item -> Objects.equals(item.getSessionId(), sessionId))
                .sorted(Comparator.comparing(ScoreItemResp::getSort).thenComparing(ScoreItemResp::getId)).collect(Collectors.toList());
    }

    private boolean matchesHeader(Map<Integer, String> actual, List<String> expected) {
        if (actual.size() != expected.size()) {
            return false;
        }
        for (int index = 0; index < expected.size(); index++) {
            if (!Objects.equals(actual.get(index), expected.get(index))) {
                return false;
            }
        }
        return true;
    }

    private String itemHeader(ScoreItemResp item) {
        return "分项#" + item.getId() + ":" + item.getName() + "(" + item.getWeight().stripTrailingZeros().toPlainString() + "%)";
    }

    private String text(Long value) {
        return String.valueOf(value);
    }

    private List<List<String>> head(List<String> headers) {
        return headers.stream().map(Collections::singletonList).collect(Collectors.toList());
    }

    public static class ParsedImport {
        private ScoreSaveReq request;
        private final List<ImportRowError> errors = new ArrayList<>();

        private void error(int row, String message) {
            ImportRowError error = new ImportRowError();
            error.setRow(row);
            error.setMessage(message);
            errors.add(error);
        }

        public ScoreSaveReq getRequest() { return request; }
        public List<ImportRowError> getErrors() { return errors; }
    }

    private static class SheetRow {
        private final int number;
        private final Map<Integer, String> cells;

        private SheetRow(int number, Map<Integer, String> cells) {
            this.number = number;
            this.cells = cells;
        }
    }

    private static class BoundedListener extends AnalysisEventListener<Map<Integer, String>> {
        private Map<Integer, String> header = Collections.emptyMap();
        private final List<SheetRow> rows = new ArrayList<>();
        private boolean overflow;
        private boolean multipleSheets;

        @Override
        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
            if (context.readSheetHolder().getSheetNo() != 0) {
                multipleSheets = true;
                throw new IllegalArgumentException("模板只允许一个工作表");
            }
            header = new HashMap<>(headMap);
        }

        @Override
        public void invoke(Map<Integer, String> data, AnalysisContext context) {
            if (rows.size() >= LIMIT) {
                overflow = true;
                throw new IllegalArgumentException("最多导入5000行");
            }
            rows.add(new SheetRow(context.readRowHolder().getRowIndex() + 1, new HashMap<>(data)));
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {}
    }
}
