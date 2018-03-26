HTML Link Parser
=================================================

Eventually, we want to be able to crawl the web and parse web pages. As such, we must be able to find the links embedded in the HTML code of the web pages. This assignment is a step towards this functionality. Specifically, you must:

1. Implement the `String fetchHTML(URL url)` method to return the HTML (and no HTTP headers) for the given URL using sockets. You may use code provided in lecture. You may **NOT** use the `getContent()` or `openConnection()` methods of the `URL` class.

2. Implement the `ArrayList<URL> listLinks(URL base, String html)` method to return a list of links from the HTML `a` anchor tag `href` attribute. This should not include links in the `href` attribute of the `link` tag!

You have been provided a `URL clean(URL url)` method that will make sure URLs are in a semi-consistent properly-encoded form. You will need to figure out how to deal with relative vs absolute links, however. See below for more.

Background
-------------------------------------------------

We do not explicitly cover HTML in this class. However, there are many helpful resources on the web for more about this simple markup language. Some resources include:

* [Mozilla Developer Network](https://developer.mozilla.org/en-US/docs/Web/HTML)
* [W3C Markup Validation](http://validator.w3.org/)
* [Web Design Group](http://htmlhelp.com/)
* [Codecademy](https://www.codecademy.com/learn/web)

You will need to be familiar with the anchor tag `<a>` for this assignment. This is the tag used to create links on web pages. For example:

```html
<a href="http://www.cs.usfca.edu/">USF CS</a>
```

The above code will generate the link <a href="http://www.cs.usfca.edu/">USF CS</a>, where the link text is `USF CS` and the link destination is `http://www.cs.usfca.edu/`. The link will always be placed in the `href` attribute of the `a` tag, but not all `a` tags will have this attribute. For example, this is a valid `a` tag without the `href` attribute:

```html
<a name="home" class="bookmark">Home</a>
```

And, the `href` attribute may appear in other tags. For example, this is a valid `link` tag to include a style sheet:

```html
<link rel="stylesheet" type="text/css" href="style.css">
```

The majority of URLs on webpages are relative (i.e. specified relative to the current webpage URL). You will need to convert those relative URLs into an absolute URL. For this, you may use the `java.net.URL` class. For example, consider the following:

```java
URL base = new URL("http://www.cs.usfca.edu/~sjengle/cs212/");
URL absolute = new URL(base, "../index.html");

// outputs http://www.cs.usfca.edu/~sjengle/index.html
System.out.println(absolute);
```

This works even if the provided string was already absolute. For example:

```java
URL base = new URL("http://www.cs.usfca.edu/~sjengle/cs212/");
URL absolute = new URL(base, "http://www.example.com/");

// outputs http://www.example.com/
System.out.println(absolute);
```

Because of this, you do not need to test if a link was relative or absolute. You can simply always use the above code.

Requirements
-------------------------------------------------

The official name of this homework is `LinkParser`. This should be the name you use for your Eclipse Java project and the name you use when running the homework test script.

You must pass all unit tests when running the `/home4/public/cs212/homework` script on the lab computers to receive a 100% on this homework assignment.
