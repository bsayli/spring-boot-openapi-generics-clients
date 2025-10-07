package io.github.bsayli.customerservice.common.api.response;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: Page")
class PageTest {

  @Test
  @DisplayName("of() -> temel metrikler doğru hesaplanır")
  void of_basic() {
    List<String> content = List.of("a", "b", "c", "d", "e", "f");
    int page = 1; // 0-index
    int size = 2;
    long total = content.size();

    Page<String> p = Page.of(content.subList(page * size, page * size + size), page, size, total);

    assertEquals(2, p.content().size());
    assertEquals(1, p.page());
    assertEquals(2, p.size());
    assertEquals(6, p.totalElements());
    assertEquals(3, p.totalPages()); // ceil(6/2)=3
    assertTrue(p.hasNext()); // page=1, totalPages=3 => next var
    assertTrue(p.hasPrev()); // page>0 => prev var
  }

  @Test
  @DisplayName("of() -> content null ise boş listeye sabitlenir")
  void of_nullContent() {
    Page<String> p = Page.of(null, 0, 10, 0);
    assertNotNull(p.content());
    assertTrue(p.content().isEmpty());
  }

  @Test
  @DisplayName("of() -> content kopyalanır ve dışarıdan değiştirilemez")
  void of_immutableContent() {
    List<String> src = new ArrayList<>(List.of("x", "y"));
    Page<String> p = Page.of(src, 0, 10, 2);

    src.add("z");
    assertEquals(2, p.content().size());

    assertThrows(UnsupportedOperationException.class, () -> p.content().add("w"));
  }

  @Test
  @DisplayName("of() -> son sayfada hasNext=false, ilk sayfada hasPrev=false")
  void of_navFlags() {
    List<Integer> content = List.of(1, 2, 3, 4, 5);
    Page<Integer> first = Page.of(content.subList(0, 2), 0, 2, content.size());
    assertFalse(first.hasPrev());
    assertTrue(first.hasNext());

    Page<Integer> last = Page.of(content.subList(4, 5), 2, 2, content.size());
    assertTrue(last.hasPrev());
    assertFalse(last.hasNext());
  }
}
