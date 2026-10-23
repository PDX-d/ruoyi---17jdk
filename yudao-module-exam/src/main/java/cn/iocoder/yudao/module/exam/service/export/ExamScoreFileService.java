package cn.iocoder.yudao.module.exam.service.export;

import cn.iocoder.yudao.module.exam.controller.admin.vo.ExamVO.*;
import cn.iocoder.yudao.module.exam.service.ExamScoreService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import static cn.iocoder.yudao.module.exam.support.ExamException.require;

@Service
public class ExamScoreFileService {
    @Resource private ExamScoreService scores;
    @Resource private ExamScoreWorkbook workbook;
    @Resource private ExamScorePdf pdf;

    public void template(Long sessionId, HttpServletResponse response) throws IOException {
        workbook.template(scores.importSnapshot(query(sessionId)), response);
    }

    public ImportResp importScores(MultipartFile file, Long sessionId, Long sessionVersion) {
        ScoreSnapshotResp snapshot = scores.importSnapshot(query(sessionId));
        ExamScoreWorkbook.ParsedImport parsed = workbook.parse(file, sessionId, sessionVersion, snapshot);
        ImportResp result = new ImportResp();
        result.setErrors(parsed.getErrors());
        if (parsed.getErrors().isEmpty()) {
            scores.importScores(parsed.getRequest());
            result.setSuccessCount(parsed.getRequest().getRows().size());
        }
        return result;
    }

    public void export(ExamQueryReq request, String format, HttpServletResponse response) throws IOException {
        require(request.getSessionId() != null || request.getCourseId() != null || request.getClassId() != null,
                "导出须指定考试、课程或班级");
        require("xlsx".equals(format) || "pdf".equals(format), "导出格式仅支持xlsx或pdf");
        ScoreSnapshotResp snapshot = scores.exportSnapshot(request);
        if ("pdf".equals(format)) {
            pdf.export(snapshot, response);
        } else {
            workbook.export(snapshot, response);
        }
    }

    private ExamQueryReq query(Long sessionId) {
        ExamQueryReq request = new ExamQueryReq();
        request.setSessionId(sessionId);
        return request;
    }
}
