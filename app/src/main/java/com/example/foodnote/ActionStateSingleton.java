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
}