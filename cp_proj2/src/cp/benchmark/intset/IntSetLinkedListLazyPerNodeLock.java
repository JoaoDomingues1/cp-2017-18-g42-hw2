package cp.benchmark.intset;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cp.benchmark.intset.IntSetLinkedListOptimisticPerNodeLock.Node;

/**
 * @author Pascal Felber
 * @author Tiago Vale
 * @since 0.1
 */
public class IntSetLinkedListLazyPerNodeLock implements IntSet {

  public class Node {
	private final Lock lock;
    private final int m_value;
    private Node m_next;
    private boolean marked;

    public Node(int value, Node next) {
      m_value = value;
      m_next = next;
      lock = new ReentrantLock();
      marked = false;
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
    
    public void lock() {
    	lock.lock();
    }
    
    public void unlock() {
    	lock.unlock();
    }
    
    public void mark() {
    	marked = true;
    }
    
    public boolean getMarked() {
    	return marked;
    }
  }

  private final Node m_first;

  public IntSetLinkedListLazyPerNodeLock() {
    Node min = new Node(Integer.MIN_VALUE);
    Node max = new Node(Integer.MAX_VALUE);
    min.setNext(max);
    m_first = min;
  }

  public boolean add(int value) {
	  while(true) {
	    	Node previous = m_first;
	    	Node next = previous.getNext();
	    	while(next.getValue() < value) {
	    		previous = next;
	    		next = previous.getNext();
	    	}
	    	previous.lock();
	    	try {
	    		next.lock();
	    		try {
	    			if(auxValidate(previous, next)) {
	    				if(next.getValue()==value) {
	    					return false;
	    				} else {
	    					previous.setNext(new Node(value, next));
	    					return true;
	    				}
	    			}
	    		} finally {
	    			next.unlock();
	    		}
	    } finally {
	    	previous.unlock();
	    }
	  }
  }

  public boolean remove(int value) {
	  while(true) {
	    	Node previous = m_first;
	    	Node next = previous.getNext();
	    	while(next.getValue() < value) {
	    		previous = next;
	    		next = previous.getNext();
	    	}
	    	previous.lock();
	    	try {
	    		next.lock();
	    		try {
	    			if(auxValidate(previous, next)) {
	    				if(next.getValue()==value) {
	    					next.mark();
	    					previous.setNext(next.getNext());
	    					return true;
	    				} else {
	    					return false;
	    				}
	    			}
	    		} finally {
	    			next.unlock();
	    		}
	    } finally {
	    	previous.unlock();
	    }
	  }
    
  }

  public boolean contains(int value) {
    Node node = m_first;
    int v;
    while ((v = node.getValue()) < value)		
    	node = node.getNext();
    return node.getValue()==value && !node.getMarked();
  }
  
//funciona numa lista ordenada
  public boolean auxValidate(Node prev, Node next) {
	  return !prev.getMarked() && !next.getMarked() && prev.getNext().equals(next);
  }

  public void validate() {
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
