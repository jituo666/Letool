package com.xjt.newpic.edit.controller;

public interface ParameterActionAndInt extends ParameterInteger {
    public static String sParameterType = "ParameterActionAndInt";

    public void fireLeftAction();

    public int getLeftIcon();

    public void fireRightAction();

    public int getRightIcon();
}
