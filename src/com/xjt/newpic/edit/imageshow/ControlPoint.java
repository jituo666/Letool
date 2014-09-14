package com.xjt.newpic.edit.imageshow;

public class ControlPoint implements Comparable {
    public float x;
    public float y;

    public ControlPoint(float px, float py) {
        x = px;
        y = py;
    }

    public ControlPoint(ControlPoint point) {
        x = point.x;
        y = point.y;
    }

    public boolean sameValues(ControlPoint other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }

        if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x)) {
            return false;
        }
        if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y)) {
            return false;
        }
        return true;
    }

    public ControlPoint copy() {
        return new ControlPoint(x, y);
    }

    @Override
    public int compareTo(Object another) {
        ControlPoint p = (ControlPoint) another;
        if (p.x < x) {
            return 1;
        } else if (p.x > x) {
            return -1;
        }
        return 0;
    }
}
