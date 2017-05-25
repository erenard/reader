package manga.model;

public interface FrameIterator<T> {
    T next();
    T previous();
    T jumpTo(int index);
}
