package fi.thl.termed.util.query;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

public class MatchNone<K extends Serializable, V>
    implements SqlSpecification<K, V>, LuceneSpecification<K, V> {

  @Override
  public boolean test(K k, V v) {
    return false;
  }

  @Override
  public Query luceneQuery() {
    return new BooleanQuery.Builder().build();
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("1 = 0");
  }

  @Override
  public int hashCode() {
    return MatchNone.class.hashCode();
  }

  @Override
  public boolean equals(Object that) {
    return this == that || that instanceof MatchNone;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }

}
