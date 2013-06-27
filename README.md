
Vim + TopCoder = VimCoder
=========================

![VimCoder Logo](http://chazmcgarvey.github.com/vimcoder/img/vimcoder.png)

This plug-in makes it easy to use [Vim](http://www.vim.org/) as your
text editor in the [TopCoder Competition Arena](http://topcoder.com/tc).
It aims to be minimal in comparison to other editors such as
[KawigiEdit](http://topcoder.yajags.com/) or
[CodeProcessor](http://www.topcoder.com/tc?module=Static&d1=applet&d2=plugins)
plug-in chains while also providing enough functionality to also be useful.

Features
--------

* Works on any platform that the TopCoder Arena applet itself supports.
* Works with any language supported by TopCoder.
* Keeps track of your code files locally so you always have a copy.
* Downloads and stores a copy of the problem statement with your code for
  off-line viewing.
* Has support for simple templates (default templates provided only for C++
  and Java).
* Test-case "drivers" can be generated locally with the example test-case data
  (currently C++ only).

License
-------

This software is licensed according to the terms and conditions of the
[BSD 2-Clause License](http://www.opensource.org/licenses/bsd-license.php).
Please see the COPYING file for more information.
This project is neither supported nor endorsed by TopCoder, Inc.

Download
--------

The latest VimCoder jar file can be downloaded from the
[vim.org script page](http://www.vim.org/scripts/script.php?script_id=3321).

Install
-------

Unfortunately, installation is a bit cumbersome, but it is what it is:

1. Download the latest version of the VimCoder jar file.
2. Run the TopCoder Arena applet and log in.
3. Click the "Options" menu and select "Editor" to show the editor
   preferences.
4. Click the "Add" button to bring up a new window.
5. For "Name," type "Vim" or whatever you want to represent this plug-in.
6. For "EntryPoint," type "com.dogcows.VimCoder" without the quotes.
7. For "ClassPath," click on "Browse" and locate the VimCoder jar file.  The
   third field should now have the path to the jar file.
8. Click "OK" to close the window with the three fields.
9. Click "Save."

You should now be able select "Vim" (or whatever you entered into the first
field) as your editor from the pull-down list on any problem statement window.

Configure
---------

Depending on your preference or system attributes, you may want or need to
first configure the plug-in so that it will work how you want it to.  You can
bring up the plug-in preferences window by following these steps:

1. Run the TopCoder Arena applet and log in.
2. Click the "Options" menu and select "Editor."
3. In the new window, make sure the entry for VimCoder is selected from the
   list, and click the "Configure" button.

![VimCoder Preferences](http://chazmcgarvey.github.com/vimcoder/img/prefs.png)

##### Storage Directory

VimCoder saves the problem files and the code you're working on in
a particular directory you can set.  By default, this directory is `.vimcoder`
in your home directory.  This is an "invisible" directory on most systems.
Within this storage directory are several sub-directories, one for each
problem you open.  Each sub-directory is named after the problem identifier
and contains your source code and other files related to the problem.

If you want to change the storage directory, click the "Browse" button in the
VimCoder preferences window and navigate to the directory you would like to
use.  If you have already saved some problems to the previous storage
directory, you may also want to actually move the directory to the new
location so that VimCoder can find the work you've already done.

Beginning with VimCoder 0.3.5, there is a new option for an alternative
directory structure.  It is not enabled by default, but it may be in the
future.  Rather than having directories named after problem identifiers, the
new structure uses two levels of directories.  On the first level, directories
are named after the contest associated with the problem (e.g. SRM-144-DIV-1),
and on the second level, directories are named after the problem's point value
(e.g. 300).  This directory structure may be preferable if you ever want to
browse your repository since the contest name and point values are more easily
identifiable than the problem identifier.

If this new directory structure is enabled, it will only apply to new
problems.  VimCoder will not try to reorganize your current repository, though
you are welcome to do it manually yourself if you would like to switch to the
new directory structure.

##### Vim Command

By default, VimCoder tries to invoke Vim using the `gvim` command (or
`C:\WINDOWS\gvim.bat` on Windows).  This will typically work just fine unless
you don't have gvim in your PATH (or your installation of Vim on Windows
didn't include the wrappers for the command line).  If you get errors about
the vim process not being able to run and no Vim session comes up when you use
the VimCoder plug-in, you need to either make sure the Vim command exists in
your PATH, or else change the Vim command in the VimCoder preferences window
to something else.

You may use an absolute path to your vim executable, such as
`/usr/local/bin/gvim` or `C:\Program Files\Vim\vim73\gvim.exe`
or wherever your actual Vim executable is.  You may also invoke vim through
some other command (e.g. `xterm -e vim` or `gnome-terminal -e vim --`).

The xterm example above demonstrates using Vim without the GUI, running in
a terminal emulator.  You can enter any elaborate command you want as long as
Vim ultimately gets executed with the arguments that will be appended to the
command when it is invoked.  After changing this value and saving your
preferences, the command you enter will be used the next time you open
a problem.

Usage
-----

To use VimCoder once it is installed and configured, go to a room in the
TopCoder Arena applet and open one of the problems.  If you have set VimCoder
as your default editor, you will see the usual problem statement window come
up as well as a separate Vim editor window.  Otherwise, you can change the
editor from the problem statement window, and the Vim editor window will come
up.  You will see that the area usually devoted to editor will be used for log
messages; you will do your actual coding in the Vim window that comes up.

Just enter your code into the Vim window and use the regular TopCoder Arena
applet buttons to compile, test, and submit your code.

**Pro Tip:** If you accidentally close your Vim session, you can get it back
by switching to a different editor (such as the default editor) and then
switching back to VimCoder.  Alternatively, the session will also reappear
(and load a buffer to a different source code file) if you switch languages.

Storage Directory Structure
---------------------------

Knowing about the files created by VimCoder is useful if you ever need to do
anything advanced.  When you open a problem, VimCoder will check to see if you
have already opened that problem by looking for the problem and solution
files.  If these files are found, it will load your previous work.  Otherwise,
it will fill out the templates based on the problem class name, parameter
types, and so on, and will create several files in a sub-directory of the main
storage directory:

##### `$CLASSNAME$`.`$LANGUAGE$`

This is the file where you write your solution code.  If the class name for
the problem was BinaryCode and your language was Java, the name of this file
would be `BinaryCode.java`.  When you open a problem, Vim will load this file
into a new buffer so that you can start coding.  If there is a template for
the language you're using, that template will be used to fill in this file to
give you a reasonable place to start.  When you save your code to TopCoder or
compile remotely, this is also the file that will be read to provide the code
for your solution.

##### testcases.txt

This file contains the example test cases that are associated with the
problem.  The format is pretty simple.  For each test case, there is one line
for the expected return value followed by the inputs (i.e. the method
arguments), in order, each on its own line.  The format of this file is meant
to be easy for a human to write and easy for a program to read so that
a driver program (more on this later) can easily be written to run the test
cases against your code.

While you are coding a solution, you may want to open this file in a new
buffer (type ":e testcases.txt") and add additional test cases to make sure
your code doesn't mess up on edge cases for which an example test case was not
provided.

##### Problem.html

This file contains the problem statement which is what you see in the top half
of the problem window.  You can load this in a browser to read the particulars
of the problem when you aren't running the TopCoder Arena applet.  You
typically shouldn't edit this file, but it's up to you.

##### Makefile

If there exists a Makefile template for the selected language, it will also be
filled out and saved in the problem directory.  The purpose of the Makefile is
to compile your code locally.  You can execute targets in the Makefile using
Vim's `:make` command.  You also shouldn't need to edit this file directly,
but of course you can if the need does arise.  Exactly what happens when you
use the `:make` command depends on the Makefile template.

If you are using the default Makefile template for C++, typing ":make" without
any arguments will compile your code.  Typing ":make run" will run all of the
test cases against your code.  Typing ":make test" will also run the test
cases against your code, except it will abort at the first failed test.

A Makefile template is not yet provided for any other language, but you can
write one yourself if you are so inclined.  Read on to learn how templates
work.

**Important:** Make sure you understand the difference between compiling
locally and compiling remotely (on the TopCoder servers).  If you use the
Makefile to compile your solution (and maybe run the tests), you are not
interacting with the TopCoder servers at all.  When you compile *remotely*,
you are sending a snapshot of your current solution to the servers for
processing.  The distinction becomes important when it comes time for you to
submit your solution.  When you push the "Submit" button, you are submitting
the **last version that was uploaded to the TopCoder servers** (by compiling
remotely), and that may be different from what is currently in your Vim
buffer, even if your Vim buffer was saved to disk.  Therefore, it is very
important that you get into the habit of always pushing the "Compile" button
right before you submit your code.  This point can't be overemphasized.

##### driver.`$LANGUAGE$`

If there exists a driver template for the selected language, it will also be
filled out and saved in the problem directory.  If the language was currently
set to C++, the driver code would be in the driver.cc file.  You normally
don't have to do anything with this file.  It just provides supporting code
for running the test cases against your code.

The driver should output TAP (Test Anything Protocol) so that tests can be run
in a test harness such as [prove](http://search.cpan.org/perldoc?prove).  The
default Makefile template has a `prove` target (type ":make prove") that can
run the tests in a test harness; the test harness is `prove` unless otherwise
configured.  TAP output is also very human-readable all by itself, so having
a test harness isn't really required.

A default driver template is currently only provided for the C++ language.
You could write your own template if you wanted to.

##### `$CLASSNAME$`

Sometimes the TopCoder Arena applet will pass back what source code it has
saved.  This will be saved in a file named after the class, without any file
extension.  You can open this file if you need to access this code for any
reason (say, you messed up some code and need to revert back to the last time
you saved from the TopCoder Arena applet).

Templates
---------

VimCoder comes with default templates for C++ and Java, but you can create
your own customized templates for any language supported by TopCoder.  To use
your own template, you need to add a file to the storage directory with a file
name depending on the language.  The file name should start with the name of
the language and end with "Template" with no file extension.  For example, if
you wanted to create a C# template and your storage directory was
`/home/foo/.topcoder`, you would need to create the file
`/home/foo/.topcoder/C#Template`.

A template is like a regular source code file with special keywords that will
be replaced as the template is "filled out" whenever you open a new problem.
Keywords are surrounded by two dollar signs so they're not confused with other
parts of the source code.  The template expansion process is rather
simplistic, so if you can't get the right format for the terms you need, you
might have to change the plug-in source code to get the effect you're trying
to achieve.  Here are the possible keywords and replacement terms:

##### `$CLASSNAME$`

This keyword is replaced by the name of the class you must use in your
solution to the problem.

##### `$METHODNAME$`

This keyword is replaced by the name of the public method your class needs to
have.

##### `$RETURNTYPE$`

This keyword is replaced by the type of the return variable of your public
method.

##### `$METHODPARAMS$`

This keyword is replaced by a comma-separated list of method parameter types
and names.

----

Other keywords are also available, but the rest are intended to be used in
driver or Makefile templates, though any keyword can be used in any type of
template.  You can create other types of templates by adding specially-named
files to the storage directory.  Driver templates are named starting with the
name of the language and ending with "Driver" with no file extension.
Similarly, Makefile templates are named starting with the name of the language
and ending with "Makefile" with no file extension.

Drivers provide additional code that allows the test cases to be run against
your solution.  Currently, Makefile and driver templates are only provided for
the C++ language.  Makefiles should have the commands needed to compile the
solution source code and/or make a driver program that will perform the tests.
If you want automatic building and testing for one of the other languages, you
will need to create a driver and Makefile template for that language.  Here
are more keywords that may be useful for these types of templates:

##### `$METHODPARAMDECLARES$`

This keyword is replaced by C-style declarations of the method parameters.  In
other words, each parameter is declared with its type on its own line
terminated by a semicolon.

##### `$METHODPARAMNAMES$`

This keyword is replaced by a comma-separated list of only the method
parameter names.

##### `$METHODPARAMSTREAMOUT$`

This keyword is replaced by a list of the method parameter names separated by
the C++ output stream operator (<<).  The C++ driver template uses this to
display the input values of the test case data.

##### `$METHODPARAMSTREAMIN$`

This keyword is replaced by a list of the method parameter names separated by
the C++ input stream operator (>>).  The C++ driver template uses this to read
in the test case data from testcases.txt.

----

To give you an idea of how this all fits together, here is an example template
for Java, similar to the built-in default Java template:

```java
import static java.lang.Math.*;
import static java.math.BigInteger.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import java.math.*;
import java.util.*;

public class $CLASSNAME$ {
    public $RETURNTYPE$ $METHODNAME$($METHODPARAMS$) {
    }
}
```

Notice that it looks just like regular code but has some keywords surrounded
by dollar signs that will be expanded to real values.  Something like this
could be saved in a filed named `JavaTemplate` in your VimCoder storage
directory.

Potential Pitfalls
------------------

##### Vim Client/Server

VimCoder requires Vim's client/server feature in order to work.  If the log is
showing errors with the invocation of Vim or if it's just not working and has
other strange symptoms, make sure your version of Vim supports the
client/server feature.  If you are unsure, use Vim's `:version` command and
look for "+clientserver" in the output.  If you see "-clientserver" instead,
then you'll need to get yourself another version of Vim.

I think this feature was introduced in Vim 6.x, but I haven't done any testing
with any versions of Vim less than 7.2.  If you're still on 6.x, you should
really upgrade anyway.

##### Vim Settings Not Applied

The problem is that sometimes your settings (in your vimrc file) are not being
applied as you would expect.  This may be because you are using `setlocal` in
your vimrc file rather than `set`.  The `setlocal` command applies settings
only to the current buffer or window (see `:help setlocal` for more
information), but VimCoder works by first launching Vim and then loading
a brand new buffer.

The solution is to consider whether or not such settings should actually be
global; if they should be global, change `setlocal` to `set` in your vimrc
file.  Alternatively, if you want certain settings to be set only for certain
kinds of buffers, you can use the `autocmd` command to selectively set
settings according to file path pattern and various events.
See `:help autocmd` for more information.

