package xnetter.utils;

public final class KeyValue<K, V> {
    private K k;
    private V v;

    public KeyValue(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public void setKey(K k) {
        this.k = k;
    }

    public K getKey() {
        return this.k;
    }

    public void setValue(V v) {
        this.v = v;
    }

    public V getValue() {
        return this.v;
    }

    public boolean equals(KeyValue obj) {
        if (this == obj) {
            return true;
        }

        return this.k.equals(obj.getKey())
                && this.v.equals(obj.getValue());
    }

    public boolean equals(K k, V v) {
        return this.k.equals(k)
                && this.v.equals(v);
    }
}
