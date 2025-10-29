package nl.han.ica.datastructures;

import java.util.LinkedList;

public class HANStack<T> implements IHANStack<T> {
    private LinkedList<T> stack  = new LinkedList<T>();

    @Override
    public void push(T t) {
        stack.addFirst(t);
    }

    @Override
    public T pop() {
       return stack.removeFirst();
    }

    @Override
    public T peek() {
        return stack.getFirst();
    }
}
