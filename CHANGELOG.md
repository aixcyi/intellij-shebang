# 更新日志／Changelog

## v2.0.0-SNAPSHOT

1. 将最低兼容版本提升到 2022.3  
   Upgrade minimum compatible version to 2022.3

## v1.5.0

1. 添加“码云”上的问题追踪链接。  
   Added issues tracker link which on Gitee.
2. 选择绝对路径插入 shebang 时，允许调整初始目录，以便快速选择。  
   Added an initial selection for Path Chooser that you can faster choose it.
3. 选择路径时允许过滤文件后缀。  
   Allow filter files by suffix while selecting path to insert Shebang.
4. 同时支持 “,” 和 “|” 作为文件后缀的分隔符。  
   Supports using both "," and "|" as delimiters when setting file suffix.

## v1.4.0

1. 修复不能在无后缀 Shell 脚本中显示菜单项的问题。  
   Fixed an issue where the menu could not be displayed in Shell scripts without suffix.
2. 将相对路径规范为类 Unix 路径格式。  
   Use Unix-like path format instead of systems when you insert a path base on project root.
3. 格式化（设置中的）文件后缀时保留原始顺序。  
   Keep input order when you reformat file suffixes on settings.

## v1.3.0

1. 兼容 2024.2 版本。  
   Compatible with version 2024.2 .
2. 修复部分情况下无法替换既有 Shebang 的问题。  
   Fixed an issue where sometimes existing Shebang could not be replaced.

## v1.2.0

1. 设置界面添加「还原为默认值」按钮。  
   Add a _"Restore to default"_ button to Shebang list in plugin setting.
2. 空白的或已有的 Shebang 不会添加到预设列表。  
   Addition will be stop if Shebang is existed or blank.
3. Shebang 预设列表添加置顶、置底、排序、编辑全表的按钮。  
   Add buttons _Move to top_, _Move to bottom_, _Sort_, _Edit list_ for Shebang list in plugin setting.
4. 允许格式化后缀表达式。  
   Support reformatting for file suffix expression in plugin setting.
5. 默认语言改为英语。  
   Change fallback nature language to English.

## v1.1.0

1. 插入 shebang 之前检测编辑器是否可写，并提示用户解除只读状态。  
   Hint user to clear read-only status if editor is not writeable while insert Shebang.

## v1.0.0

正式发布。  
Officially released.

_Built in 2024-07-21_
