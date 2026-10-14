INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, visible, keep_alive, always_show)
SELECT '课程表管理权限', '', 1, 80, 0, 'timetable-permissions', '', '', 0, b'0', b'0', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE path = 'timetable-permissions' AND type = 1 AND deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, visible, keep_alive, always_show)
SELECT p.name, p.permission, 3, p.sort, root.id, '', '', '', 0, b'0', b'0', b'0'
FROM (
  SELECT '课程表管理员' name, 'timetable:admin' permission, 1 sort
  UNION ALL SELECT '教室查询', 'timetable:classroom:query', 2
  UNION ALL SELECT '教室创建', 'timetable:classroom:create', 3
  UNION ALL SELECT '教室修改', 'timetable:classroom:update', 4
  UNION ALL SELECT '教室删除', 'timetable:classroom:delete', 5
  UNION ALL SELECT '课次查询及冲突预检', 'timetable:lesson:query', 6
  UNION ALL SELECT '课次创建及批量排课', 'timetable:lesson:create', 7
  UNION ALL SELECT '课次修改', 'timetable:lesson:update', 8
  UNION ALL SELECT '课次删除', 'timetable:lesson:delete', 9
  UNION ALL SELECT '课程表导出', 'timetable:lesson:export', 10
  UNION ALL SELECT '调停课申请', 'timetable:change-request:create', 11
  UNION ALL SELECT '调停课申请查询', 'timetable:change-request:query', 12
  UNION ALL SELECT '调停课审批通过', 'timetable:change-request:approve', 13
  UNION ALL SELECT '调停课审批拒绝', 'timetable:change-request:reject', 14
) p
JOIN (SELECT MIN(id) id FROM system_menu WHERE path = 'timetable-permissions' AND type = 1 AND deleted = b'0') root
WHERE root.id IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM system_menu m WHERE m.permission = p.permission AND m.deleted = b'0');

INSERT INTO system_notify_template (name, code, type, nickname, content, params, status, remark)
SELECT '课表调停课通知', 'timetable_lesson_changed', 1, '课程表管理',
       '课程：{course}；变更：{change}；上课时间：{time}；教室：{classroom}。请及时查看课表。',
       '["course","change","time","classroom"]', 0, ''
WHERE NOT EXISTS (SELECT 1 FROM system_notify_template WHERE code = 'timetable_lesson_changed' AND deleted = b'0');
