package com.trilogy.app.crm.account.foo;

import java.net.URLConnection;

public class GettersConstructorOnly {

    private int intValue;
    private boolean booleanValue;
    private long longValue;
    private double doubleValue;
    private int someValue;
    private Object object;
    private int account;
    private URLConnection context;
    private String statusName;

    public GettersConstructorOnly(int intValue, boolean booleanValue, long longValue,
            double doubleValue, int someValueInput, Object object, URLConnection context, String statValue) {
        this.intValue = intValue;
        this.booleanValue = booleanValue;
        this.longValue = longValue;
        this.doubleValue = doubleValue;
        this.someValue = someValueInput;
        this.object = object;
        this.context = context;
        this.statusName = statValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public boolean isBooleanValue() {
        return booleanValue;
    }

    public long getLongValue() {
        return longValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public int getSomeValue() {
        return someValue;
    }

    public Object getObject() throws NullPointerException {
        return object;
    }

    public int getAccount() { return account; }

    public URLConnection getContext() { return this.context; }

    public String statusName() {
        return statusName;
    }

    private static final int privStatFinInt;

    static {
        privStatFinInt = 33;
    }

    public int getPrivStatFinInt() {
        return privStatFinInt;
    }

    public class FirstInner {
        private final int foo = 54;

        public int getFoo() {
            return foo;
        }

        double getMaximum() {
            return Integer.MIN_VALUE;
        }
    }

    class SecondInner {
        private class InsideSecondPrivate {
            public int getInt() {
                return 8;
            }

            class OtherClass {
                private int methodOne() {
                    return 7;
                }
            }
        }

        class OtherInsideSecond {
            private OtherInsideSecond() {}

            int methodOne() {
                return 9;
            }
        }

        class WithNumberInsideSecond {
            int x;
            WithNumberInsideSecond(int n) {
                x = n;
            }

            int getX() {
                return x;
            }

            double getMax() {
                return Double.MAX_VALUE;
            }
        }
    }

    private class ThirdInner {
        class InsideThirdInner {
            double getDouble() {
                return 3.4;
            }
        }
    }

    class FourthInner {
        private int x;

        FourthInner(int x) {
            this.x = x;
        }

        class InnerInner {
            public int getX() {
                return x;
            }
        }
    }
}
