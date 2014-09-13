package com.xjt.newpic.filtershow.editors;

import com.xjt.newpic.filtershow.EditorPlaceHolder;
import com.xjt.newpic.filtershow.editors.BasicEditor;
import com.xjt.newpic.filtershow.editors.EditorCurves;
import com.xjt.newpic.filtershow.editors.EditorZoom;

public class EditorManager {

    public static void addEditors(EditorPlaceHolder editorPlaceHolder) {
        editorPlaceHolder.addEditor(new EditorGrad());
        editorPlaceHolder.addEditor(new EditorChanSat());
        editorPlaceHolder.addEditor(new EditorZoom());
        editorPlaceHolder.addEditor(new EditorCurves());
        editorPlaceHolder.addEditor(new EditorTinyPlanet());
        editorPlaceHolder.addEditor(new EditorDraw());
        editorPlaceHolder.addEditor(new EditorVignette());
        editorPlaceHolder.addEditor(new EditorColorBorder());
        editorPlaceHolder.addEditor(new EditorMirror());
        editorPlaceHolder.addEditor(new EditorRotate());
        editorPlaceHolder.addEditor(new EditorStraighten());
        editorPlaceHolder.addEditor(new EditorCrop());
    }

}
