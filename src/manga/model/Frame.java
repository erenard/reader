package manga.model;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Frame extends Rectangle {
    private final String title;
    private final List<Frame> childs = new ArrayList<>();

    public Frame(String title, Rectangle rectangle) {
        super(rectangle);
        this.title = title;
    }
    
    public Frame(String title, int x, int y, int w, int h) {
        super(x, y, w, h);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public boolean add(Frame e) {
        return childs.add(e);
    }

    public List<Frame> getFrames() {
        return childs;
    }
}
