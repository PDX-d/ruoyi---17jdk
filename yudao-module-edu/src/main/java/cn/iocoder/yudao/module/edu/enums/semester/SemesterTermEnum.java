package cn.iocoder.yudao.module.edu.enums.semester;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 学期枚举
 *
 * 1 - 第一学期；2 - 第二学期
 */
@Getter
@AllArgsConstructor
public enum SemesterTermEnum {

    FIRST(1, "第一学期"),
    SECOND(2, "第二学期");

    /**
     * 学期值
     */
    private final Integer term;
    /**
     * 学期名
     */
    private final String name;

    /**
     * 按学期值取学期名（用于学期名称自动拼装）
     *
     * @param term 学期值
     * @return 学期名；未匹配到时返回 null
     */
    public static String getName(Integer term) {
        if (term == null) {
            return null;
        }
        for (SemesterTermEnum item : values()) {
            if (item.getTerm().equals(term)) {
                return item.getName();
            }
        }
        return null;
    }

}
