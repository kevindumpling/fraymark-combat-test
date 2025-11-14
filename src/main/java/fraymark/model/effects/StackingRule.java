package fraymark.model.effects;

public enum StackingRule {
    REFRESH_DURATION,   // keep stronger/current value, refresh timer
    STACK_ADD,          // sum magnitudes (cap elsewhere)
    REPLACE_IF_STRONGER // overwrite only if new is stronger
}
