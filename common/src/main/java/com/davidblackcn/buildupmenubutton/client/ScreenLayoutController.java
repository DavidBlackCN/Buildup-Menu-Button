package com.davidblackcn.buildupmenubutton.client;

import com.davidblackcn.buildupmenubutton.BuildupMenuButton;
import com.davidblackcn.buildupmenubutton.client.layout.DynamicButtonLayoutManager;
import com.davidblackcn.buildupmenubutton.client.layout.LayoutPlan;
import com.davidblackcn.buildupmenubutton.client.layout.Rect;
import com.davidblackcn.buildupmenubutton.client.profile.PauseScreenLayoutProfile;
import com.davidblackcn.buildupmenubutton.client.profile.ScreenLayoutProfile;
import com.davidblackcn.buildupmenubutton.client.profile.TitleScreenLayoutProfile;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

/**
 * 采集-规划-应用三阶段调度的入口，管理每个目标屏幕的布局状态。
 *
 * <p>只精确处理原生 {@link TitleScreen}/{@link PauseScreen}（不含子类）。刷新时机：初始化事件
 * 触发一次请求；正常帧仅做轻量指纹检查（成员集合、控件几何、屏幕尺寸），变化时才通过
 * {@link Minecraft#execute(Runnable)} 排到下一客户端任务执行布局，避免渲染中途改动几何。
 * 同一控件连续多帧被外部改写时停止管理并告警一次。</p>
 */
public final class ScreenLayoutController {

    private static final int DRIFT_LIMIT = 3;

    private final ScreenButtonCollector collector = new ScreenButtonCollector();
    private final DynamicButtonLayoutManager manager = new DynamicButtonLayoutManager();
    private final LayoutApplier applier = new LayoutApplier();
    private final TitleScreenLayoutProfile titleProfile = new TitleScreenLayoutProfile();
    private final PauseScreenLayoutProfile pauseProfile = new PauseScreenLayoutProfile();
    private final Map<Screen, LayoutState> states = new WeakHashMap<>();

    public boolean accepts(Screen screen) {
        return screen != null && (screen.getClass() == TitleScreen.class || screen.getClass() == PauseScreen.class);
    }

    /** 初始化后标记一次布局；具体写入由加载器在安全的界面阶段触发。 */
    public void requestLayout(Screen screen) {
        if (!accepts(screen)) {
            return;
        }
        LayoutState state = states.computeIfAbsent(screen, ignored -> new LayoutState());
        state.dirty = true;
        state.conflict = false;
        state.driftCount = 0;
        state.warnedConflict = false;
        state.warnedFailOpen = false;
    }

    /** 每帧/每 tick 的轻量指纹检查；检测到变化时请求重排。 */
    public void onFrame(Screen screen) {
        LayoutState state = states.get(screen);
        if (state == null || state.conflict) {
            return;
        }
        if (observe(screen, state)) {
            schedule(state, screen);
        }
    }

    /**
     * 在界面控件提取前同步应用待处理布局。Fabric 用此阶段保证首帧不显示原生临时布局，
     * 同时仍会检测第三方在上一帧修改过的控件几何。
     */
    public void applyBeforeExtract(Screen screen) {
        LayoutState state = states.get(screen);
        if (state == null || state.conflict) {
            return;
        }
        if (!state.dirty) {
            observe(screen, state);
        }
        if (state.dirty && !state.conflict) {
            runLayout(screen, state);
        }
    }

    private boolean observe(Screen screen, LayoutState state) {
        Fingerprint current = fingerprint(screen);
        Fingerprint last = state.lastFingerprint;
        if (last == null) {
            state.dirty = true;
            return true;
        }
        if (current.equals(last)) {
            state.dirty = false;
            return false;
        }
        if (current.widgets.keySet().equals(last.widgets.keySet())
                && current.width == last.width
                && current.height == last.height) {
            state.driftCount++;
            if (state.driftCount >= DRIFT_LIMIT) {
                state.conflict = true;
                state.dirty = false;
                if (!state.warnedConflict) {
                    BuildupMenuButton.LOGGER.warn("[{}] buttons were rewritten externally for {} consecutive frames; "
                            + "stopped managing this screen to avoid fighting", BuildupMenuButton.MOD_ID, DRIFT_LIMIT);
                    state.warnedConflict = true;
                }
                return false;
            }
            state.dirty = true;
        } else {
            state.driftCount = 0;
            state.dirty = true;
        }
        return true;
    }

    /** 屏幕关闭时清理状态。 */
    public void onScreenRemoved(Screen screen) {
        states.remove(screen);
    }

    private void runLayout(Screen screen, LayoutState state) {
        state.scheduled = false;
        if (state.conflict || !state.dirty) {
            return;
        }
        if (states.get(screen) != state) {
            state.dirty = false;
            return;
        }
        ScreenButtonCollector.CollectResult result = collector.collect(screen);
        ScreenLayoutProfile profile = screen.getClass() == TitleScreen.class ? titleProfile : pauseProfile;
        LayoutPlan plan = manager.plan(profile, result.snapshots(), screen.width, screen.height);
        if (plan.isFailedOpen() || !plan.isValid()) {
            state.lastFingerprint = fingerprint(screen);
            state.dirty = false;
            if (!state.warnedFailOpen) {
                BuildupMenuButton.LOGGER.warn("[{}] layout {}; screen left untouched",
                        BuildupMenuButton.MOD_ID, plan.failureReason() == null ? "plan invalid" : plan.failureReason());
                state.warnedFailOpen = true;
            }
            return;
        }
        if (applier.apply(result.widgets(), plan)) {
            state.lastFingerprint = fingerprint(screen);
            state.dirty = false;
        }
    }

    private void schedule(LayoutState state, Screen screen) {
        if (state.scheduled) {
            return;
        }
        state.scheduled = true;
        Minecraft.getInstance().execute(() -> runLayout(screen, state));
    }

    /** 当前被管理候选按钮的轻量指纹：屏幕尺寸 + 每个可见按钮的几何（按身份哈希去重）。 */
    private static Fingerprint fingerprint(Screen screen) {
        Map<Integer, Rect> widgets = new HashMap<>();
        for (Object listener : screen.children()) {
            if (listener instanceof Button button && !(button instanceof PlainTextButton) && button.visible) {
                widgets.put(
                        System.identityHashCode(button),
                        new Rect(button.getX(), button.getY(), button.getWidth(), button.getHeight()));
            }
        }
        return new Fingerprint(screen.width, screen.height, widgets);
    }

    private static final class Fingerprint {
        private final int width;
        private final int height;
        private final Map<Integer, Rect> widgets;

        private Fingerprint(int width, int height, Map<Integer, Rect> widgets) {
            this.width = width;
            this.height = height;
            this.widgets = widgets;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Fingerprint that)) {
                return false;
            }
            return width == that.width && height == that.height && widgets.equals(that.widgets);
        }

        @Override
        public int hashCode() {
            return 31 * (31 * width + height) + widgets.hashCode();
        }
    }

    private static final class LayoutState {
        private boolean dirty;
        private boolean scheduled;
        private boolean conflict;
        private int driftCount;
        private boolean warnedConflict;
        private boolean warnedFailOpen;
        private Fingerprint lastFingerprint;
    }
}
