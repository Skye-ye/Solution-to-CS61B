import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class FilteredList<T> implements Iterable<T>{
    List<T> list;
    Predicate<T> filter;

    public FilteredList(List<T> list, Predicate<T> filter) {
        this.list = list;
        this.filter = filter;
    }
    @Override
    public Iterator<T> iterator() {
        return new FilteredListIterator();
    }

    private class FilteredListIterator implements Iterator<T> {
        private int index = 0;
        @Override
        public boolean hasNext() {
            while (index < list.size() && !filter.test(list.get(index))) {
                index++;
            }
            return index < list.size();
        }
        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return list.get(index++);
        }
    }

    public static void main(String[] args) {
        List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Predicate<Integer> even = n -> n % 2 == 0;
        FilteredList<Integer> filteredList = new FilteredList<>(list, even);
        for (int n : filteredList) {
            System.out.println(n);
        }
    }
}
