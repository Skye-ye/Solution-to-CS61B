public class SLList {
  Node sentinel;

  Public SLList() {
    this.sentinel = new Node();
  }

  private static class Node {
    int item;
    Node next;
  }

  public int findFrist(int n) {
    return findFirstHelper(n, 0, this.sentinel.next);
  }

  private int findFirstHelper(int n, int index, Node curr) {
    if (curr == null) {
      return -1;
    }
    if (curr.item == n) {
      return index;
    } else {
      return findFirstHelper(n, index + 1, curr.next);
    }
  }
}
