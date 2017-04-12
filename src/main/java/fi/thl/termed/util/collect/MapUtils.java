package fi.thl.termed.util.collect;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public final class MapUtils {

  private MapUtils() {
  }

  public static <K, V> Map<K, V> nullToEmpty(Map<K, V> map) {
    return map == null ? Collections.emptyMap() : map;
  }

  public static <K, V> Map.Entry<K, V> entry(K key, V value) {
    return new AbstractMap.SimpleEntry<>(key, value);
  }

  @SafeVarargs
  public static <K, V> Map<K, V> newLinkedHashMap(Map.Entry<K, V>... entries) {
    return newLinkedHashMap(Arrays.asList(entries));
  }

  public static <K, V> Map<K, V> newLinkedHashMap(Iterable<Map.Entry<K, V>> entries) {
    return putEntries(Maps.newLinkedHashMap(), entries);
  }

  @SafeVarargs
  public static <K, V> Map<K, V> putEntries(Map<K, V> map, Map.Entry<K, V>... entries) {
    return putEntries(map, Arrays.asList(entries));
  }

  public static <K, V> Map<K, V> putEntries(Map<K, V> map, Iterable<Map.Entry<K, V>> entries) {
    entries.forEach(entry -> map.put(entry.getKey(), entry.getValue()));
    return map;
  }

  public static <K, V> Map<K, V> leftValues(Map<K, MapDifference.ValueDifference<V>> diff) {
    return Maps.transformValues(diff, MapDifference.ValueDifference::leftValue);
  }

  public static <K, V> Map<K, V> rightValues(Map<K, MapDifference.ValueDifference<V>> diff) {
    return Maps.transformValues(diff, MapDifference.ValueDifference::rightValue);
  }

}
