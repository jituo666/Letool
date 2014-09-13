package com.xjt.newpic.edit.controller;

import android.view.View;
import android.view.ViewGroup;

import com.xjt.newpic.edit.editors.Editor;

public interface Control {
    public void setUp(ViewGroup container, Parameter parameter, Editor editor);

    public View getTopView();

    public void setPrameter(Parameter parameter);

    public void updateUI();
}
