package cp.benchmark.intset;

/**
 * @author Pascal Felber
 * @author Tiago Vale
 * @since 0.1
 */
public class IntSetLinkedListSynchronized implements IntSet {

  public class Node {
    private final int m_value;
    private Node m_next;

    public Node(int value, Node next) {
      m_value = value;
      m_next = next;
    }

    public Node(int value) {
      this(value, null);
    }

    public int getValue() {
      return m_value;
    }

    public void setNext(Node next) {
      m_next = next;
    }

    public Node getNext() {
      return m_next;
    }
  }

  private final Node m_first;

  /** statements
  synchronized(this) 
  {
	  
  }
  */
  
  public IntSetLinkedListSynchronized() {
    Node min = new Node(Integer.MIN_VALUE);
    Node max = new Node(Integer.MAX_VALUE);
    min.setNext(max);
    m_first = min;
  }

  public synchronized boolean add(int value) {
    boolean result;

    Node previous = m_first;
    Node next = previous.getNext();
    int v;
    while ((v = next.getValue()) < value) {
      previous = next;
      next = previous.getNext();
    }
    result = v != value;
    if (result) {
      previous.setNext(new Node(value, next));
    }

    return result;
  }

  public synchronized boolean remove(int value) {
    boolean result;

    Node previous = m_first;
    Node next = previous.getNext();
    int v;
    while ((v = next.getValue()) < value) {
      previous = next;
      next = previous.getNext();
    }
    result = v == value;
    if (result) {
      previous.setNext(next.getNext());
    }

    return result;
  }

  public synchronized boolean contains(int value) {
    boolean result;

    Node previous = m_first;
    Node next = previous.getNext();
    int v;
    while ((v = next.getValue()) < value) {
      previous = next;
      next = previous.getNext();
    }
    result = (v == value);

    return result;
  }

  public synchronized void validate() {
    java.util.Set<Integer> checker = new java.util.HashSet<>();
    int previous_value = m_first.getValue();
    Node node = m_first.getNext();
    int value = node.getValue();
    while (value < Integer.MAX_VALUE) {
      assert previous_value < value : "list is unordered: " + previous_value + " before " + value;
      assert !checker.contains(value) : "list has duplicates: " + value;
      checker.add(value);
      previous_value = value;
      node = node.getNext();
      value = node.getValue();
    }
  }
}
