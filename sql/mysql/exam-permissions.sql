INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, visible, keep_alive, always_show)
SELECT '考务管理权限', '', 1, 81, 0, 'exam-permissions', '', '', 0, b'0', b'0', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE path = 'exam-permissions' AND type = 1 AND deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, visible, keep_alive, always_show)
SELECT p.name, p.permission, 3, p.sort, root.id, '', '', '', 0, b'0', b'0', b'0'
FROM (
  SELECT '考务管理员' name, 'exam:admin' permission, 1 sort
  UNION ALL SELECT '公共数据查询', 'exam:reference:query', 2
  UNION ALL SELECT '考场创建', 'exam:venue:create', 3
  UNION ALL SELECT '考场修改', 'exam:venue:update', 4
  UNION ALL SELECT '考场删除', 'exam:venue:delete', 5
  UNION ALL SELECT '考场查询', 'exam:venue:query', 6
  UNION ALL SELECT '考试创建', 'exam:session:create', 7
  UNION ALL SELECT '考试修改', 'exam:session:update', 8
  UNION ALL SELECT '考试删除', 'exam:session:delete', 9
  UNION ALL SELECT '考试查询', 'exam:session:query', 10
  UNION ALL SELECT '成绩查询', 'exam:score:query', 11
  UNION ALL SELECT '成绩录入', 'exam:score:update', 12
  UNION ALL SELECT '成绩分项配置', 'exam:score:config', 13
  UNION ALL SELECT '成绩导入', 'exam:score:import', 14
  UNION ALL SELECT '成绩导出', 'exam:score:export', 15
  UNION ALL SELECT '成绩统计', 'exam:statistics:query', 16
  UNION ALL SELECT '本人定位签到', 'exam:check-in:create', 17
  UNION ALL SELECT '本人考试查询', 'exam:my-exam:query', 18
) p
JOIN (SELECT MIN(id) id FROM system_menu WHERE path = 'exam-permissions' AND type = 1 AND deleted = b'0') root
WHERE root.id IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM system_menu m WHERE m.permission = p.permission AND m.deleted = b'0');

