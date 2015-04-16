package aat;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class AsciiArtTable {

  private static String prependToLength(final Object subject, final int length) {
    if (subject.toString().length() < length) {
      return StringUtils.repeat(' ', length - subject.toString().length()) + subject;
    }
    return subject.toString();
  }

  private final List<Object> contentCols;
  private final List<Object> headerCols;
  private final List<Object> headlines;
  private final int padding;

  public AsciiArtTable() {
    this(1);
  }

  public AsciiArtTable(final int padding) {
    this.headerCols = new ArrayList<>();
    this.contentCols = new ArrayList<>();
    this.headlines = new ArrayList<>();
    this.padding = padding;
  }

  public void add(final List<Object> contentCols) {
    this.contentCols.addAll(contentCols);
  }

  public void add(final Object... contentCols) {
    add(new ArrayList<>(Arrays.asList(contentCols)));
  }

  public void addHeaderCols(final List<Object> headerCols) {
    this.headerCols.addAll(headerCols);
  }

  public void addHeaderCols(final Object... headerCols) {
    addHeaderCols(new ArrayList<>(Arrays.asList(headerCols)));
  }

  public void addHeadline(final Object headline) {
    this.headlines.add(headline);
  }

  public void clear() {
    headerCols.clear();
    contentCols.clear();
  }

  private int[] getColWidths() {
    int[] result = new int[headerCols.size()];
    int col = 0;
    while (col < headerCols.size()) {
      result[col] = headerCols.get(col).toString().length();
      col++;
    }
    int index = 0;
    while (index < contentCols.size()) {
      col = index % headerCols.size();
      if (contentCols.get(index).toString().length() > result[col]) {
        result[col] = contentCols.get(index).toString().length();
      }
      index++;
    }
    return result;
  }

  public String getOutput() {
    // prepare data
    while (contentCols.size() % headerCols.size() != 0) {
      contentCols.add("");
    }
    // build header
    String result = "";
    if (headlines.isEmpty()) {
      result += row('╔', '═', '╤', '╗') + System.lineSeparator();
    } else {
      result += row('╔', '═', '═', '╗') + System.lineSeparator();
      for (Object headline : headlines) {
        result += rowHeadline(headline.toString(), '║', '║');
        if (headlines.indexOf(headline) == headlines.size() - 1) {
          result += row('╟', '─', '┬', '╢') + System.lineSeparator();
        } else {
          result += row('╟', '─', '─', '╢') + System.lineSeparator();
        }
      }
    }
    result += line(headerCols, '║', '│', '║') + System.lineSeparator();
    result += row('╠', '═', '╪', '╣') + System.lineSeparator();
    int col = 0;
    while (col < contentCols.size()) {
      result += line(contentCols.subList(col, col + headerCols.size()), '║', '│', '║') + System.lineSeparator();
      col += headerCols.size();
      if (col == contentCols.size()) {
        result += row('╚', '═', '╧', '╝') + System.lineSeparator();
      } else {
        result += row('╟', '─', '┼', '╢') + System.lineSeparator();
      }
    }
    return result;
  }

  private int getTableLength() {
    final int[] colWidths = getColWidths();
    int result = 0;
    for (int colWidth : colWidths) {
      result += colWidth + 2 * padding;
    }
    return result + colWidths.length + 1;
  }

  private String line(final List<Object> contents, final char left, final char columnSeparator, final char right) {
    final int[] colWidths = getColWidths();
    String result = left + "";
    int col = 0;
    while (col < headerCols.size()) {
      result += prependToLength(contents.get(col), padding + colWidths[col]);
      result += StringUtils.repeat(' ', padding);
      col++;
      result += col == headerCols.size() ? right : columnSeparator;
    }
    return result;
  }

  public void print(final PrintStream printStream) {
    printStream.print(getOutput());
  }

  private String row(final char left, final char middle, final char columnSeparator, final char right) {
    final int[] colWidths = getColWidths();
    String result = left + "";
    int col = 0;
    while (col < headerCols.size()) {
      result += StringUtils.repeat(middle, padding + colWidths[col] + padding);
      col++;
      result += col == headerCols.size() ? right : columnSeparator;
    }
    return result;
  }

  private String rowHeadline(final String headline, final char left, final char right) {
    final int tableLength = getTableLength();
    final int contentWidth = tableLength - (2 * padding);
    // FIXME a single word could be longer than the table
    // split into headline rows
    final List<String> headlineLines = new ArrayList<>();
    final String[] headlineWords = headline.split(" ");
    List<String> rowWords = new ArrayList<>();
    for (int index = 0; index < headlineWords.length; index++) {
      if (index + 1 != headlineWords.length && StringUtils.join(rowWords, ' ').length() + 1 + headlineWords[index + 1].length() >= contentWidth) {
        headlineLines.add(StringUtils.join(rowWords, ' '));
        rowWords.clear();
      }
      rowWords.add(headlineWords[index]);
    }
    if (!rowWords.isEmpty()) {
      headlineLines.add(StringUtils.join(rowWords, ' '));
    }
    // build result
    String result = "";
    for (String headlineLine : headlineLines) {
      result += left + StringUtils.repeat(' ', padding) + StringUtils.rightPad(headlineLine, tableLength - padding - 2) + right + System.lineSeparator();
    }
    return result;
  }
}
