package cp.benchmark.intset;

/**
 * @author Pascal Felber
 * @author Tiago Vale
 * @since 0.1
 */

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class IntSetLinkedListLockFree implements IntSet {
	
	
	
  public class Node {
    private final int m_value;
    private AtomicMarkableReference<Node> m_next;

    public Node(int value, AtomicMarkableReference<Node> next) {
      m_value = value;
      m_next = next;
    }

    public Node(int value) {
      this(value, new AtomicMarkableReference<Node>(null, false));
    }

    public int getValue() {
      return m_value;
    }

    public void setNext(AtomicMarkableReference<Node> next) {
      m_next = next;
    }

    public AtomicMarkableReference<Node> getNext() {
      return m_next;
    }
  }
  
  
  
  private final Node m_first;
  
  public IntSetLinkedListLockFree() {
    Node min = new Node(Integer.MIN_VALUE);
    Node max = new Node(Integer.MAX_VALUE);
    min.setNext(new AtomicMarkableReference<Node>(max,false));
    m_first = min;
  }
  
  class Window {
	  
		public Node previous; 
		public Node current;
		
		public Window(Node previous, Node current) {
			this.previous = previous;
			this.current = current;
		}
  }
		
  public Window find(Node head, int value) {
			Node prev = null;
			Node curr = null;
			Node next = null;
			boolean[] marked = {false};
			boolean snip;
			retry : while(true) {
				prev = head;
				curr = prev.getNext().getReference();
				while(true) {
					next = curr.getNext().get(marked);
					while(marked[0]) {
						snip = prev.getNext().compareAndSet(curr, next, false, false);
						if(!snip) continue retry;
						curr = next;
						next = curr.getNext().get(marked);
					}
					if(curr.getValue() >= value)
						return new Window(prev, curr);
					prev = curr;
					curr = next;
					
				}
			}
	}
  
  public boolean add(int value) {
	while(true) {
		Window window = find(m_first, value);
		Node previous = window.previous;
		Node current = window.current;
		if(current.getValue()==value) {
			return false;
		} else {
			Node node = new Node(value);
			node.setNext(new AtomicMarkableReference<Node>(current, false));
			if(previous.getNext().compareAndSet(current, node, false, false)) {
				return true;
			}
		}
	}
  }

  public boolean remove(int value) {
    boolean snip;
    while(true) {
    	Window window = find(m_first, value);
		Node previous = window.previous;
		Node current = window.current;
		if(current.getValue()!=value) {
			return false;
		} else {
			Node next = current.getNext().getReference();
			snip = current.getNext().attemptMark(next, true);
			if(!snip) continue;
			previous.getNext().compareAndSet(current, next, false, false);
			return true;
		}
    }
  }

  public boolean contains(int value) {
    boolean[] marked = {false};
    Node current = m_first;
    while(current.getValue() < value) {
    	current = current.getNext().getReference();
    	Node next = current.getNext().get(marked);
    }
    return (current.getValue()==value && !marked[0]);
  }

  public void validate() {
    java.util.Set<Integer> checker = new java.util.HashSet<>();
    int previous_value = m_first.getValue();
    Node node = m_first.getNext().getReference();
    int value = node.getValue();
    while (value < Integer.MAX_VALUE) {
      assert previous_value < value : "list is unordered: " + previous_value + " before " + value;
      assert !checker.contains(value) : "list has duplicates: " + value;
      checker.add(value);
      previous_value = value;
      node = node.getNext().getReference();
      value = node.getValue();
    }
  }
}
