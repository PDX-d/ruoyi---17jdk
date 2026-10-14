package cn.iocoder.yudao.module.timetable;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.LessonQueryReqVO;
import cn.iocoder.yudao.module.timetable.controller.admin.vo.LessonSaveReqVO;
import cn.iocoder.yudao.module.timetable.service.export.TimetableExportService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import static org.junit.jupiter.api.Assertions.*;

class TimetableExportRegressionTest extends TimetableIntegrationTestSupport {
    @Autowired private TimetableExportService exports;

    @Test
    void generatesReadablePdfIncludingChineseAndCancelledStatus() throws Exception {
        Long id = lessons.create(lesson(9, 1L, room, 1L, 2L));
        jdbc.update("UPDATE timetable_lesson SET status = 1 WHERE id = ?", id);
        LessonQueryReqVO request = query();
        request.setFormat("pdf");
        MockHttpServletResponse response = new MockHttpServletResponse();
        exports.export(request, response);
        assertEquals("application/pdf", response.getContentType());
        assertEquals("no-store", response.getHeader("Cache-Control"));
        assertTrue(response.getHeader("Content-Disposition").endsWith("timetable.pdf"));
        try (PDDocument document = Loader.loadPDF(response.getContentAsByteArray())) {
            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("课程表"));
            assertTrue(text.contains("程序设计课程"));
            assertTrue(text.contains("教师甲"));
            assertTrue(text.contains("一班"));
            assertTrue(text.contains("二班"));
            assertTrue(text.contains("测试教室R1"));
            assertTrue(text.contains("已停课"));
        }
    }

    @Test
    void pdfPaginatesByWeekAndAllowsMoreThanSevenDays() throws Exception {
        LocalDate monday = day.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        LessonSaveReqVO first = lesson(9, 1L, room, 1L);
        first.setStartTime(monday.atTime(9, 0));
        first.setEndTime(monday.atTime(10, 0));
        lessons.create(first);
        LessonSaveReqVO second = lesson(9, 1L, room, 1L);
        second.setStartTime(monday.plusWeeks(1).atTime(9, 0));
        second.setEndTime(monday.plusWeeks(1).atTime(10, 0));
        lessons.create(second);
        LessonQueryReqVO request = query();
        request.setStartDate(monday);
        request.setEndDate(monday.plusDays(13));
        request.setFormat("pdf");
        MockHttpServletResponse response = new MockHttpServletResponse();
        exports.export(request, response);
        try (PDDocument document = Loader.loadPDF(response.getContentAsByteArray())) {
            assertEquals(2, document.getNumberOfPages());
            assertTrue(new PDFTextStripper().getText(document).contains("程序设计课程"));
        }
    }

    @Test
    void generatesSevenDayPngWithValidImageSignature() throws Exception {
        lessons.create(lesson(9, 1L, room, 1L));
        LessonQueryReqVO request = query();
        request.setFormat("png");
        MockHttpServletResponse response = new MockHttpServletResponse();
        exports.export(request, response);
        assertEquals("image/png", response.getContentType());
        byte[] bytes = response.getContentAsByteArray();
        assertArrayEquals(new byte[]{(byte) 137, 80, 78, 71, 13, 10, 26, 10},
                java.util.Arrays.copyOf(bytes, 8));
        assertNotNull(ImageIO.read(new ByteArrayInputStream(bytes)));
    }

    @Test
    void rejectsEightDayPngAndInvalidFormatsWithoutWritingFile() {
        LessonQueryReqVO request = query();
        request.setFormat("png");
        request.setEndDate(day.plusDays(7));
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServiceException error = assertThrows(ServiceException.class, () -> exports.export(request, response));
        assertTrue(error.getMessage().contains("PDF"));
        assertEquals(0, response.getContentAsByteArray().length);
        request.setFormat("jpeg");
        assertThrows(ServiceException.class, () -> exports.export(request, response));
    }

    @Test
    void realExportUsesSameIdentityScopeAsCalendarAndRejectsForeignClass() throws Exception {
        lessons.create(lesson(9, 1L, room, 1L));
        Long otherRoom = createRoom("R2");
        lessons.create(lesson(11, 2L, otherRoom, 2L));
        login(4L);
        LessonQueryReqVO request = query();
        request.setFormat("pdf");
        MockHttpServletResponse response = new MockHttpServletResponse();
        exports.export(request, response);
        try (PDDocument document = Loader.loadPDF(response.getContentAsByteArray())) {
            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("教师甲"));
            assertFalse(text.contains("教师乙"));
            assertFalse(text.contains("测试教室R2"));
        }
        request.setDimension("CLASS");
        request.setTargetId(2L);
        MockHttpServletResponse denied = new MockHttpServletResponse();
        assertThrows(ServiceException.class, () -> exports.export(request, denied));
        assertEquals(0, denied.getContentAsByteArray().length);
    }
}
