package com.davidblackcn.buildupmenubutton.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Registers this mod's live configuration screen with Mod Menu. */
public final class BuildupMenuButtonModMenuApi implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return BuildupMenuButtonConfigScreen::new;
    }
}
