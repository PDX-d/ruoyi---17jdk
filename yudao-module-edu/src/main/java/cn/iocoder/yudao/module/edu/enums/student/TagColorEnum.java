package cn.iocoder.yudao.module.edu.enums.student;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 学生标签颜色枚举（固定 16 色）
 *
 * 颜色由「自由输入 hex」改为「固定 16 色单选」，后端统一用本枚举校验。
 * 如需扩充色板，在此追加枚举项，并同步前端 COLOR_OPTIONS，不要放开自由输入。
 */
@Getter
@AllArgsConstructor
public enum TagColorEnum {

    BLUE("#409EFF", "蓝色"),
    GREEN("#67C23A", "绿色"),
    ORANGE("#E6A23C", "橙色"),
    RED("#F56C6C", "红色"),
    GRAY("#909399", "灰色"),
    CYAN("#00BCD4", "青色"),
    INDIGO("#3F51B5", "靛蓝"),
    PURPLE("#9C27B0", "紫色"),
    ROSE("#E91E63", "玫红"),
    DEEP_ORANGE("#FF9800", "深橙"),
    BROWN("#795548", "棕色"),
    BLUE_GRAY("#607D8B", "蓝灰"),
    DEEP_GREEN("#4CAF50", "深绿"),
    AMBER("#FFC107", "琥珀"),
    DEEP_PURPLE("#673AB7", "深紫"),
    TEAL("#009688", "蓝绿"),
    ;

    /**
     * 颜色值（十六进制）
     */
    private final String color;
    /**
     * 颜色名称（前端色板提示用）
     */
    private final String name;

    /**
     * 是否为合法的标签颜色（固定 16 色之一）
     *
     * @param color 颜色值
     * @return 是否合法
     */
    public static boolean isValid(String color) {
        if (StrUtil.isBlank(color)) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(item -> item.getColor().equalsIgnoreCase(color.trim()));
    }

}
