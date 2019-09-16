package com.netease.nim.uikit.business.robot.parser.elements.base;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>
 * 元素，只有属性，不支持嵌套
 */

public abstract class Element {
    public abstract void parse(JSONObject jsonObject) throws JSONException;
}
