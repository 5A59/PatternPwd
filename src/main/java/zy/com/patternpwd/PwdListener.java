package zy.com.patternpwd;

import java.util.List;

/**
 * Created by zy on 16-6-21.
 */
public interface PwdListener {
    void patternStart();
    void patternAdd(int point);
    void patternEnd(List<Integer> pattern);
}
