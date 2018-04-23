
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

@RunWith(Enclosed.class)
public class LinkParserTest {

	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class SingleURLTests {

		public void testValid(String link, String html) throws MalformedURLException {
			URL base = new URL("http://www.example.com");
			URL expected = new URL(link);
			ArrayList<URL> actual = LinkParser.listLinks(base, html);

			String debug = String.format("%nHTML:%n%s%n%n", html);
			Assert.assertEquals(debug, expected, actual.get(0));
		}

		public void testInvalid(String html) throws MalformedURLException {
			URL base = new URL("http://www.example.com");
			ArrayList<URL> actual = LinkParser.listLinks(base, html);

			String debug = String.format("%nHTML:%n%s%n%nLinks:%n%s%n", html, actual);
			Assert.assertEquals(debug, 0, actual.size());
		}

		@Test
		public void test01Simple() throws MalformedURLException {
			String link = "http://www.usfca.edu/";
			String html = "<a href=\"http://www.usfca.edu/\">";
			testValid(link, html);
		}

		@Test
		public void test02Fragment() throws MalformedURLException {
			String link = "http://docs.python.org/library/string.html?highlight=string";
			String html = "<a href=\"http://docs.python.org/library/string.html?highlight=string#module-string\">";
			testValid(link, html);
		}

		@Test
		public void test03Uppercase() throws MalformedURLException {
			String link = "HTTP://WWW.USFCA.EDU";
			String html = "<A HREF=\"HTTP://WWW.USFCA.EDU\">";
			testValid(link, html);
		}

		@Test
		public void test04MixedCase() throws MalformedURLException {
			String link = "http://www.usfca.edu";
			String html = "<A hREf=\"http://www.usfca.edu\">";
			testValid(link, html);
		}

		@Test
		public void test05Spaces() throws MalformedURLException {
			String link = "http://www.usfca.edu";
			String html = "<a href = \"http://www.usfca.edu\" >";
			testValid(link, html);
		}

		@Test
		public void test06OneNewline() throws MalformedURLException {
			String link = "http://www.usfca.edu";
			String html = "<a href = \n \"http://www.usfca.edu\">";
			testValid(link, html);
		}

		@Test
		public void test07ManyNewlines() throws MalformedURLException {
			String link = "http://www.usfca.edu";
			String html = "<a\n\nhref\n=\n\"http://www.usfca.edu\"\n>";
			testValid(link, html);
		}

		@Test
		public void test08Snippet() throws MalformedURLException {
			String link = "http://www.usfca.edu";
			String html = "<p><a href=\"http://www.usfca.edu\">USFCA</a> is in San Francisco.</p>";
			testValid(link, html);
		}

		@Test
		public void test09Relative() throws MalformedURLException {
			String link = "http://www.example.com/index.html";
			String html = "<a href=\"index.html\">";
			testValid(link, html);
		}

		@Test
		public void test10HREFLast() throws MalformedURLException {
			String link = "http://www.example.com/index.html";
			String html = "<a name=\"home\" href=\"index.html\">";
			testValid(link, html);
		}

		@Test
		public void test11HREFFirst() throws MalformedURLException {
			String link = "http://www.example.com/index.html";
			String html = "<a href=\"index.html\" class=\"primary\">";
			testValid(link, html);
		}

		@Test
		public void test12MultipleAttributes() throws MalformedURLException {
			String link = "http://www.example.com/index.html";
			String html = "<a name=\"home\" target=\"_top\" href=\"index.html\" id=\"home\" accesskey=\"A\">";
			testValid(link, html);
		}

		@Test
		public void test13NoHREF() throws MalformedURLException {
			String html = "<a name = \"home\">";
			testInvalid(html);
		}

		@Test
		public void test14NoAnchor() throws MalformedURLException {
			String html = "<h1>Home</h1>";
			testInvalid(html);
		}

		@Test
		public void test15MixedNoHREF() throws MalformedURLException {
			String html = "<a name=href>The href = \"link\" attribute is useful.</a>";
			testInvalid(html);
		}

		@Test
		public void test16LinkTag() throws MalformedURLException {
			String html = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">";
			testInvalid(html);
		}

		@Test
		public void test17NoTag() throws MalformedURLException {
			String html = "<p>The a href=\"http://www.google.com\" attribute is often used in HTML.</p>";
			testInvalid(html);
		}
	}

	public static class MultipleLinkTest {
		@Test
		public void testMultiple() throws MalformedURLException {
			String html = "<h1><a name=\"about\">About</a></h1>\n"
					+ "<p>The <a class=\"primary\" href=\"index.html\">Department of "
					+ "Computer Science</a> offers an undergraduate and graduate degree at "
					+ "<a href=\"http://www.usfca.edu\">University of San Francisco</a>.</p>\n"
					+ "<p>Find out more about those degrees at <a href=\"https://www.usfca.edu/"
					+ "catalog/undergraduate/arts-sciences/computer-science\">https://www.usfca.edu/"
					+ "catalog/undergraduate/arts-sciences/computer-science</a>.</p>";

			ArrayList<URL> expected = new ArrayList<>();

			Collections.addAll(expected, new URL("http://www.cs.usfca.edu/index.html"), new URL("http://www.usfca.edu"),
					new URL("https://www.usfca.edu/catalog/undergraduate/arts-sciences/computer-science"));

			URL base = new URL("http://www.cs.usfca.edu/");
			ArrayList<URL> actual = LinkParser.listLinks(base, html);

			String debug = String.format("%nHTML:%n%s%n%n", html);
			Assert.assertEquals(debug, expected, actual);
		}
	}

	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class RemoteLinkTest {
		/*
		 * Do not run these tests until you are passing the others! You risk
		 * being rate-limited or banned from the web server if you access it too
		 * frequently while testing.
		 *
		 * These are the same seed URLs you will use in project 4, so getting the
		 * parsing correct now will help tremendously!
		 */

		public void testRemote(String url, ArrayList<URL> expected) throws MalformedURLException {
			URL base = new URL(url);
			String html = LinkParser.fetchHTML(base);
			ArrayList<URL> actual = LinkParser.listLinks(base, html);

			StringBuffer debug = new StringBuffer("\nLinks (Expected, Actual):\n");

			for (int i = 0; i < Math.max(expected.size(), actual.size()); i++) {
				debug.append(i + ":\t");

				if (i < expected.size() && i < actual.size() && expected.get(i).equals(actual.get(i))) {
					debug.append("OKAY\t ");
					debug.append(expected.get(i));
				}
				else {
					debug.append("ERROR\t");
					debug.append(i < expected.size() ? expected.get(i) : "null");
					debug.append(",\t");
					debug.append(i < actual.size() ? actual.get(i) : "null");
				}

				debug.append("\n");
			}

			debug.append("\nHTML:\n");
			debug.append(html);
			debug.append("\n");
			System.out.println(debug);
			Assert.assertEquals(debug.toString(), expected, actual);
		}

		@Test
		public void test01Hello() throws MalformedURLException {
			ArrayList<URL> expected = new ArrayList<>();
			testRemote("http://vis.cs.ucdavis.edu/~cjbryan/cs212/simple/hello.html", expected);
		}

		@Test
		public void test02Simple() throws MalformedURLException {
			ArrayList<URL> expected = new ArrayList<>();
			Collections.addAll(expected, new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/simple/a/b/c/subdir.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/simple/capital_extension.HTML"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/simple/double_extension.html.txt"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/simple/empty.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/simple/hello.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/simple/mixed_case.htm"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/simple/no_extension"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/simple/position.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/simple/symbols.html"));
			testRemote("http://vis.cs.ucdavis.edu/~cjbryan/cs212/simple/index.html", expected);
		}

		@Test
		public void test03Birds() throws MalformedURLException {
			ArrayList<URL> expected = new ArrayList<>();
			Collections.addAll(expected, new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/albatross.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/blackbird.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/bluebird.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/cardinal.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/chickadee.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/crane.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/crow.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/cuckoo.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/dove.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/duck.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/eagle.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/egret.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/falcon.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/finch.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/goose.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/gull.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/hawk.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/heron.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/hummingbird.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/ibis.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/kingfisher.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/loon.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/magpie.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/mallard.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/meadowlark.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/mockingbird.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/nighthawk.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/osprey.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/owl.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/pelican.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/pheasant.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/pigeon.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/puffin.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/quail.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/raven.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/roadrunner.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/robin.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/sandpiper.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/sparrow.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/starling.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/stork.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/swallow.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/swan.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/tern.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/turkey.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/vulture.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/warbler.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/woodpecker.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/wren.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/yellowthroat.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/birds.html"));
			testRemote("http://vis.cs.ucdavis.edu/~cjbryan/cs212/birds/birds.html", expected);
		}

		@Test
		public void test04Recurse() throws MalformedURLException {
			ArrayList<URL> expected = new ArrayList<>();
			expected.add(new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/recurse/link02.html"));
			testRemote("http://vis.cs.ucdavis.edu/~cjbryan/cs212/recurse/link01.html", expected);
		}

		@Test
		public void test05SecondVariety() throws MalformedURLException {
			ArrayList<URL> expected = new ArrayList<>();
			expected.add(new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/gutenberg/32032-h/images/illo1-left.jpg"));
			expected.add(new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/gutenberg/32032-h/images/illo1-right.jpg"));
			expected.add(new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/gutenberg/32032-h/images/illo2.jpg"));
			testRemote("http://vis.cs.ucdavis.edu/~cjbryan/cs212/gutenberg/32032-h/32032-h.htm", expected);
		}

		@Test
		public void test06Gutenberg() throws MalformedURLException {
			ArrayList<URL> expected = new ArrayList<>();
			expected.add(new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/gutenberg/31516-h/31516-h.htm"));
			expected.add(new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/gutenberg/32032-h/32032-h.htm"));
			expected.add(new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/gutenberg/32154-h/32154-h.htm"));
			expected.add(new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/gutenberg/32522-h/32522-h.htm"));
			testRemote("http://vis.cs.ucdavis.edu/~cjbryan/cs212/gutenberg/", expected);
		}

		@Test
		public void test07CSSProperties() throws MalformedURLException {
			ArrayList<URL> expected = new ArrayList<>();
			Collections.addAll(expected,
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/all-properties.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/syntax.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/font/index.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/font/font-family.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/font/font-style.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/font/font-variant.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/font/font-weight.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/font/font-size.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/font/font.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/color-background/index.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/color-background/color.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/color-background/background-color.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/color-background/background-image.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/color-background/background-repeat.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/color-background/background-attachment.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/color-background/background-position.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/color-background/background.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/text/index.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/text/word-spacing.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/text/letter-spacing.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/text/text-decoration.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/text/vertical-align.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/text/text-transform.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/text/text-align.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/text/text-indent.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/text/line-height.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/index.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/margin-top.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/margin-right.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/margin-bottom.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/margin-left.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/margin.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/padding-top.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/padding-right.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/padding-bottom.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/padding-left.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/padding.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-top-width.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-right-width.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-bottom-width.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-left-width.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-width.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-color.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-style.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-top.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-right.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-bottom.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-left.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/width.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/height.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/float.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/clear.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/classification/index.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/classification/display.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/classification/white-space.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/classification/list-style-type.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/classification/list-style-image.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/classification/list-style-position.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/classification/list-style.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/units.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/units.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/units.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/units.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/units.html"),
				new URL("http://www.htmlhelp.com/"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/index.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/structure.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/properties.html"),
				new URL("http://www.htmlhelp.com/copyright.html"));
			testRemote("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/properties.html", expected);
		}

		@Test
		public void test08CSS() throws MalformedURLException {
			ArrayList<URL> expected = new ArrayList<>();
			Collections.addAll(expected,
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/all-properties.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/syntax.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/font/index.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/font/font-family.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/font/font-style.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/font/font-variant.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/font/font-weight.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/font/font-size.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/font/font.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/color-background/index.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/color-background/color.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/color-background/background-color.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/color-background/background-image.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/color-background/background-repeat.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/color-background/background-attachment.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/color-background/background-position.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/color-background/background.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/text/index.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/text/word-spacing.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/text/letter-spacing.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/text/text-decoration.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/text/vertical-align.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/text/text-transform.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/text/text-align.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/text/text-indent.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/text/line-height.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/index.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/margin-top.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/margin-right.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/margin-bottom.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/margin-left.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/margin.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/padding-top.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/padding-right.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/padding-bottom.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/padding-left.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/padding.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-top-width.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-right-width.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-bottom-width.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-left-width.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-width.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-color.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-style.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-top.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-right.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-bottom.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border-left.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/border.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/width.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/height.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/float.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/box/clear.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/classification/index.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/classification/display.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/classification/white-space.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/classification/list-style-type.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/classification/list-style-image.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/classification/list-style-position.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/classification/list-style.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/units.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/units.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/units.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/units.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/units.html"),
				new URL("http://www.htmlhelp.com/"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/index.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/structure.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/properties.html"),
				new URL("http://www.htmlhelp.com/copyright.html"));
			testRemote("http://vis.cs.ucdavis.edu/~cjbryan/cs212/wdgcss/properties.html", expected);
		}

		@Test
		public void test09NumpyQuick() throws MalformedURLException {
			ArrayList<URL> expected = new ArrayList<>();
			Collections.addAll(expected,
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/index.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/index.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/genindex.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/basics.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/install.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/contents.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/install.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/basics.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://docs.python.org/tut/"),
				new URL("http://scipy.org/install.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.array.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.zeros.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.zeros_like.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.ones.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.ones_like.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.empty.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.empty_like.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.arange.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.linspace.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.random.rand.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.random.randn.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.fromfunction.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.fromfile.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.all.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.any.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.apply_along_axis.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.argmax.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.argmin.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.argsort.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.average.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.bincount.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.ceil.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.clip.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.conj.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.corrcoef.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.cov.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.cross.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.cumprod.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.cumsum.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.diff.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.dot.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.floor.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.inner.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.lexsort.html"),
				new URL("https://docs.python.org/dev/library/functions.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.maximum.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.mean.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.median.html"),
				new URL("https://docs.python.org/dev/library/functions.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.minimum.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.nonzero.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.outer.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.prod.html"),
				new URL("https://docs.python.org/dev/library/re.html"),
				new URL("https://docs.python.org/dev/library/functions.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.sort.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.std.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.sum.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.trace.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.transpose.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.var.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.vdot.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.vectorize.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.where.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("https://docs.python.org/tutorial/introduction.html"),
				new URL("https://docs.python.org/2/tutorial/classes.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/basics.indexing.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/arrays.indexing.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/arrays.indexing.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.ndenumerate.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.indices.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.reshape.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.ndarray.resize.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.ndarray.shape.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.reshape.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.resize.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.ravel.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.column_stack.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.vstack.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.hstack.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.vstack.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.concatenate.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.r_.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.c_.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.r_.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.c_.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.vstack.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.hstack.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.hstack.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.vstack.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.column_stack.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.concatenate.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.c_.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.r_.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.hsplit.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.vsplit.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.array_split.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/routines.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.arange.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.array.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.copy.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.empty.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.empty_like.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.eye.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.fromfile.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.fromfunction.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.identity.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.linspace.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.logspace.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.mgrid.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.ogrid.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.ones.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.ones_like.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.zeros.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.zeros_like.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.ndarray.astype.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.atleast_1d.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.atleast_2d.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.atleast_3d.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.mat.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.array_split.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.column_stack.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.concatenate.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.diagonal.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.dsplit.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.dstack.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.hsplit.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.hstack.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.ndarray.item.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/arrays.indexing.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.ravel.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.repeat.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.reshape.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.resize.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.squeeze.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.swapaxes.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.take.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.transpose.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.vsplit.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.vstack.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.all.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.any.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.nonzero.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.where.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.argmax.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.argmin.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.argsort.html"),
				new URL("https://docs.python.org/dev/library/functions.html"),
				new URL("https://docs.python.org/dev/library/functions.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.ptp.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.searchsorted.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.sort.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.choose.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.compress.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.cumprod.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.cumsum.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.inner.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.ndarray.fill.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.imag.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.prod.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.put.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.putmask.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.real.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.sum.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.cov.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.mean.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.std.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.var.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.cross.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.dot.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.outer.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.linalg.svd.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.vdot.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/basics.broadcasting.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://en.wikipedia.org/wiki/Mandelbrot_set"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart-1.py"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart-1.png"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart-1.pdf"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/generated/numpy.ix_.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/Tentative_NumPy_Tutorial.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/basics.rec.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/numpy-for-matlab-users.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart-2.py"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart-2_00_00.png"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart-2_00_00.pdf"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart-2_01_00.png"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart-2_01_00.pdf"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://docs.python.org/tutorial/"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/index.html"),
				new URL("https://docs.scipy.org/doc/scipy/reference/tutorial/index.html"),
				new URL("http://www.scipy-lectures.org"),
				new URL("http://mathesaurus.sf.net/"),
				new URL("http://sphinx.pocoo.org/"));
			testRemote("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html", expected);
		}

		@Test
		public void test10Numpy() throws MalformedURLException {
			ArrayList<URL> expected = new ArrayList<>();
			Collections.addAll(expected,
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/index.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/genindex.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/setting-up.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/contents.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/contents.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/setting-up.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/index.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/reference/index.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/setting-up.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/quickstart.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/basics.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/misc.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/numpy-for-matlab-users.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/building.html"),
				new URL("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/c-info.html"),
				new URL("http://sphinx.pocoo.org/"));
			testRemote("http://vis.cs.ucdavis.edu/~cjbryan/cs212/numpy/user/index.html", expected);
		}
	}
}
