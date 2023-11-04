package deque;

import java.util.Comparator;

/*为了代码的简洁性，不要copyArrayDeque的方法，而是使用继承：extends*/
public class MaxArrayDeque<T> extends ArrayDeque<T>{
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
        T maxItem = this.get(0);
        for (T i : this.getItems()) {
            if (c.compare(i, maxItem) > 0) {
                maxItem = i;
            }
        }
        return maxItem;
    }
}
