package com.threathunter.variable;

/**
 * created by www.threathunter.cn
 */
public enum WindowType {
    // aggregate type, sliding is for real realtime
    TIME_SLIDING,
    TIME_SLOT,
    // count of a group data to aggregate
    Length_SLIDING,
    Length_SLOT
}
