package com.example.foodnote;

/*
 *  A singleton pattern class that remembers the current user action
 */
public class ActionStateSingleton {
    private static ActionStateSingleton instance = null;

    public enum EditorAction {
        None, Create, Edit
    }
    private EditorAction editorAction;
    private long viewId;
    private String appEngineUserId;
    private String viewAuthorId;
    private int browser;

    private ActionStateSingleton() {
        this.editorAction = EditorAction.None;
    }

    public static synchronized ActionStateSingleton getInstance() {
        if (instance == null) {
            instance = new ActionStateSingleton();
        }
        return instance;
    }

    public void setEditorAction(EditorAction editorAction) {
        this.editorAction = editorAction;
    }

    public EditorAction getEditorAction() {
        return editorAction;
    }

    public void setViewId(long id) {
        viewId = id;
    }

    public long getViewId() {
        return viewId;
    }

    public void setAppEngineUserId(String id) {
        this.appEngineUserId = id;
    }

    public String getAppEngineUserId() {
        return appEngineUserId;
    }

    public void setViewAuthorId(String id) {
        this.viewAuthorId = id;
    }

    public String getViewAuthorId() {
        return viewAuthorId;
    }

    public void setBrowser(int browser) {
        this.browser = browser;
    }

    public int getBrowser() {
        return browser;
    }
}