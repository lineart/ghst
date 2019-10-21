package pro.cosy.ghst;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.Optional;

/**
 * Common used utils
 */
public class Utils {

    /**
     * Converts nullable Map Entries to MultiValueMap. Null valued entries are dropped
     */
    static public MultiValueMap<String, String> toMultiMap(Map<String, Optional<String>> map) {
        LinkedMultiValueMap<String, String> multiMap = new LinkedMultiValueMap<>();
        map.entrySet().stream()
                .filter(e -> e.getValue().isPresent())
                .forEach(e -> multiMap.add(e.getKey(), e.getValue().get()));

        return multiMap;
    }
}
