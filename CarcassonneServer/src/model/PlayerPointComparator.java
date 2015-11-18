package model;

import java.util.Comparator;
import java.util.Map;

public class PlayerPointComparator implements Comparator<Integer>{
        Map<Integer, Integer> map;

        public PlayerPointComparator(Map<Integer, Integer> map) {
            this.map = map;
        }

        @Override
        public int compare(Integer o1, Integer o2) {
            if (map.get(o1) >= map.get(o2)) {
                return 1;
            } else {
                return -1;
            }
        }
}
