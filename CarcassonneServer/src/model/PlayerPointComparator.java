package model;

import java.util.Comparator;
import java.util.Map;

// A játék végén a pontok rendezéséhez a Map-ek összehasonlítását segítő osztály
public class PlayerPointComparator implements Comparator<Integer>{
        Map<Integer, Integer> map;

        public PlayerPointComparator(Map<Integer, Integer> map) {
            this.map = map;
        }

        // A Comparator osztály összehasonlításának felülírása
        @Override
        public int compare(Integer o1, Integer o2) {
            if (map.get(o1) >= map.get(o2)) {
                return 1;
            } else {
                return -1;
            }
        }
}
