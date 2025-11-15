package com.jakubjirak.ansilog;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Service(Service.Level.APP)
@State(name = "AnsiLogSettingsState", storages = @Storage("ansi-log-viewer.xml"))
public final class AnsiLogSettingsState implements PersistentStateComponent<AnsiLogSettingsState.State> {
    public static class State { 
        public List<String> extensions = new ArrayList<>();
        public boolean hideAnsiCodes = true;
        public boolean showAnsiCodesOnCursor = true;
        public boolean darkTheme = true;
    }
    private State state = new State();

    public AnsiLogSettingsState() { if (state.extensions.isEmpty()) state.extensions.add("log"); }

    public static AnsiLogSettingsState getInstance() { return com.intellij.openapi.application.ApplicationManager.getApplication().getService(AnsiLogSettingsState.class); }

    public List<String> getExtensions() { return state.extensions; }

    public void setExtensions(List<String> exts) { state.extensions = new ArrayList<>(exts); }
    
    public boolean isHideAnsiCodes() { return state.hideAnsiCodes; }
    
    public void setHideAnsiCodes(boolean hide) { state.hideAnsiCodes = hide; }
    
    public boolean isShowAnsiCodesOnCursor() { return state.showAnsiCodesOnCursor; }
    
    public void setShowAnsiCodesOnCursor(boolean show) { state.showAnsiCodesOnCursor = show; }
    
    public boolean isDarkTheme() { return state.darkTheme; }
    
    public void setDarkTheme(boolean dark) { state.darkTheme = dark; }

    @Override public @Nullable State getState() { return state; }
    @Override public void loadState(@NotNull State state) { this.state = state; if (this.state.extensions.isEmpty()) this.state.extensions.add("log"); }
}
