package manga.worker;

import java.util.List;
import manga.model.*;

public interface FrameFinder {
    List<Frame> getFrames(Page page);
}
