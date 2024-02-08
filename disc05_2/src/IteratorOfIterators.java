import java.util.*;
public class IteratorOfIterators implements Iterator<Integer>, Iterable<Integer> {

    private final LinkedList<Iterator<Integer>> iterators;
    public IteratorOfIterators(Iterator<Iterator<Integer>> a) {
        iterators = new LinkedList<>();
        while (a.hasNext()) {
            Iterator<Integer> iterator = a.next();
            if (iterator.hasNext()) {
                iterators.add(iterator);
            }
        }
    }

    @Override
    public boolean hasNext() {
        return !iterators.isEmpty();
    }

    @Override
    public Integer next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Iterator<Integer> iterator = iterators.removeFirst();
        int ans = iterator.next();
        if (iterator.hasNext()) {
            iterators.addLast(iterator);
        }
        return ans;
    }

    @Override
    public Iterator<Integer> iterator() {
        return this;
    }

    public static void main(String[] args) {
        List<Integer> list1 = List.of(1, 2, 3, 4, 5);
        List<Integer> list2 = List.of(6, 7, 8);
        List<Integer> list3 = List.of(11, 12, 13, 14, 15);
        Iterator<Integer> it1 = list1.iterator();
        Iterator<Integer> it2 = list2.iterator();
        Iterator<Integer> it3 = list3.iterator();
        Iterator<Iterator<Integer>> it = List.of(it1, it2, it3).iterator();
        IteratorOfIterators iteratorOfIterators = new IteratorOfIterators(it);
        for (int n : iteratorOfIterators) {
            System.out.println(n);
        }
    }
}
