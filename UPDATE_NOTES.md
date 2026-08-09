# UPDATE_NOTES

## 1.0.0 (2026-08-09)

- ✨ feat(mod): 恢复 Minecraft 26.2 主菜单与暂停菜单的旧版按钮布局，仅处理精确 `TitleScreen`/`PauseScreen`，不影响其他 Screen
- ✨ feat(layout): 新增动态布局引擎，按屏幕尺寸、按钮数量与尺寸计算核心区/辅助区/扩展区；右侧小按钮列在高度超限时自动换列（flex-wrap），支持间距→列数→收窄→保留原位→fail-open 的降级链路
- ✨ feat(layout): 只改写按钮几何，保留原生按钮实例、回调、Tooltip、active/visible、焦点与旁白行为
- ♻️ refactor(deps): 移除 Architectury API 运行时依赖，改用 Fabric ScreenEvents 与 NeoForge ScreenEvent 薄适配器；元数据统一为客户端专用
- 🧪 test(layout): 接入纯 Java 布局测试（WrapGridLayout、动态布局管理器、布局不变量）到 `check`，无需启动 Minecraft
- 🐛 fix(title-screen): 将主菜单主体恢复到原生垂直锚点，并消除语言、无障碍、好友等紧凑按钮在最大化窗口时贴近 Logo 或落入右下角竖列的问题
- 🐛 fix(layout): 按经典菜单锚点放置主菜单好友、语言与无障碍按钮，并让暂停菜单右侧图标列从“回到游戏”按钮顶部开始对齐
- 🐛 fix(modmenu): 适配 Mod Menu 26.2 的标题与暂停菜单按钮样式，自动布局图标、插入、替换、并排及经典大按钮模式
- 🐛 fix(title-screen): 主菜单所有紧凑按钮改为优先贴齐最底部按钮行、左右扩展并在空间不足时逐行向上排列；Mod Menu 图标位于好友按钮左侧
- 🐛 fix(title-screen): 标题页等待第三方按钮注册完成后再首次重排；紧凑按钮改为每个主体行左右各占一个槽位，满行后逐步向上排列
- 🐛 fix(fabric): 标题页布局改在首帧绘制前同步应用，避免首次启动、窗口缩放或返回主菜单时短暂显示原生布局
- 🐛 fix(fabric): 窗口缩放或第三方重初始化屏幕后重新绑定屏幕事件，防止布局监听被 Fabric 重置后失效
- 🐛 fix(title-screen): 小窗口高度下 Mod Menu 并排模式自动压缩经典大间距，避免主体菜单无法放置而回退为原版布局
- 🐛 fix(pause-screen): 暂停菜单主体按实际按钮行数垂直居中；原生四个小图标优先排在左侧，额外模组图标再按左列、右列顶部顺序自适应放置
- 🐛 fix(pause-screen): 暂停菜单保持原版风格的 1/4 高度基准，并兼容没有翻译键的第三方小按钮，避免打开暂停菜单崩溃
- 🐛 fix(realms): 让主菜单 Realms 的未读、新闻和邀请提示图标跟随重排后的 Realms 按钮，修复 Fabric 小窗口与 NeoForge 下的错位
