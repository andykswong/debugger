/** Linked list of integer
 * @author ffleurey
 * @version 0.1
 */

package com.jpmorgan.tracer;

public class IntegerList {

	/** the number of elements of the list
	 */    
	protected int count = 0;
	/** The first cell of the list
	 */    
	protected Cell front = null;
	/** The last cell of the list
	 */    
	protected Cell back = null;
	/** The current cell
	 */    
	protected Cell cursor = null;

	/** Creates new IntegerList */
	public IntegerList() {
		count = 0;
		front = null;
	}

	/** get a string representing the list :
	 * <CODE>
	 * EX : 3,5,8,9,6,45.
	 * </CODE>
	 * @return A string representing the list
	 */    
	public String toString() {

		String result = "";
		Cell c = front;
		while(c!=null) {
			if (c != back) result += c.value + ", ";
			else result += c.value + ".";
			c = c.succ;
		}
		return result;
	}

	/** add an element at the begining of the list
	 * @param x the int to add to the list
	 * @post count == count@pre + 1 //increment count
	 */    
	public void pushFront(int x) {
		Cell n = new Cell(x);
		n.prec = null;
		n.succ = front;
		if (front != null) front.prec = n;
		else back = n;
		front = n;
		count++;
	}

	/** Add an element at the end of the list
	 * @param x the integer to add
	 * @post count == count@pre + 1 //increment count
	 */    
	public void pushBack(int x) {
		Cell n = new Cell(x);
		n.prec = back;
		n.succ = null;
		if (back != null) back.succ = n;
		else front = n;
		back = n;
		count++;
	}

	/** get and remove the first element of the list
	 * @return the first element of the list
	 * @throws Exception if the list is empty
	 * @pre count > 0 //pop on an empty list
	 * @post count == count@pre - 1 // Element not removed
	 */    
	public int popFront() throws Exception {

		if (front == null) throw new Exception("Pop on an empty list"); 

		int result = front.value;
		front = front.succ;
		if (front != null) front.prec = null;
		count--;
		return result;
	}

	/** get and remove the last element of the list
	 * @return the last element of the list
	 * @throws Exception if the list is empty
	 * @pre count > 0 //pop on an empty list
	 * @post count == count@pre - 1 // Element not removed
	 */    
	public int popBack() throws Exception {
		if (back == null) throw new Exception("Pop on an empty list"); 

		int result = back.value;
		back = back.prec;
		if (back != null) back.succ = null;
		count--;
		return result;
	}

	/** Place the cursor under the first element of the list
	 * @post cursor == front // cursor not set to front
	 */    
	public void start() {
		cursor = front;
	}

	/** get the int under the cursor
	 * @return get the int under the cursor
	 * @throws Exception if the cursor is off
	 * @pre !off()
	 * @post cursor.value == return // wrong value
	 */    
	public int current() throws Exception {
		if (off()) throw new Exception("No element under list cursor");
		return cursor.value;
	}

	/** move the cursor to the next element
	 * @return does the list has more elements
	 * @post off() implies !return // bad return value
	 * @post !off() implies return && cursor = cursor@pre.next //rate
	 */    
	public boolean next() {
		if (cursor==null) return false;
		cursor = cursor.succ;
		if (cursor == null) return false;
		return true;
	}

	/** is the cursor on an element
	 * @return is the cursor on an element
	 * @post cursor == null implies !return // wrong result
	 */    
	public boolean off() {
		return cursor == null;
	}

	/** Does the list contains the integer given as paramenter
	 * @param v the value to search
	 * @return is the integer in the list
	 */    
	public boolean contains(int v) {
		Cell c = front;
		while(c!=null) {
			if (c.value == v) return true;
			c = c.succ;
		}
		return false;
	}

	/** get the size of the list
	 * @return the size of the list
	 * @post count == result // wrong result
	 */    
	public int size() {
		return count;
	}

	/** Get a list containing the same elements sorted
	 * @return the sorted list
	 */    
	protected IntegerList getSortedList() {
		IntegerList Result = new IntegerList();
		Cell c = front;
		while(c!=null) {
			Result.pushSorted(c.value);
		}
		return Result;
	}

	/** Add an element sorted in the list
	 * The list must be already sorted
	 * @param v the integer to add
	 * @post size() == size()@pre // wrong list size
	 */    
	public void pushSorted(int v) {
		Cell n = new Cell(v);
		Cell c = front;
		while(c!=null) {
			if (c.value>=v) {
				n.succ = c;
				n.prec = c.prec;
				c.prec = n;
				if (c.prec != null) c.prec.succ = n;
				break;
			}
			c = c.succ;
		}
		if (c == null) pushBack(v);
	}
}

class Cell {
	/** the value of the cell
	 */    
	public int value;
	/** the next cell or null if last element
	 */    
	public Cell succ = null;
	/** the prec element or null if first
	 */    
	public Cell prec = null;

	/** make a new cell
	 * @param v value for the new cell
	 */    
	public Cell(int v) {
		value = v;
	}
}   