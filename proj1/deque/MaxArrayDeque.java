package deque;

import java.util.Comparator;

/*为了代码的简洁性，不要copyArrayDeque的方法，而是使用继承：extends*/
public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> cmp;

    public MaxArrayDeque(Comparator<T> c) {
        // 父类的构造方法
        super();
        cmp = c;
    }

    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }
        int maxIndex = 0;
        for (int i = 1; i < size(); i++) {
            if (c.compare(get(i), get(maxIndex)) > 0) {
                maxIndex = i;
            }
        }
        return get(maxIndex);
    }

    public T max() {
        return max(cmp);
    }
}
