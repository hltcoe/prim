package edu.jhu.prim.vector;

import edu.jhu.prim.Primitives.MutableDouble;
import edu.jhu.prim.Primitives.MutableInt;
import edu.jhu.prim.util.Lambda.FnIntDoubleToVoid;

public abstract class AbstractIntDoubleVector {

    private static final long serialVersionUID = 1L;
    
    public abstract void iterate(FnIntDoubleToVoid function);
    
    public double getSum() {
        final MutableDouble sum = new MutableDouble(0);
        this.iterate(new FnIntDoubleToVoid() {
            public void call(int idx, double val) {
                sum.v += val;
            }
        });
        return sum.v;
    }

    public double getProd() {
        final MutableDouble prod = new MutableDouble(1);
        this.iterate(new FnIntDoubleToVoid() {
            public void call(int idx, double val) {
                prod.v *= val;
            }
        });
        return prod.v;
    }

    public double getMax() {
        final MutableDouble max = new MutableDouble(Double.NEGATIVE_INFINITY);
        this.iterate(new FnIntDoubleToVoid() {
            public void call(int idx, double val) {
                if (val > max.v) {
                    max.v = val;
                }
            }
        });
        return max.v;
    }
    
    public int getArgmax() {
        final MutableInt argmax = new MutableInt(-1);
        final MutableDouble max = new MutableDouble(Double.NEGATIVE_INFINITY);
        this.iterate(new FnIntDoubleToVoid() {
            public void call(int idx, double val) {
                if (val > max.v) {
                    argmax.v = idx;
                    max.v = val;
                }
            }
        });
        return argmax.v;
    }
    
    public double getInfNorm() {
        final MutableDouble maxAbs = new MutableDouble(Double.NEGATIVE_INFINITY);
        this.iterate(new FnIntDoubleToVoid() {
            public void call(int idx, double val) {
                double abs = Math.abs(val);
                if (abs > maxAbs.v) {
                    maxAbs.v = abs;
                }
            }
        });
        return maxAbs.v;
    }
    
}
